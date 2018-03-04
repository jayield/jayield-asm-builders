package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.LambdaToAdvancerMethodGenerator;
import jayield.lite.codegen.visitors.method.ChangeOwnersMethodVisitor;
import jayield.lite.codegen.visitors.method.ConstructorVisitor;
import jayield.lite.codegen.visitors.method.LocalVariable;
import jayield.lite.codegen.visitors.method.StateMachineMethodVisitor;
import jayield.lite.codegen.wrappers.LambdaToAdvancer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.SerializedLambda;
import java.util.Map;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;

public class YieldStateMachineVisitor extends ClassVisitor implements Opcodes {

    private static final String STATE_FIELD_NAME = "$currentState";
    private static final String INIT_METHOD_NAME = "<init>";
    private final String finalName;
    private final String sourceName;
    private final SerializedLambda source;
    private final Map<Integer, LocalVariable> localVariables;
    private final String[] interfaces;
    private final LambdaToAdvancerMethodGenerator advanverMethodGenerator;

    public YieldStateMachineVisitor(ClassVisitor cv, String finalName, String sourceName, SerializedLambda source, Map<Integer, LocalVariable> localVariables) {
        super(ASM6, cv);
        this.finalName = finalName;
        this.sourceName = sourceName;
        this.source = source;
        this.localVariables = localVariables;
        this.interfaces = new String[]{classNameToPath(LambdaToAdvancer.class)};
        this.advanverMethodGenerator = new LambdaToAdvancerMethodGenerator(source, this, finalName);
    }

    // Change class Name
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, finalName, signature, superName, this.interfaces);
        FieldVisitor fv = super.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, STATE_FIELD_NAME, "[I", null, null);
        fv.visitEnd();
        advanverMethodGenerator.generateGetAdvancerMethod();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals(INIT_METHOD_NAME)) {
            return new ConstructorVisitor(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    sourceName,
                    finalName,
                    STATE_FIELD_NAME
            );
        } else if (!name.equals(finalName)) {
            return new ChangeOwnersMethodVisitor(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    sourceName,
                    finalName);
        } else {
            return new StateMachineMethodVisitor(super.visitMethod(ACC_PUBLIC + ACC_STATIC,
                    name,
                    desc.replace(";)V", ";)Z"),
                    signature,
                    exceptions),
                    this,
                    sourceName,
                    finalName,
                    STATE_FIELD_NAME,
                    this.localVariables);
        }
    }
}
