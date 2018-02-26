package jayield.lite.codegen.visitors.method;

import jayield.lite.Yield;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Iterator;
import java.util.Map;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;
import static jayield.lite.codegen.visitors.method.ConstructorVisitor.YIELD_VARIABLE_NAME;

public class TryAdvanceStateMachineMethodVisitor extends AdvancerMethodVisitor {

    private static final String YIELD_METHOD_NAME = "ret";
    private static final String YIELD_METHOD_DESCRIPTION = "(Ljava/lang/Object;)V";

    private final ClassVisitor cv;
    private final String stateFieldName;
    private final Map<Integer, LocalVariable> localVariables;
    private int yieldIndex;
    private int state = 0;
    private Label nextLabel;

    private Label startLabel;
    private Label endLabel;


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

        Iterator<LocalVariable> iterator = localVariables.values().iterator();
        while(iterator.hasNext()){
            LocalVariable current = iterator.next();
            if(current.getName().equals(YIELD_VARIABLE_NAME)) {
                this.yieldIndex = current.getIndex();
                break;
            }
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
    public void visitIincInsn(int var, int increment) {
        LocalVariable localVariable = this.localVariables.get(var);
        super.visitIincInsn(getVarMapping(var), increment);
        super.visitVarInsn(ALOAD, 0);
        this.visitVarInsn(ILOAD, getVarMapping(var));
        super.visitFieldInsn(PUTFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        int actualVar = getVarMapping(var);
        if (isLoadOpcode(opcode) && var != yieldIndex) {
            LocalVariable localVariable = this.localVariables.get(var);
            super.visitVarInsn(ALOAD, 0);
            super.visitFieldInsn(GETFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
            super.visitVarInsn(opcode + 33, actualVar);
            super.visitVarInsn(opcode, actualVar);
        } else if (isStoreOpcode(opcode) && var != yieldIndex) {
            LocalVariable localVariable = this.localVariables.get(var);
            super.visitVarInsn(opcode, actualVar);
            super.visitVarInsn(ALOAD, 0);
            super.visitVarInsn(opcode - 33, actualVar);
            super.visitFieldInsn(PUTFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
        } else {
            super.visitVarInsn(opcode, actualVar);
        }
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
            this.endLabel = this.nextLabel;
            super.visitLocalVariable("this", "L" + newOwner+ ";", null, startLabel, endLabel, 0);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, startLabel, endLabel, getVarMapping(index));
    }

    private void finishState() {
        // Increment current state
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
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
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
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

    private int getVarMapping(int var) {
        if(var != yieldIndex){
            return yieldIndex + var + 1;
        } else {
            return 1;
        }

    }
}
