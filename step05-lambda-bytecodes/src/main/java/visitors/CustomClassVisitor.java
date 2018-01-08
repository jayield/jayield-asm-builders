package visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class CustomClassVisitor extends ClassVisitor implements Opcodes {
    private final String targetName;
    private final String originalName;


    public CustomClassVisitor(ClassVisitor cv, String targetName, String originalName) {
        super(ASM6, cv);
        this.originalName = originalName;
        this.cv = cv;
        this.targetName = targetName;

    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version,
                access,
                targetName,
                signature,
                superName,
                interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(!name.equals(targetName))
            return new InvokeDynamicMethodVisitor(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    originalName,
                    targetName);
        return new CustomMethodVisitor(super.visitMethod(ACC_PUBLIC + ACC_STATIC,
                name,
                desc.replace(";)V",";)Z"),
                signature,
                exceptions),
                originalName,
                targetName);
    }

}

