package visitors;

import jayield.lite.YieldWrapper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CustomMethodVisitor extends OwnerMapperMethodVisitor implements Opcodes{

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
            super.visitVarInsn(ALOAD,1);
            super.visitMethodInsn(INVOKEVIRTUAL, YieldWrapper.class.getName().replace('.', '/'), "hasElement", "()Z", false);
            super.visitInsn(IRETURN);
        }else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if(opcode == ALOAD && var == 1){
            super.visitTypeInsn(NEW, YieldWrapper.class.getName().replace('.', '/'));
            super.visitInsn(DUP);
            super.visitVarInsn(opcode,var);
            super.visitMethodInsn(INVOKESPECIAL, YieldWrapper.class.getName().replace('.', '/'),"<init>", "(Ljayield/lite/Yield;)V", false);
            super.visitVarInsn(ASTORE,var);
            super.visitVarInsn(opcode,var);
        } else {
            super.visitVarInsn(opcode,var);
        }
    }

}
