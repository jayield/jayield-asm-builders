package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.*;

import static jayield.lite.codegen.GeneratorUtils.isLoadOpcode;
import static jayield.lite.codegen.GeneratorUtils.isStoreOpcode;

public class YieldStateMachineMethodVisitor extends ChangeOwnersMethodVisitor {

    private final ClassVisitor klass;
    private final String stateFieldName;
    private final String classTypeName;
    private final Map<Integer, LocalVariable> localVariables;
    private final int itemIndex;
    private final Set<Label> visitedLabels;
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
                                          Map<Integer, LocalVariable> localVariables,
                                          Type[] argumentTypes) {
        super(methodVisitor, originalName, newName);
        this.klass = klass;
        this.stateFieldName = stateFieldName;
        this.localVariables = localVariables;
        this.labelStack = new Stack<>();
        this.classTypeName = String.format("L%s;", newName);
        this.itemIndex = argumentTypes.length - 1;
        this.visitedLabels = new HashSet<>();
    }


    @Override
    public void visitVarInsn(int opcode, int var) {
        int actualVar = getActualVar(var);
        if (isLoadOpcode(opcode) && isLocalVariable(var)) {
            LocalVariable localVariable = this.localVariables.get(var);
            super.visitVarInsn(ALOAD, getStateVar());
            super.visitFieldInsn(GETFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
            super.visitVarInsn(opcode + 33, actualVar);
            super.visitVarInsn(opcode, actualVar);
        } else if (isStoreOpcode(opcode) && isLocalVariable(var)) {
            LocalVariable localVariable = this.localVariables.get(var);
            super.visitVarInsn(opcode, actualVar);
            super.visitVarInsn(ALOAD, getStateVar());
            super.visitVarInsn(opcode - 33, actualVar);
            super.visitFieldInsn(PUTFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
            finishCycle();
        } else {
            super.visitVarInsn(opcode, actualVar);
        }
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitLdcInsn(increment);
        this.visitVarInsn(ILOAD, var);
        super.visitInsn(IADD);
        this.visitVarInsn(ISTORE, var);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, getActualVar(index));
    }

    @Override
    public void visitCode() {
//        printCurrentState();
        nextLabel = new Label();
        startLabel = nextLabel;
        startState(nextLabel);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        String name = label.toString();
        String desc = "Z";

        this.klass.visitField(ACC_PRIVATE, name, desc, null, null).visitEnd();

        // handle cycles
        if(this.visitedLabels.contains(label)){
            this.decrementState();
        }

        //jump
        super.visitJumpInsn(opcode, label);

        //handle ifs
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitInsn(ICONST_1);
        super.visitFieldInsn(PUTFIELD, newOwner, name, desc);
        this.labelStack.push(label);

    }

    @Override
    public void visitLabel(Label label) {
        this.visitedLabels.add(label);
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

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable("this", classTypeName, null, startLabel, endLabel, getStateVar());
        super.visitMaxs(maxStack, maxLocals);
    }

    private int getActualVar(int var) {
        if(var == itemIndex) {
            return itemIndex + 1;
        } else if (var > itemIndex) {
            return var + 1;
        } else {
            return var;
        }
    }

    private boolean isLocalVariable(int var) {
        return var > itemIndex;
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

    private void finishState() {
        super.visitInsn(RETURN);
        super.visitLabel(nextLabel);
    }

    private void incrementState() {
        changeState(IADD, ICONST_1);
    }

    private void decrementState() {
        changeState(ISUB, ICONST_2);
    }

    private void changeState(int op, int value) {
        super.visitVarInsn(ALOAD, getStateVar());
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, "[I");
        super.visitInsn(ICONST_0);
        super.visitInsn(DUP2);
        super.visitInsn(IALOAD);
        super.visitInsn(value);
        super.visitInsn(op);
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

    private int getStateVar() {
        return itemIndex;
    }
}
