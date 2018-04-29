package jayield.advancer.generator.visitor.info.extractor.yield.count;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static jayield.advancer.generator.InstrumentationUtils.isYield;

public class YieldCountVisitor extends MethodVisitor implements Opcodes {

    private final Set<Label> visitedLabels;
    private final Consumer<Integer> setStates;
    private Set<Label> visitedLabelsSnapshot;
    private Set<Label> labelsJumpedTo;
    private Integer yieldCount;

    public YieldCountVisitor(MethodVisitor mv, Consumer<Integer> statesSetter) {
        super(ASM6, mv);
        setStates = statesSetter;
        visitedLabels = new HashSet<>();
        visitedLabelsSnapshot = new HashSet<>();
        labelsJumpedTo = new HashSet<>();
        yieldCount = 0;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // handle cycles
        if (this.visitedLabelsSnapshot.contains(label) && yieldCount == 1) {
            setStates.accept(this.yieldCount);
        }
        //jump
        mv.visitJumpInsn(opcode, label);

        this.labelsJumpedTo.add(label);
    }

    @Override
    public void visitLabel(Label label) {
        this.visitedLabels.add(label);
        mv.visitLabel(label);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (isYield(opcode, owner, name, desc, itf)) {
            this.visitedLabelsSnapshot = new HashSet<>(this.visitedLabels);
            this.yieldCount++;
            if (this.yieldCount > 1 || !this.visitedLabelsSnapshot.containsAll(this.labelsJumpedTo)) {
                setStates.accept(this.yieldCount);
            }
        }
    }
}

