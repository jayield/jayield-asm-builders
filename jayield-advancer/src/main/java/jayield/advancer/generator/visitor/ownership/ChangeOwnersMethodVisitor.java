package jayield.advancer.generator.visitor.ownership;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * This class's purpose is to change the handle's owner from the source class to the generated one
 */
public class ChangeOwnersMethodVisitor extends MethodVisitor implements Opcodes {

    private final String originalOwner;
    protected final String newOwner;

    public ChangeOwnersMethodVisitor(MethodVisitor mv,
                                     String originalOwner,
                                     String newOwner) {
        super(ASM6, mv);
        this.originalOwner = originalOwner;
        this.newOwner = newOwner;
    }

    @Override
    public void visitLabel(Label label){
//        System.out.println("visiting " + label);
        super.visitLabel(label);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        bsm = instrumentHandle(bsm);
        for (int i = 0; i < bsmArgs.length; i++) {
            if (bsmArgs[i] instanceof Handle) {
                bsmArgs[i] = instrumentHandle((Handle) bsmArgs[i]);
            }
        }
        if (mv != null) {
            mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        String finalOwner = owner;
        if (finalOwner.equals(originalOwner)) {
            finalOwner = newOwner;
        }
        super.visitFieldInsn(opcode, finalOwner, name, desc);
    }


    private Handle instrumentHandle(Handle source) {
        if (!source.getOwner().equals(this.originalOwner)) {
            return source;
        } else {
            return new Handle(source.getTag(), this.newOwner, source.getName(), source.getDesc(), source.isInterface());
        }
    }
}
