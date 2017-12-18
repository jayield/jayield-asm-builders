package visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class CustomClassVisitor extends ClassVisitor implements Opcodes {
    private final ClassVisitor cv;
    private final String targetName;
    private String[] interfaces;
    private MethodVisitor mv;


    public CustomClassVisitor(ClassVisitor cv, String targetName) {
        super(ASM6, cv);
        this.cv = cv;
        this.targetName = targetName;
        interfaces = new String[] {YieldWrapper.class.getCanonicalName().replace('.', '/')};

    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version,
                access,
                targetName,
                signature,
                superName,
                Optional.ofNullable(this.interfaces).orElse(interfaces));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(!name.equals(targetName))
            return super.visitMethod(access, name, desc, signature, exceptions);
        System.out.println("found it");
        return new CustomMethodVisitor(super.visitMethod(ACC_PUBLIC + ACC_INTERFACE + ACC_FINAL, "tryAdvanceWrapper", desc, signature, exceptions));
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

}

