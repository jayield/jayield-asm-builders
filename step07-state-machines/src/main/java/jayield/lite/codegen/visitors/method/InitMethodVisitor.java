package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.MethodVisitor;

import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;

public class InitMethodVisitor extends ChangeOwnersMethodVisitor {

    private final String stateFieldName;

    public InitMethodVisitor(MethodVisitor mv, String originalOwner, String newOwner, String stateFieldName) {
        super(mv, originalOwner, newOwner);
        this.stateFieldName = stateFieldName;
    }

    @Override
    public void visitInsn(int opcode) {
        if(opcode == RETURN){
            this.initCurrentStateArray();
        }
        super.visitInsn(opcode);
    }

    private void initCurrentStateArray() {
        super.visitVarInsn(ALOAD, 0);
        super.visitInsn(ICONST_1);
        super.visitIntInsn(NEWARRAY, T_INT);
        super.visitInsn(DUP);
        super.visitInsn(ICONST_0);
        super.visitInsn(ICONST_0);
        super.visitInsn(IASTORE);
        super.visitFieldInsn(PUTSTATIC, newOwner, stateFieldName, INT_ARRAY_DESCRIPTION);
    }
}
