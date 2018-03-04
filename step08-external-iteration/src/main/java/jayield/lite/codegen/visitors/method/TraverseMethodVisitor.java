package jayield.lite.codegen.visitors.method;

import jayield.lite.Yield;
import org.objectweb.asm.*;

import java.util.Map;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;

public class TraverseMethodVisitor extends ChangeOwnersMethodVisitor implements Opcodes {

    public static final String TRAVERSE_METHOD_NAME = "traverse";
    public static final String YIELD_METHOD_NAME = "ret";
    public static final String YIELD_METHOD_DESCRIPTION = "(Ljava/lang/Object;)V";

    protected final Map<Integer, LocalVariable> localVariables;
    protected int yieldIndex;
    protected Label startLabel;
    protected Label endLabel;

    public TraverseMethodVisitor(MethodVisitor mv, String sourceName, String finalName, Map<Integer, LocalVariable> localVariables) {
        super(mv, sourceName, finalName);
        this.localVariables = localVariables;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (name.equals(TRAVERSE_METHOD_NAME)) {
            traverseToIterator();
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    private void traverseToIterator() {
        Label loopStart = new Label();
        Label loopEnd = new Label();
        Label nextState = new Label();
        Label advance = new Label();
        super.visitInsn(POP);
        super.visitLabel(loopStart);
        super.visitLineNumber(27, loopStart);
        super.visitFrame(Opcodes.F_APPEND, yieldIndex, new Object[]{"jayield/lite/Yield"}, 0, null);
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
        super.visitMethodInsn(INVOKEVIRTUAL, "jayield/lite/boxes/BoolBox", "isFalse", "()Z", false);
        super.visitJumpInsn(IFEQ, loopEnd);
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "iterator", "Ljava/util/Iterator;");
        super.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
        super.visitJumpInsn(IFNE, advance);
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "validValue", "Z");
        super.visitJumpInsn(IFEQ, loopEnd);
        super.visitLabel(advance);
        super.visitVarInsn(ALOAD, 0);
        super.visitVarInsn(ALOAD, getAuxIndex());
        super.visitMethodInsn(INVOKESPECIAL, newOwner, "advance", "(Ljayield/lite/Yield;)V", false);
        super.visitJumpInsn(GOTO, loopStart);
        super.visitLabel(loopEnd);
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
        super.visitMethodInsn(INVOKEVIRTUAL, "jayield/lite/boxes/BoolBox", "isTrue", "()Z", false);
        super.visitJumpInsn(IFEQ, nextState);
        super.visitInsn(ICONST_1);
        super.visitInsn(IRETURN);
        super.visitLabel(nextState);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) { // need to change the return type in order to return the boolean value
            super.visitVarInsn(ALOAD, 0);
            super.visitFieldInsn(GETFIELD, newOwner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
            super.visitMethodInsn(INVOKEVIRTUAL, "jayield/lite/boxes/BoolBox", "isTrue", "()Z", false);
            super.visitInsn(IRETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        if (YIELD_METHOD_NAME.equals(name)) {
            super.visitVarInsn(ASTORE, getAuxIndex());
        }
    }
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable("aux", "Ljayield/lite/Yield;", "Ljayield/lite/Yield<TT;>;", startLabel, endLabel, getAuxIndex());
        super.visitMaxs(maxStack + 1, maxLocals + 1);
    }


    protected boolean isYield(int opcode, String owner, String name, String desc, boolean isInterface) {
        return opcode == INVOKEINTERFACE &&
                owner.equals(classNameToPath(Yield.class)) &&
                name.equals(YIELD_METHOD_NAME) &&
                desc.equals(YIELD_METHOD_DESCRIPTION) &&
                isInterface;
    }

    protected boolean isLoadOpcode(int opcode) {
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

    protected boolean isStoreOpcode(int opcode) {
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

    protected int getVarMapping(int var) {
        if (var != yieldIndex) {
            return var + 1 + (var > yieldIndex ? 0 : 1);
        } else {
            return 1;
        }

    }

    protected int getWrapperIndex() {
        return localVariables.values().size() + 1;
    }

    protected int getAuxIndex() {
        return localVariables.values().size() + 2;
    }

    protected void returnElementFound() {
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, newOwner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
        super.visitMethodInsn(INVOKEVIRTUAL, "jayield/lite/boxes/BoolBox", "isTrue", "()Z", false);
        super.visitInsn(IRETURN);
    }

}
