package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.visitors.method.LocalVariable;
import jayield.lite.codegen.visitors.method.LocalVariableExtractorMethodVisitor;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.util.Map;

public class LocalVariableExtractorClassVisitor extends ClassVisitor implements Opcodes {

    private final String finalName;
    private LocalVariableExtractorMethodVisitor mv;

    public LocalVariableExtractorClassVisitor(ClassVisitor cv, String finalName) {
        super(ASM6, cv);
        this.finalName = finalName;
    }

    public Map<Integer, LocalVariable> getLocalVariables() {
        return this.mv.getLocalVariables();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, finalName + "LocalVariableExtractor", signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!name.equals(finalName)) {
            return null;
        }
        this.mv = new LocalVariableExtractorMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
        return this.mv;
    }

    public static  Map<Integer, LocalVariable> getLocalVariables(SerializedLambda traversable) {
        try {
            ClassReader reader = new ClassReader(traversable.getImplClass());
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            LocalVariableExtractorClassVisitor visitor = new LocalVariableExtractorClassVisitor(writer, traversable.getImplMethodName());
            reader.accept(visitor, 0);

            return visitor.getLocalVariables();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
