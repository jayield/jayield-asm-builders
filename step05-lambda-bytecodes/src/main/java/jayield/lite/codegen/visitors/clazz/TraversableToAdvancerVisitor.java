package jayield.lite.codegen.visitors.clazz;

import jayield.lite.codegen.visitors.method.AdvancerMethodVisitor;
import jayield.lite.codegen.visitors.method.InvokeDynamicMethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TraversableToAdvancerVisitor extends ClassVisitor implements Opcodes {

    private final String finalName;
    private final String sourceName;


    public TraversableToAdvancerVisitor(ClassVisitor cv, String finalName, String sourceName) {
        super(ASM6, cv);
        this.sourceName = sourceName;
        this.cv = cv;
        this.finalName = finalName;

    }

    // Change class Name
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, finalName, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!name.equals(finalName))
            return new InvokeDynamicMethodVisitor(
                    super.visitMethod(access, name, desc, signature, exceptions),
                    sourceName,
                    finalName);
        return new AdvancerMethodVisitor(super.visitMethod(ACC_PUBLIC + ACC_STATIC,
                name,
                desc.replace(";)V", ";)Z"),
                signature,
                exceptions),
                sourceName,
                finalName);
    }

}

