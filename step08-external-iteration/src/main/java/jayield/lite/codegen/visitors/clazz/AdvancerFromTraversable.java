package jayield.lite.codegen.visitors.clazz;

import jayield.lite.Advancer;
import jayield.lite.codegen.LambdaToAdvancerMethodGenerator;
import jayield.lite.codegen.visitors.method.ChangeOwnersMethodVisitor;
import jayield.lite.codegen.visitors.method.ConstructorVisitor;
import jayield.lite.codegen.visitors.method.LocalVariable;
import jayield.lite.codegen.visitors.method.StateMachineMethodVisitor;
import jayield.lite.codegen.wrappers.AbstractAdvance;
import jayield.lite.codegen.wrappers.LambdaToAdvancer;
import org.objectweb.asm.*;

import java.lang.invoke.SerializedLambda;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;
import static jayield.lite.codegen.visitors.method.ConstructorVisitor.*;

public class AdvancerFromTraversable extends ClassVisitor implements Opcodes {

    public static final String TRY_ADVANCE_METHOD_NAME = "tryAdvance";
    public static final String TRY_ADVANCE_METHOD_SIGNATURE = "(Ljayield/lite/Yield;)Z";

    private final ClassWriter visitor;
    private final SerializedLambda traversable;
    private final String[] interfaces;
    private final String sourceName;
    private final String targetName;
    private final List<String> ramifications;
    private final Map<String, Map<Integer, LocalVariable>> localVariables;
    private final LambdaToAdvancerMethodGenerator advancerMethodGenerator;

    public AdvancerFromTraversable(ClassWriter visitor, SerializedLambda traversable) {
        super(ASM6, visitor);
        this.visitor = visitor;
        this.traversable = traversable;
        this.interfaces = new String[]{classNameToPath(LambdaToAdvancer.class), classNameToPath(Advancer.class)};
        this.sourceName = traversable.getCapturingClass();
        this.targetName = traversable.getImplMethodName();
        this.ramifications = RamificationCalculatorVisitor.getRamifications(traversable);
        this.localVariables = LocalVariableExtractorClassVisitor.getLocalVariables(traversable, ramifications);
        this.advancerMethodGenerator = new LambdaToAdvancerMethodGenerator(traversable, this, targetName);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, targetName, signature, classNameToPath(AbstractAdvance.class), this.interfaces);
        visitFields();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        this.advancerMethodGenerator.generateGetAdvancerMethod();
    }

    private void visitFields() {
        Iterator<LocalVariable> variableIterator = localVariables.get(targetName).values().iterator();
        LocalVariable variable;
        while (variableIterator.hasNext()) {
            variable = variableIterator.next();
            if (!variable.getName().equals(YIELD_VARIABLE_NAME)) {
                super.visitField(ACC_PRIVATE, variable.getName(), variable.getDesc(), variable.getSignature(), null).visitEnd();
            }
        }
        super.visitField(ACC_PRIVATE, STATE_FIELD_NAME, "[I", null, null).visitEnd();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(INIT_METHOD_NAME + targetName)) {
            return super.visitMethod(access, INIT_METHOD_NAME, desc, signature, exceptions);
        } else if ("getAdvancer".equals(name)) {
            return new ChangeOwnersMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
                    this.traversable.getCapturingClass(),
                    this.targetName);
        } else if (shouldInstrument(name)) {
            return new ChangeOwnersMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions),
                    this.traversable.getCapturingClass(),
                    this.targetName);
        } else if (this.targetName.equals(name)) {
            ConstructorVisitor.generateConstructor(this, localVariables.get(name).values(), desc, targetName);
            return new StateMachineMethodVisitor(
                    super.visitMethod(ACC_PUBLIC, TRY_ADVANCE_METHOD_NAME, TRY_ADVANCE_METHOD_SIGNATURE, signature, exceptions),
                    this,
                    sourceName,
                    targetName,
                    STATE_FIELD_NAME,
                    this.localVariables.get(name));
        }
        return null;
    }

    private boolean shouldInstrument(String name) {
        return this.ramifications.contains(name);
    }


}
