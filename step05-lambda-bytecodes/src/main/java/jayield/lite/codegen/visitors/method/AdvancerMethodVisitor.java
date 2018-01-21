package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AdvancerMethodVisitor extends InvokeDynamicMethodVisitor implements Opcodes {

    private static final String TRAVERSE_METHOD_NAME = "traverse";
    private static final String TRY_ADVANCE_METHOD_NAME = "tryAdvance";
    private static final String TRY_ADVANCE_METHOD_SIGNATURE = "(Ljayield/lite/Yield;)Z";

    public AdvancerMethodVisitor(MethodVisitor mv, String sourceName, String finalName) {
        super(mv, sourceName, finalName);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (name.equals(TRAVERSE_METHOD_NAME)) {
            super.visitMethodInsn(opcode, owner, TRY_ADVANCE_METHOD_NAME, TRY_ADVANCE_METHOD_SIGNATURE, itf);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) { // need to change the return type in order to return the boolean value
            super.visitInsn(IRETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

}
