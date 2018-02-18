package jayield.lite.codegen.visitors.method;

import jayield.lite.Yield;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;

public class TryAdvanceStateMachineMethodVisitor extends ChangeOwnersMethodVisitor {

    private static final String YIELD_METHOD_NAME = "ret";
    private static final String YIELD_METHOD_DESCRIPTION = "(Ljava/lang/Object;)V";

    private final ClassVisitor cv;
    private final String stateFieldName;
    private final Map<Integer, LocalVariable> localVariables;
    private int state = 0;
    private Label nextLabel;


    public TryAdvanceStateMachineMethodVisitor(MethodVisitor methodVisitor,
                                               ClassVisitor cv,
                                               String originalName,
                                               String newName,
                                               String stateFieldName,
                                               Map<Integer, LocalVariable> localVariables) {
        super(methodVisitor, originalName, newName);
        this.cv = cv;
        this.stateFieldName = stateFieldName;
        this.localVariables = localVariables;
        System.out.println(localVariables);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
        if (!name.equals("yield")) {
            FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, name, desc, signature, null);
            fv.visitEnd();
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
        nextLabel = new Label();
        startState(nextLabel);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        LocalVariable localVariable = this.localVariables.get(var);
        super.visitIincInsn(var, increment);
        super.visitVarInsn(ILOAD, var);
        super.visitFieldInsn(PUTSTATIC, newOwner, localVariable.getName(), localVariable.getDesc());
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (isLoadOpcode(opcode) && var > 0) {
            LocalVariable localVariable = this.localVariables.get(var);
            super.visitFieldInsn(GETSTATIC, newOwner, localVariable.getName(), localVariable.getDesc());
            super.visitVarInsn(opcode + 33, var);
        } else if (isStoreOpcode(opcode) && var > 0) {
            LocalVariable localVariable = this.localVariables.get(var);
            super.visitFieldInsn(PUTSTATIC, newOwner, localVariable.getName(), localVariable.getDesc());
            super.visitFieldInsn(GETSTATIC, newOwner, localVariable.getName(), localVariable.getDesc());
        }
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (isYield(opcode, owner, name, desc, itf)) {
            finishState();
            nextLabel = new Label();
            startState(nextLabel);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            // return false
            finishState();
            super.visitInsn(ICONST_0);
            super.visitInsn(IRETURN);
        }
        super.visitInsn(opcode);
    }

    private void finishState() {
        // Increment current state
        super.visitFieldInsn(GETSTATIC, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
        super.visitInsn(ICONST_0);
        super.visitInsn(DUP2);
        super.visitInsn(IALOAD);
        super.visitInsn(ICONST_1);
        super.visitInsn(IADD);
        super.visitInsn(IASTORE);

        // Return true, element was found
        super.visitInsn(ICONST_1);
        super.visitInsn(IRETURN);

        // set State End Label
        super.visitLabel(nextLabel);
    }

    private void startState(Label nextStateLabel) {
        super.visitLdcInsn(state);
        state++;
        // Load state field and current state
        super.visitFieldInsn(GETSTATIC, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
        super.visitInsn(ICONST_0);
        super.visitInsn(IALOAD);

        // if current state does not match this state, jump to else
        super.visitInsn(ISUB);
        super.visitJumpInsn(IFNE, nextStateLabel);
    }

    private boolean isYield(int opcode, String owner, String name, String desc, boolean isInterface) {
        return opcode == INVOKEINTERFACE &&
                owner.equals(classNameToPath(Yield.class)) &&
                name.equals(YIELD_METHOD_NAME) &&
                desc.equals(YIELD_METHOD_DESCRIPTION) &&
                isInterface;
    }

    private boolean isLoadOpcode(int opcode) {
        switch (opcode) {
            case ALOAD:
            case FLOAD:
            case LLOAD:
            case ILOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case DALOAD:
            case DLOAD:
            case FALOAD:
            case IALOAD:
            case LALOAD:
            case SALOAD:
                return true;
            default:
                return false;
        }
    }

    private boolean isStoreOpcode(int opcode) {
        switch (opcode) {
            case ASTORE:
            case FSTORE:
            case LSTORE:
            case ISTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case DASTORE:
            case DSTORE:
            case FASTORE:
            case IASTORE:
            case LASTORE:
            case SASTORE:
                return true;
            default:
                return false;
        }
    }
}
