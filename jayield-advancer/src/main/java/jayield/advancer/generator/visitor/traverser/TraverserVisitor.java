package jayield.advancer.generator.visitor.traverser;

import jayield.advancer.Advancer;
import jayield.advancer.generator.visitor.constructor.ConstructorVisitor;
import jayield.advancer.generator.visitor.info.extractor.Info;
import jayield.advancer.generator.visitor.info.extractor.InfoExtractorVisitor;
import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariable;
import jayield.advancer.generator.visitor.ownership.ChangeOwnersMethodVisitor;
import jayield.advancer.generator.visitor.state.machine.StateMachineMethodVisitor;
import jayield.advancer.generator.visitor.yield.YieldVisitor;
import jayield.advancer.generator.wrapper.AbstractAdvance;
import jayield.advancer.generator.wrapper.Initializable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static jayield.advancer.generator.Constants.CONSTRUCTOR_METHOD_NAME;
import static jayield.advancer.generator.Constants.INITIALIZE;
import static jayield.advancer.generator.Constants.INT_ARRAY_DESCRIPTOR;
import static jayield.advancer.generator.Constants.STATE_FIELD_NAME;
import static jayield.advancer.generator.Constants.TRAVERSE_METHOD_NAME;
import static jayield.advancer.generator.Constants.TRY_ADVANCE_METHOD_DESC;
import static jayield.advancer.generator.Constants.TRY_ADVANCE_METHOD_NAME;
import static jayield.advancer.generator.InitializeMethodGenerator.generateInitializeMethod;
import static jayield.advancer.generator.InstrumentationUtils.getClassName;
import static jayield.advancer.generator.InstrumentationUtils.getClassPath;
import static jayield.advancer.generator.InstrumentationUtils.insertTypeInDescriptor;

public class TraverserVisitor extends ClassVisitor implements Opcodes {

    private final SerializedLambda traverser;
    private final String[] interfaces;
    private final String sourceName;
    private final String targetName;
    private final List<String> ramifications;
    private final Map<String, Info> methodInfos;
    private final String lambdaName;

    public TraverserVisitor(ClassWriter visitor, SerializedLambda traverser) {
        super(ASM6, visitor);
        this.traverser = traverser;
        this.interfaces = new String[]{getClassPath(Initializable.class), getClassPath(Advancer.class)};
        this.sourceName = traverser.getCapturingClass();
        this.targetName = getClassName(traverser);
        this.lambdaName = traverser.getImplMethodName();
        this.methodInfos = InfoExtractorVisitor.extractInfo(traverser);
        this.ramifications = methodInfos.keySet()
                                        .stream()
                                        .map(this.methodInfos::get)
                                        .map(Info::getRamifications)
                                        .reduce((prev, curr) -> {
                                            ArrayList<String> result = new ArrayList<>(prev);
                                            result.addAll(curr);
                                            return result;
                                        }).orElse(Collections.emptyList());

    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, targetName, null, getClassPath(AbstractAdvance.class), this.interfaces);
        visitFields();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        generateInitializeMethod(traverser, this, targetName);
    }

    private void visitFields() {
        Info info = methodInfos.get(this.lambdaName);
        Iterator<LocalVariable> variableIterator = info
                .getLocalVariables()
                .values()
                .iterator();
        int yieldIndex = Type.getArgumentTypes(info.getDescriptor()).length - 1;
        LocalVariable variable;
        while (variableIterator.hasNext()) {
            variable = variableIterator.next();
            if (yieldIndex != 0) {
                super.visitField(ACC_PRIVATE, variable.getName(), variable.getDesc(), variable.getSignature(), null)
                     .visitEnd();
            }
            yieldIndex--;
        }
        super.visitField(ACC_PRIVATE, STATE_FIELD_NAME, INT_ARRAY_DESCRIPTOR, null, null)
             .visitEnd();
        ramifications.forEach(ramification -> super.visitField(ACC_PRIVATE,
                                                               STATE_FIELD_NAME + ramification,
                                                               INT_ARRAY_DESCRIPTOR,
                                                               null,
                                                               null)
                                                   .visitEnd());
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(CONSTRUCTOR_METHOD_NAME + targetName)) {
            return super.visitMethod(access, CONSTRUCTOR_METHOD_NAME, desc, signature, exceptions);
        } else if (INITIALIZE.equals(name)) {
            return new ChangeOwnersMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
                                                 this.traverser.getCapturingClass(),
                                                 this.targetName);
        } else {
            Info methodInfo = this.methodInfos.get(name);
            Type[] argumentTypes = Type.getArgumentTypes(desc);

            if (shouldInstrument(name)) {
                return instrumentMethod(access, name, desc, signature, exceptions, methodInfo, argumentTypes);

            } else if (this.lambdaName.equals(name)) {
                return instrumentLambdaConstructor(signature,
                                                   exceptions,
                                                   methodInfo,
                                                   this.traverser,
                                                   argumentTypes);
            }
        }
        return null;
    }

    private MethodVisitor instrumentLambdaConstructor(String signature,
                                                      String[] exceptions,
                                                      Info methodInfo,
                                                      SerializedLambda traverser,
                                                      Type[] argumentTypes) {
        ConstructorVisitor.generateConstructor(this,
                                               methodInfo.getLocalVariables(),
                                               targetName,
                                               traverser,
                                               traverser.getCapturedArgCount(),
                                               ramifications);
        return new StateMachineMethodVisitor(
                super.visitMethod(ACC_PUBLIC,
                                  TRY_ADVANCE_METHOD_NAME,
                                  TRY_ADVANCE_METHOD_DESC,
                                  signature,
                                  exceptions),
                sourceName,
                targetName,
                STATE_FIELD_NAME,
                methodInfo,
                argumentTypes);
    }

    private MethodVisitor instrumentMethod(int access,
                                           String name,
                                           String desc,
                                           String signature,
                                           String[] exceptions,
                                           Info methodInfo, Type[] argumentTypes) {
        this.visitLambdaFields(desc, methodInfo.getLocalVariables().values());
        return new YieldVisitor(this,
                                super.visitMethod(access,
                                                  name,
                                                  insertTypeInDescriptor(desc, targetName),
                                                  signature,
                                                  exceptions),
                                sourceName,
                                targetName,
                                STATE_FIELD_NAME + name,
                                methodInfo,
                                argumentTypes);
    }

    private void visitLambdaFields(String methodDescriptor, Collection<LocalVariable> values) {
        Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
        values.stream()
              .skip(argumentTypes.length)
              .forEach(this::localVariableToField);
    }

    private void localVariableToField(LocalVariable v) {
        if (!v.getName().equals("yield")) {
            this.visitField(ACC_PRIVATE, v.getName(), v.getDesc(), v.getSignature(), null);
        }
    }

    private boolean shouldInstrument(String name) {
        return this.ramifications.contains(name);
    }


}
