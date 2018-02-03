package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * This class's purpose is to change the handle's owner from the source class to the generated one
 */
public class InvokeDynamicMethodVisitor extends MethodVisitor implements Opcodes {

    private final String originalOwner;
    private final String newOwner;

    public InvokeDynamicMethodVisitor(MethodVisitor mv, String originalOwner, String newOwner) {
        super(ASM6, mv);
        this.originalOwner = originalOwner;
        this.newOwner = newOwner;
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


    private Handle instrumentHandle(Handle source) {
        if (!source.getOwner().equals(this.originalOwner)) {
            return source;
        } else {
            return new Handle(source.getTag(), this.newOwner, source.getName(), source.getDesc(), source.isInterface());
        }
    }
}