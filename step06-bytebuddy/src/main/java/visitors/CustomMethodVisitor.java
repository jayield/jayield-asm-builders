package visitors;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CustomMethodVisitor extends MethodVisitor implements Opcodes{

    public CustomMethodVisitor(MethodVisitor mv) {
        super(ASM6, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc, boolean itf) {
        if(name.equals("traverse")) {
            name = "tryAdvance";
//            super.visitVarInsn(ALOAD, 0);
//            super.visitVarInsn(ALOAD, 1);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
                           Object... bsmArgs) {
        if (name.equals("ret")) {
            super.visitVarInsn(ALOAD, 1);
        }
        super.visitInvokeDynamicInsn(name, desc ,bsm , bsmArgs);
    }

}
