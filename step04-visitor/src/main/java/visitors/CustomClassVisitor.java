package visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;
import java.util.function.Function;

public class CustomClassVisitor extends ClassVisitor implements Opcodes {
    private final ClassVisitor cv;
    private final String targetName;
    private String[] interfaces;
    private Function<MethodVisitor, MethodVisitor> toMethodIntrumenter;
    private Function<ClassVisitor, MethodVisitor> toMethodDeclaration;

    CustomClassVisitor(ClassVisitor cv, String targetName) {
        super(ASM6, cv);
        this.cv = cv;
        this.targetName = targetName;
        interfaces = null;
        toMethodIntrumenter = CustomMethodVisitor::new;
        toMethodDeclaration = null;
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
        if(!name.equals("toNumber"))
            return super.visitMethod(access, name, desc, signature, exceptions);
        MethodVisitor mv = Optional.ofNullable(this.toMethodDeclaration)
                .orElse(cv -> super.visitMethod(access, name, desc, signature, exceptions))
                .apply(cv);
        return toMethodIntrumenter.apply(mv);
    }

    void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

    void setToMethodIntrumenter(Function<MethodVisitor, MethodVisitor> toMethodIntrumenter) {
        this.toMethodIntrumenter = toMethodIntrumenter;
    }

    void setToMethodDeclaration(Function<ClassVisitor, MethodVisitor> toMethodDeclaration) {
        this.toMethodDeclaration = toMethodDeclaration;
    }
}

