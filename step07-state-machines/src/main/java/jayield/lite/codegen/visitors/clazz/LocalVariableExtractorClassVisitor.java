package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.visitors.method.LocalVariable;
import jayield.lite.codegen.visitors.method.LocalVariableExtractorMethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
}
