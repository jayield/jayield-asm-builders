package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;
import java.util.Stack;

public class YieldStateMachineMethodVisitor extends ChangeOwnersMethodVisitor {

    private final String stateFieldName;
    private final int itemVar;
    private final Map<Integer, LocalVariable> localVariables;
    private int state = 0;
    private int cenas = 0;
    private Label nextLabel;
    private Label startLabel;
    private Label endLabel;
    private Stack<Label> labelStack;
    private Stack<Runnable> runnables;


    public YieldStateMachineMethodVisitor(MethodVisitor methodVisitor,
                                          String originalName,
                                          String newName,
                                          String stateFieldName,
                                          Map<Integer, LocalVariable> localVariables) {
        super(methodVisitor, originalName, newName);
        this.stateFieldName = stateFieldName;
        this.localVariables = localVariables;
        itemVar = localVariables.size() - 1;
        this.labelStack = new Stack<>();
        this.runnables = new Stack<>();
    }


    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, getActualVar(var));
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(getActualVar(var), increment);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, getActualVar(index));
    }

    private int getActualVar(int var) {
        if (var == itemVar) {
            return itemVar + 1;
        } else {
            return var;
        }
    }


    @Override
    public void visitCode() {
        super.visitCode();
        nextLabel = new Label();
        startLabel = nextLabel;
        startState(nextLabel);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label){
        super.visitJumpInsn(opcode, label);
        this.labelStack.push(label);
    }

    @Override
    public void visitLabel(Label label){
        super.visitLabel(label);
        if(!this.labelStack.empty() && this.labelStack.peek().equals(label)){
            this.labelStack.pop();
            while(!runnables.empty()) {
                runnables.pop().run();
            }
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (isRet(opcode, owner, name, desc, itf)) {
            Runnable finishCycle = () -> {
                finishState();
                nextLabel = new Label();
                startState(nextLabel);
            };
            if(this.labelStack.empty()){
                finishCycle.run();
            } else {
                runnables.push(finishCycle);
            }
        }
    }

    private boolean isRet(int opcode, String owner, String name, String desc, boolean itf) {
        return opcode == INVOKEINTERFACE &&
                "jayield/lite/Yield".equals(owner) &&
                "ret".equals(name) &&
                "(Ljava/lang/Object;)V".equals(desc) &&
                itf;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            super.visitVarInsn(ALOAD, getStateVar());
            super.visitInsn(ICONST_0);
            super.visitInsn(DUP);
            super.visitInsn(IASTORE);
            super.visitInsn(RETURN);
            super.visitLabel(nextLabel);
            this.endLabel = this.nextLabel;
        }
        super.visitInsn(opcode);
    }

    private void finishState() {
        // set State End Label

        // Increment current state
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitInsn(ICONST_0);
        super.visitInsn(DUP2);
        super.visitInsn(IALOAD);
        super.visitInsn(ICONST_1);
        super.visitInsn(IADD);
        super.visitInsn(IASTORE);

        super.visitInsn(RETURN);
        super.visitLabel(nextLabel);
    }

    private void startState(Label nextStateLabel) {
        super.visitLdcInsn(state);
        state++;
        // Load state field and current state
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitInsn(ICONST_0);
        super.visitInsn(IALOAD);

        // if current state does not match this state, jump to else
        super.visitInsn(ISUB);
        super.visitJumpInsn(IFNE, nextStateLabel);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable(stateFieldName, "[I", null, startLabel, endLabel, getStateVar());
        super.visitMaxs(maxStack + 1, maxLocals + 1);
    }

    private int getStateVar() {
        return this.localVariables.size() - 1;
    }
}
