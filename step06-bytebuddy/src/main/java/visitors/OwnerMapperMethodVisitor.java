package visitors;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class OwnerMapperMethodVisitor extends MethodVisitor implements Opcodes {

    private final String originalOwner;
    private final String newOwner;

    public OwnerMapperMethodVisitor(MethodVisitor mv, String originalOwner, String newOwner) {
        super(ASM6, mv);
        this.originalOwner = originalOwner;
        this.newOwner = newOwner;
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        bsm = instrumentHandle(bsm);
        for(int i = 0; i < bsmArgs.length; i++){
            if(bsmArgs[i] instanceof Handle){
                bsmArgs[i] = instrumentHandle((Handle) bsmArgs[i]);
            }
        }
        if (mv != null) {
            mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode,getCurrentType(owner), name, desc);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode,getCurrentType(type));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode,getCurrentType(owner), name, desc, itf);
    }



    private Handle instrumentHandle(Handle source){
        if(!source.getOwner().equals(this.originalOwner)){
            return source;
        } else {
            return new Handle(source.getTag(), this.newOwner, source.getName(), source.getDesc(), source.isInterface());
        }
    }

    private String getCurrentType(String originalType){
        if(originalType.equals(this.originalOwner.substring(0, this.originalOwner.lastIndexOf('/'))))
            return this.newOwner;
        return originalType;
    }
}
