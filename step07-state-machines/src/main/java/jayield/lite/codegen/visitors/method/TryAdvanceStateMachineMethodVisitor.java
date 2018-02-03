package jayield.lite.codegen.visitors.method;

import jayield.lite.Yield;
import jayield.lite.codegen.GeneratorUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;

public class TryAdvanceStateMachineMethodVisitor extends ChangeOwnersMethodVisitor {

    private static final String YIELD_METHOD_NAME = "ret";
    private static final String YIELD_METHOD_DESCRIPTION = "(Ljava/lang/Object;)V";
    private final String stateFieldName;
    private int state = 0;


    public TryAdvanceStateMachineMethodVisitor(MethodVisitor methodVisitor, String originalName, String newName, String stateFieldName) {
        super(methodVisitor, originalName, newName);
        this.stateFieldName = stateFieldName;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (isYield(opcode, owner, name, desc, itf)) {
            createState(opcode, owner, name, desc, itf);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            // return false
            super.visitInsn(ICONST_0);
            super.visitInsn(IRETURN);
        }
        super.visitInsn(opcode);
    }

    private void createState(int opcode, String owner, String name, String desc, boolean itf) {
        Label elseLabel = new Label();

        // Push State value to compare with
        super.visitLdcInsn(state);
        state++;
        // Load state field and current state
        super.visitFieldInsn(GETSTATIC, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
        super.visitInsn(ICONST_0);
        super.visitInsn(IALOAD);

        // if current state does not match this state, jump to else
        super.visitInsn(ISUB);
        super.visitJumpInsn(IFNE, elseLabel);

        //:::: IF BLOCK CODE ::::

        // Call Yield Return
        super.visitMethodInsn(opcode, owner, name, desc, itf);
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

        //:::: ELSE BLOCK CODE ::::
        super.visitLabel(elseLabel);

        //POP loaded values from stack
        super.visitInsn(POP);
        super.visitInsn(POP);
    }

    private boolean isYield(int opcode, String owner, String name, String desc, boolean isInterface) {
        return opcode == INVOKEINTERFACE &&
                owner.equals(classNameToPath(Yield.class)) &&
                name.equals(YIELD_METHOD_NAME) &&
                desc.equals(YIELD_METHOD_DESCRIPTION) &&
                isInterface;
    }
}
