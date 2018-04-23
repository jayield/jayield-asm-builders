package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Iterator;
import java.util.Map;

import static jayield.lite.codegen.GeneratorUtils.isLoadOpcode;
import static jayield.lite.codegen.GeneratorUtils.isStoreOpcode;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;
import static jayield.lite.codegen.visitors.method.ConstructorVisitor.STATE_FIELD_NAME;
import static jayield.lite.codegen.visitors.method.ConstructorVisitor.YIELD_VARIABLE_NAME;

public class StateMachineMethodVisitor extends TraverseMethodVisitor {

    private final ClassVisitor cv;
    private final String stateFieldName;
    private int state = 0;
    private Label nextLabel;



    public StateMachineMethodVisitor(MethodVisitor methodVisitor,
                                     ClassVisitor cv,
                                     String originalName,
                                     String newName,
                                     String stateFieldName,
                                     Map<Integer, LocalVariable> localVariables) {
        super(methodVisitor, originalName, newName, localVariables);
        this.cv = cv;
        this.stateFieldName = stateFieldName;

        Iterator<LocalVariable> iterator = localVariables.values().iterator();
        while (iterator.hasNext()) {
            LocalVariable current = iterator.next();
            if (current.getName().equals(YIELD_VARIABLE_NAME)) {
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
        setUp();
        startState(nextLabel);
    }

    private void setUp() {
        // reset boolbox
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
        super.visitMethodInsn(INVOKEVIRTUAL, "jayield/lite/boxes/BoolBox", "reset", "()V", false);

        // instantiate wrapper
        super.visitTypeInsn(NEW, "jayield/lite/codegen/wrappers/YieldWrapper");
        super.visitInsn(DUP);
        super.visitVarInsn(ALOAD, getVarMapping(yieldIndex));
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
        super.visitMethodInsn(INVOKESPECIAL, "jayield/lite/codegen/wrappers/YieldWrapper", "<init>", "(Ljayield/lite/Yield;Ljayield/lite/boxes/BoolBox;)V", false);
        super.visitVarInsn(ASTORE, getWrapperIndex());
        /*super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, STATE_FIELD_NAME, "[I");
        super.visitInsn(ICONST_0);
        super.visitInsn(IALOAD);
        super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);*/


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
            if (var == yieldIndex) {
                super.visitVarInsn(opcode, getWrapperIndex());
            } else {
                super.visitVarInsn(opcode, actualVar);
            }

        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (isYield(opcode, owner, name, desc, itf)) {
            finishState(false);
            nextLabel = new Label();
            startState(nextLabel);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            finishState(true);
            this.endLabel = this.nextLabel;
            super.visitLocalVariable("this", "L" + newOwner + ";", null, startLabel, endLabel, 0);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, startLabel, endLabel, getVarMapping(index));
    }

    private void finishState(boolean last) {
        // Increment current state
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
        super.visitInsn(ICONST_0);
        if(!last){
            super.visitInsn(DUP2);
            super.visitInsn(IALOAD);
            super.visitInsn(ICONST_1);
            super.visitInsn(IADD);
            super.visitInsn(IASTORE);
        } else {
            super.visitInsn(DUP);
            super.visitInsn(IASTORE);
        }


        // Return element was found
        returnElementFound();

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

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable("wrapper", "Ljayield/lite/Yield;", "Ljayield/lite/Yield<TT;>;", startLabel, endLabel, getWrapperIndex());
        super.visitMaxs(maxStack + 1, maxLocals + 1);
    }
}
