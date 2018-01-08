package visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CustomMethodVisitor extends InvokeDynamicMethodVisitor implements Opcodes{

    public CustomMethodVisitor(MethodVisitor mv, String originalName, String newName) {
        super(mv, originalName, newName);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
                                String desc, boolean itf) {
        if(name.equals("traverse")) {
            super.visitMethodInsn(opcode, owner, "tryAdvance", "(Ljayield/lite/Yield;)Z", itf);
        }
        else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if(opcode == RETURN){
            super.visitInsn(IRETURN);
        }else {
            super.visitInsn(opcode);
        }
    }

}
