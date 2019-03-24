package jayield.advancer.generator.visitor.info.extractor;

import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariableVisitor;
import jayield.advancer.generator.visitor.info.extractor.ramification.RamificationsVisitor;
import jayield.advancer.generator.visitor.info.extractor.yield.count.YieldCountVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class InfoMethodVisitor extends MethodVisitor implements Opcodes {

    private final LocalVariableVisitor localVariableVisitor;
    private final RamificationsVisitor ramificationsMethodVisitor;
    private final YieldCountVisitor yieldCountVisitor;

    InfoMethodVisitor(MethodVisitor mv, String owner, Info info) {
        super(ASM6, mv);
        List<String> ramifications = new ArrayList<>();
        localVariableVisitor = new LocalVariableVisitor(mv);
        ramificationsMethodVisitor = new RamificationsVisitor(mv, ramifications::add, owner);
        yieldCountVisitor = new YieldCountVisitor(mv, info::setStates);

        info.setLocalVariables(localVariableVisitor.getLocalVariables());
        info.setRamifications(ramifications);
    }



    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
        localVariableVisitor.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        yieldCountVisitor.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        yieldCountVisitor.visitLabel(label);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        yieldCountVisitor.visitMethodInsn(opcode, owner, name, desc, itf);
        ramificationsMethodVisitor.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        ramificationsMethodVisitor.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitMaxs(int stack, int local) {}

}
