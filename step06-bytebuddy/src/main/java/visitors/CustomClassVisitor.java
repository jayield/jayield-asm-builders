package visitors;

import jayield.lite.Advancer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Optional;

public class CustomClassVisitor extends ClassVisitor implements Opcodes {
    private final String targetName;
    private final String originalName;
    private final String[] interfaces;


    public CustomClassVisitor(ClassVisitor cv, String targetName, String originalName) {
        super(ASM6, cv);
        this.originalName = originalName;
        this.cv = cv;
        this.targetName = targetName;
        interfaces = new String[]{Advancer.class.getCanonicalName().replace('.', '/')};

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
        if (name.equals("traverse")) {
            return new CustomMethodVisitor(super.visitMethod(ACC_PUBLIC,
                    "tryAdvance",
                    desc.replace(";)V", ";)Z"),
                    signature,
                    exceptions),
                    originalName,
                    targetName);
        } else {
            return new OwnerMapperMethodVisitor(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    originalName,
                    targetName);
        }
    }

}

