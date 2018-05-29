package jayield.advancer.generator.visitor.traverser;

import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariable;
import jayield.advancer.generator.visitor.ownership.ChangeOwnersMethodVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

import static java.lang.String.valueOf;
import static jayield.advancer.generator.Constants.ADVANCE;
import static jayield.advancer.generator.Constants.ADVANCE_METHOD_DESC;
import static jayield.advancer.generator.Constants.AUX;
import static jayield.advancer.generator.Constants.BOOLEAN_SUPPLIER;
import static jayield.advancer.generator.Constants.BOOL_BOX;
import static jayield.advancer.generator.Constants.BOOL_BOX_DESCRIPTOR;
import static jayield.advancer.generator.Constants.HAS_ELEMENT;
import static jayield.advancer.generator.Constants.HAS_NEXT;
import static jayield.advancer.generator.Constants.IS_FALSE;
import static jayield.advancer.generator.Constants.IS_TRUE;
import static jayield.advancer.generator.Constants.ITERATOR;
import static jayield.advancer.generator.Constants.ITERATOR_CLASS;
import static jayield.advancer.generator.Constants.ITERATOR_DESCRIPTOR;
import static jayield.advancer.generator.Constants.TRAVERSE_METHOD_NAME;
import static jayield.advancer.generator.Constants.VALID_VALUE;
import static jayield.advancer.generator.Constants.YIELD;
import static jayield.advancer.generator.Constants.YIELD_DESCRIPTOR;
import static jayield.advancer.generator.Constants.YIELD_METHOD_NAME;
import static jayield.advancer.generator.InstrumentationUtils.BOOLEAN;
import static jayield.advancer.generator.InstrumentationUtils.METHOD_PARAMETERS_END;
import static jayield.advancer.generator.InstrumentationUtils.getTypeDescriptor;
import static jayield.advancer.generator.InstrumentationUtils.insertTypeInDescriptor;

public abstract class TraverseMethodVisitor extends ChangeOwnersMethodVisitor implements Opcodes {


    protected final Map<Integer, LocalVariable> localVariables;
    protected boolean isTryAdvance;
    protected int yieldIndex;
    protected Label startLabel;
    protected Label endLabel;

    public TraverseMethodVisitor(MethodVisitor mv,
                                 String originalOwner,
                                 String newOwner,
                                 Map<Integer, LocalVariable> localVariables) {
        super(mv, originalOwner, newOwner);
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
        super.visitFrame(Opcodes.F_APPEND, yieldIndex, new Object[]{YIELD}, 0, null);
        super.visitVarInsn(ALOAD, getThisVar());
        super.visitFieldInsn(GETFIELD, newOwner, HAS_ELEMENT, BOOL_BOX_DESCRIPTOR);
        super.visitMethodInsn(INVOKEVIRTUAL,
                              BOOL_BOX,
                              IS_FALSE,
                              BOOLEAN_SUPPLIER,
                              false);
        super.visitJumpInsn(IFEQ, loopEnd);
        super.visitVarInsn(ALOAD, getThisVar());
        super.visitFieldInsn(GETFIELD, newOwner, ITERATOR, ITERATOR_DESCRIPTOR);
        super.visitMethodInsn(INVOKEINTERFACE, ITERATOR_CLASS, HAS_NEXT, BOOLEAN_SUPPLIER, true);
        super.visitJumpInsn(IFNE, advance);
        super.visitVarInsn(ALOAD, getThisVar());
        super.visitFieldInsn(GETFIELD, newOwner, VALID_VALUE, valueOf(BOOLEAN));
        super.visitJumpInsn(IFEQ, loopEnd);
        super.visitLabel(advance);
        super.visitVarInsn(ALOAD, getThisVar());
        super.visitVarInsn(ALOAD, getAuxIndex());
        super.visitMethodInsn(INVOKESPECIAL, newOwner, ADVANCE, ADVANCE_METHOD_DESC, false);
        super.visitJumpInsn(GOTO, loopStart);
        super.visitLabel(loopEnd);
        super.visitVarInsn(ALOAD, getThisVar());
        super.visitFieldInsn(GETFIELD, newOwner, HAS_ELEMENT, BOOL_BOX_DESCRIPTOR);
        super.visitMethodInsn(INVOKEVIRTUAL, BOOL_BOX, IS_TRUE, BOOLEAN_SUPPLIER, false);
        super.visitJumpInsn(IFEQ, nextState);
        super.visitInsn(ICONST_1);
        super.visitInsn(isTryAdvance ? IRETURN : RETURN);
        super.visitLabel(nextState);
    }

    protected abstract int getThisVar();

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN && isTryAdvance) { // need to change the return type in order to return the boolean value
            super.visitVarInsn(ALOAD, getThisVar());
            super.visitFieldInsn(GETFIELD, newOwner, HAS_ELEMENT, BOOL_BOX_DESCRIPTOR);
            super.visitMethodInsn(INVOKEVIRTUAL, BOOL_BOX, IS_TRUE, BOOLEAN_SUPPLIER, false);
            super.visitInsn(IRETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        if (YIELD_METHOD_NAME.equals(name)) {
            int index = getHandleIndex(bsmArgs);
            if (index != -1) {
                Handle lambda = (Handle) bsmArgs[index];
                Handle newHandle = new Handle(lambda.getTag(),
                                              lambda.getOwner(),
                                              lambda.getName(),
                                              insertTypeInDescriptor(lambda.getDesc(), newOwner),
                                              lambda.isInterface());
                bsmArgs[index] = newHandle;
                super.visitVarInsn(ALOAD, getThisVar());
                super.visitInvokeDynamicInsn(name,
                                             desc.replace(valueOf(METHOD_PARAMETERS_END),
                                                          String.format("%s%c",
                                                                        getTypeDescriptor(newOwner),
                                                                        METHOD_PARAMETERS_END)),
                                             bsm,
                                             bsmArgs);
            } else {
                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }
            super.visitVarInsn(ASTORE, getAuxIndex());
        } else {
            super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }
    }

    private int getHandleIndex(Object[] bsmArgs) {
        for (int i = 0; i < bsmArgs.length; i++) {
            if (bsmArgs[i] instanceof Handle)
                return i;
        }
        return -1;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitLocalVariable(AUX, YIELD_DESCRIPTOR, null, startLabel, endLabel, getAuxIndex());
        super.visitMaxs(maxStack, maxLocals);
    }

    protected int getWrapperIndex() {
        return localVariables.values().size() + 1;
    }

    protected int getAuxIndex() {
        return localVariables.values().size() + 2;
    }

}
