package visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CustomMethodVisitor extends MethodVisitor implements Opcodes{
    private final MethodVisitor mv;

    public CustomMethodVisitor(MethodVisitor mv) {
        super(ASM6, mv);
        this.mv = mv;
    }

    @Override
    public void visitCode() {
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        super.visitCode();
    }
}
