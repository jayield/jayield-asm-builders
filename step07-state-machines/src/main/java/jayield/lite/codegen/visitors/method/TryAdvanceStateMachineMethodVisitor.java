package jayield.lite.codegen.visitors.method;

import jayield.lite.Yield;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;

public class TryAdvanceStateMachineMethodVisitor extends ChangeOwnersMethodVisitor {

    private static final String YIELD_METHOD_NAME = "ret";
    private static final String YIELD_METHOD_DESCRIPTION = "(Ljava/lang/Object;)V";

    private final ClassVisitor cv;
    private final String stateFieldName;
    private int state = 0;
    private Label nextLabel;


    public TryAdvanceStateMachineMethodVisitor(MethodVisitor methodVisitor,
                                               ClassVisitor cv,
                                               String originalName,
                                               String newName,
                                               String stateFieldName) {
        super(methodVisitor, originalName, newName);
        this.cv = cv;
        this.stateFieldName = stateFieldName;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index){
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
        super.visitIincInsn(var, increment);
        super.visitVarInsn(ILOAD, var);
        super.visitFieldInsn(PUTSTATIC, newOwner, "n", "I");
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if(var == 1 && ((opcode & ILOAD) != 0)) {
            super.visitFieldInsn(GETSTATIC, newOwner, "n", "I");
            super.visitVarInsn(ISTORE, 1);
            super.visitVarInsn(opcode, var);
        } else if(var == 1 && ((opcode & ISTORE) != 0)) {
            super.visitFieldInsn(PUTSTATIC, newOwner, "n", "I");
            super.visitFieldInsn(GETSTATIC, newOwner, "n", "I");
            super.visitVarInsn(ISTORE, 1);
        } else {
            super.visitVarInsn(opcode, var);
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
}
