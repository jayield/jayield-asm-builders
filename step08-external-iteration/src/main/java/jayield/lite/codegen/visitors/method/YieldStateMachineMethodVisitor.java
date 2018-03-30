package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;
import java.util.Stack;

public class YieldStateMachineMethodVisitor extends ChangeOwnersMethodVisitor {

    private final ClassVisitor klass;
    private final String stateFieldName;
    private final int itemVar;
    private final Map<Integer, LocalVariable> localVariables;
    private int state = 0;
    private Label nextLabel;
    private Label startLabel;
    private Label endLabel;
    private Stack<Label> labelStack;
    private boolean notOver = false;


    public YieldStateMachineMethodVisitor(ClassVisitor klass,
                                          MethodVisitor methodVisitor,
                                          String originalName,
                                          String newName,
                                          String stateFieldName,
                                          Map<Integer, LocalVariable> localVariables) {
        super(methodVisitor, originalName, newName);
        this.klass = klass;
        this.stateFieldName = stateFieldName;
        this.localVariables = localVariables;
        itemVar = localVariables.size() - 1;
        this.labelStack = new Stack<>();
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
//        printCurrentState();
        nextLabel = new Label();
        startLabel = nextLabel;
        startState(nextLabel);
    }

    private void printCurrentState() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("state: %d");
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, "[I");
        mv.visitInsn(ICONST_0);
        super.visitInsn(IALOAD);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitInsn(AASTORE);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        String name = label.toString();
        String desc = "Z";
        this.klass.visitField(ACC_PRIVATE, name, desc, null, null).visitEnd();
        super.visitJumpInsn(opcode, label);
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitInsn(ICONST_1);
        super.visitFieldInsn(PUTFIELD, newOwner, name, desc);
        this.labelStack.push(label);

    }

    @Override
    public void visitLabel(Label label) {
        if (!this.labelStack.empty() && this.labelStack.peek().equals(label)) {
            this.labelStack.pop();
            Label label1 = new Label();
            super.visitJumpInsn(GOTO, label1);
            super.visitLabel(label);
            super.visitVarInsn(ALOAD, getStateVar());
            super.visitInsn(ICONST_0);
            super.visitFieldInsn(PUTFIELD, newOwner, label.toString(), "Z");
            super.visitInsn(RETURN);
            super.visitLabel(label1);
        } else {
            super.visitLabel(label);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (isRet(opcode, owner, name, desc, itf)) {
            if (this.labelStack.empty()) {
                finishCycle();
            } else {
                super.visitInsn(RETURN);
                finishCycle();
                super.visitVarInsn(ALOAD, getStateVar());
                super.visitFieldInsn(GETFIELD, newOwner, this.labelStack.peek().toString(), "Z");
                super.visitJumpInsn(IFEQ, nextLabel);
                this.notOver = true;
            }
        }
    }

    private void finishCycle() {
        finishState();
        nextLabel = new Label();
        startState(nextLabel);
        this.notOver = false;
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
            if (this.notOver) {
                finishCycle();
            }
            super.visitVarInsn(ALOAD, getStateVar());
            super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, "[I");
            super.visitInsn(ICONST_0);
            super.visitInsn(DUP);
            super.visitInsn(IASTORE);
            super.visitVarInsn(ALOAD, getStateVar());
            super.visitInsn(ICONST_0);
            super.visitFieldInsn(PUTFIELD, newOwner, "validValue", "Z");
            super.visitInsn(RETURN);
            super.visitLabel(nextLabel);
            this.endLabel = this.nextLabel;
        }
        super.visitInsn(opcode);
    }

    private void finishState() {
        // set State End Label

        // Increment current state
//        incrementState();

        super.visitInsn(RETURN);
        super.visitLabel(nextLabel);
    }

    private void incrementState() {
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, "[I");
        super.visitInsn(ICONST_0);
        super.visitInsn(DUP2);
        super.visitInsn(IALOAD);
        super.visitInsn(ICONST_1);
        super.visitInsn(IADD);
        super.visitInsn(IASTORE);
    }

    private void startState(Label nextStateLabel) {
        super.visitLdcInsn(state);
        state++;
        // Load state field and current state
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, "[I");
        super.visitInsn(ICONST_0);
        super.visitInsn(IALOAD);

        // if current state does not match this state, jump to else
        super.visitInsn(ISUB);
        super.visitJumpInsn(IFNE, nextStateLabel);

        incrementState();
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
