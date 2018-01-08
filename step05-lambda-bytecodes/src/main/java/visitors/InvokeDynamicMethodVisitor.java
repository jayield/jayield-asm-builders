package visitors;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class InvokeDynamicMethodVisitor extends MethodVisitor implements Opcodes {

    private final String originalOwner;
    private final String newOwner;

    public InvokeDynamicMethodVisitor(MethodVisitor mv, String originalOwner, String newOwner) {
        super(ASM6, mv);
        this.originalOwner = originalOwner;
        this.newOwner = newOwner;
    }

    /**
     * Visits an invokedynamic instruction.
     *
     * @param name    the method's name.
     * @param desc    the method's descriptor (see {@link Type Type}).
     * @param bsm     the bootstrap method.
     * @param bsmArgs the bootstrap method constant arguments. Each argument must be
     *                an {@link Integer}, {@link Float}, {@link Long},
     *                {@link Double}, {@link String}, {@link Type} or {@link Handle}
     *                value. This method is allowed to modify the content of the
     *                array so a caller should expect that this array may change.
     */
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


    private Handle instrumentHandle(Handle source){
        if(!source.getOwner().equals(this.originalOwner)){
            return source;
        } else {
            return new Handle(source.getTag(), this.newOwner, source.getName(), source.getDesc(), source.isInterface());
        }
    }
}
