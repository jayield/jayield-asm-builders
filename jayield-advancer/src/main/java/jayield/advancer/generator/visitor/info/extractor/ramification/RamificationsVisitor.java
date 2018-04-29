package jayield.advancer.generator.visitor.info.extractor.ramification;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.Consumer;

public class RamificationsVisitor extends MethodVisitor implements Opcodes {


    private final Consumer<String> addRamification;
    private final String owner;

    public RamificationsVisitor(MethodVisitor mv, Consumer<String> addRamification, String owner) {
        super(ASM6, mv);
        this.addRamification = addRamification;
        this.owner = owner;
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        if (this.owner.equals(bsm.getOwner())) {
            addRamification.accept(name);
        }
        addNamesFromBsm(bsmArgs);
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    private void addNamesFromBsm(Object[] bsmArgs) {
        for (Object arg : bsmArgs) {
            if (arg instanceof Handle) {
                Handle bsmArg = (Handle) arg;
                if (this.owner.equals(bsmArg.getOwner())) {
                    addRamification.accept(bsmArg.getName());
                }
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (this.owner.equals(owner)) {
            addRamification.accept(name);
        }
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
