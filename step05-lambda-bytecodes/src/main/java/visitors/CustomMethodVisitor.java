package visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CustomMethodVisitor extends MethodVisitor implements Opcodes{
    private final MethodVisitor mv;

    public CustomMethodVisitor(MethodVisitor mv) {
        super(ASM6, mv);
        this.mv = mv;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc, boolean itf) {
        if(name.equals("traverse")) {
            name = "tryAdvance";
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }


}
