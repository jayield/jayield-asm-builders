package jayield.lite.codegen.visitors.method;

import jayield.lite.codegen.visitors.clazz.RamificationCalculatorVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RamificationsMethodVisitor extends MethodVisitor implements Opcodes {


    private final RamificationCalculatorVisitor classVisitor;
    private final String name;
    private final String owner;

    public RamificationsMethodVisitor(MethodVisitor methodVisitor,
                                      RamificationCalculatorVisitor classVisitor,
                                      String name,
                                      String owner) {
        super(ASM6, methodVisitor);
        this.classVisitor = classVisitor;
        this.name = name;
        this.owner = owner;
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        if (this.owner.equals(bsm.getOwner())) {
            this.classVisitor.addRamification(this.name, name);
        }
        addNamesFromBsm(bsmArgs);
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    private void addNamesFromBsm(Object[] bsmArgs) {
        for (Object arg : bsmArgs) {
            if (arg instanceof Handle) {
                 Handle bsmArg = (Handle) arg;
                 if(this.owner.equals(bsmArg.getOwner())){
                     this.classVisitor.addRamification(this.name, bsmArg.getName());
                 }
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (this.owner.equals(owner)) {
            this.classVisitor.addRamification(this.name, name);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
