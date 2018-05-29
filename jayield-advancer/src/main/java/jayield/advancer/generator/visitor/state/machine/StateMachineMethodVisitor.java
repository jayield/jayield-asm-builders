package jayield.advancer.generator.visitor.state.machine;

import jayield.advancer.generator.visitor.captured.context.CapturedContextHandler;
import jayield.advancer.generator.visitor.captured.context.CapturedContextVisitor;
import jayield.advancer.generator.visitor.info.extractor.Info;
import jayield.advancer.generator.visitor.traverser.TraverseMethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static jayield.advancer.generator.Constants.BOOL_BOX;
import static jayield.advancer.generator.Constants.BOOL_BOX_DESCRIPTOR;
import static jayield.advancer.generator.Constants.CONSTRUCTOR_METHOD_NAME;
import static jayield.advancer.generator.Constants.DEBUG;
import static jayield.advancer.generator.Constants.FORMAT;
import static jayield.advancer.generator.Constants.FORMAT_METHOD_DESCRIPTOR;
import static jayield.advancer.generator.Constants.HAS_ELEMENT;
import static jayield.advancer.generator.Constants.INT_ARRAY_DESCRIPTOR;
import static jayield.advancer.generator.Constants.INT_BOX_METHOD_DESCRIPTOR;
import static jayield.advancer.generator.Constants.JAVA_IO_PRINT_STREAM;
import static jayield.advancer.generator.Constants.JAVA_LANG_INTEGER;
import static jayield.advancer.generator.Constants.JAVA_LANG_OBJECT;
import static jayield.advancer.generator.Constants.JAVA_LANG_STRING;
import static jayield.advancer.generator.Constants.JAVA_LANG_SYSTEM;
import static jayield.advancer.generator.Constants.OUT;
import static jayield.advancer.generator.Constants.PRINTLN;
import static jayield.advancer.generator.Constants.PRINTLN_DESCRIPTOR;
import static jayield.advancer.generator.Constants.PRINT_STREAM_DESCRIPTOR;
import static jayield.advancer.generator.Constants.RESET;
import static jayield.advancer.generator.Constants.STATE_TEMPLATE_STRING;
import static jayield.advancer.generator.Constants.THIS;
import static jayield.advancer.generator.Constants.VALUE_OF;
import static jayield.advancer.generator.Constants.WRAPPER;
import static jayield.advancer.generator.Constants.YIELD_DESCRIPTOR;
import static jayield.advancer.generator.Constants.YIELD_WRAPPER;
import static jayield.advancer.generator.InstrumentationUtils.VOID;
import static jayield.advancer.generator.InstrumentationUtils.getMethodDescriptor;
import static jayield.advancer.generator.InstrumentationUtils.getTypeDescriptor;
import static jayield.advancer.generator.InstrumentationUtils.isYield;

public class StateMachineMethodVisitor extends TraverseMethodVisitor implements CapturedContextHandler {

    protected final String stateField;
    private final Info info;
    private final CapturedContextVisitor contextVisitor;
    private Label[] labels;
    private int[] cases;
    private int state = 0;


    public StateMachineMethodVisitor(MethodVisitor mv,
                                     String originalOwner,
                                     String newOwner,
                                     String stateFieldName,
                                     Info info,
                                     Type[] argumentTypes) {
        super(mv, originalOwner, newOwner, info.getLocalVariables());
        contextVisitor = new CapturedContextVisitor(mv, newOwner, this, info.getLocalVariables());
        isTryAdvance = true;
        stateField = stateFieldName;
        this.info = info;
        yieldIndex = argumentTypes.length - 1;
        createCases();
    }

    protected void createCases() {
        Integer states = info.getStates();
        if (states != null || !isTryAdvance) {
            int size = (states == null) ? 2 : states + 1;
            labels = new Label[size];
            cases = new int[size];
            for (int i = 0; i < size; i++) {
                cases[i] = i;
                labels[i] = new Label();
            }
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
        debugState();
        startLabel = new Label();
        endLabel = new Label();
        super.visitFrame(F_SAME, 0, null, 1, null);
        if (isTryAdvance) {
            setUpElementFoundBox();
        }
        setUpStates();
    }

    private void setUpElementFoundBox() {
        // reset boolbox
        super.visitVarInsn(ALOAD, getThisVar());
        super.visitFieldInsn(GETFIELD, newOwner, HAS_ELEMENT, BOOL_BOX_DESCRIPTOR);
        super.visitMethodInsn(INVOKEVIRTUAL, BOOL_BOX, RESET, getMethodDescriptor(VOID), false);

        // instantiate wrapper
        super.visitTypeInsn(NEW, YIELD_WRAPPER);
        super.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, getActualVar(yieldIndex));
        mv.visitVarInsn(ALOAD, getThisVar());
        super.visitFieldInsn(GETFIELD, newOwner, HAS_ELEMENT, BOOL_BOX_DESCRIPTOR);
        super.visitMethodInsn(INVOKESPECIAL,
                              YIELD_WRAPPER,
                              CONSTRUCTOR_METHOD_NAME,
                              getMethodDescriptor(VOID,
                                                  YIELD_DESCRIPTOR,
                                                  BOOL_BOX_DESCRIPTOR),
                              false);
        mv.visitVarInsn(ASTORE, getWrapperIndex());
    }

    private void setUpStates() {
        if (labels != null) {
            mv.visitVarInsn(ALOAD, getThisVar());
            super.visitFieldInsn(GETFIELD, newOwner, stateField, INT_ARRAY_DESCRIPTOR);
            super.visitInsn(ICONST_0);
            super.visitInsn(IALOAD);
            super.visitLookupSwitchInsn(endLabel, cases, labels);
            startLabel = labels[state++];
            super.visitLabel(startLabel);
        }
    }

    @Override
    public int getThisVar() {
        return 0;
    }

    @Override
    public int getActualVar(int var) {
        if (var != yieldIndex) {
            return var + 1 + (var > yieldIndex ? 0 : 1);
        } else {
            return 1;
        }
    }

    @Override
    public boolean isLocalVariable(int var) {
        return var != yieldIndex;
    }

    @Override
    public void handleNonLocalVariable(int opcode, int var) {
        if (var == yieldIndex) {
            mv.visitVarInsn(opcode, getWrapperIndex());
        } else {
            mv.visitVarInsn(opcode, getActualVar(var));
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        contextVisitor.visitVarInsn(opcode, var);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        contextVisitor.visitIincInsn(var, increment);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, startLabel, endLabel, getActualVar(index));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (isYield(opcode, owner, name, desc, itf)) {
            finishState();
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            super.visitLabel(endLabel);
            finishState();
            super.visitLocalVariable(THIS, getTypeDescriptor(newOwner), null, startLabel, endLabel, getThisVar());
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitLocalVariable(WRAPPER, YIELD_DESCRIPTOR, null, startLabel, endLabel, getWrapperIndex());
        super.visitMaxs(maxStack, maxLocals);
    }

    private void finishState() {
        boolean isACase = labels != null && state < labels.length;
        if (isACase) {
            mv.visitVarInsn(ALOAD, getThisVar());
            super.visitFieldInsn(GETFIELD, newOwner, stateField, INT_ARRAY_DESCRIPTOR);
            super.visitInsn(ICONST_0);
            super.visitLdcInsn(state);
            super.visitInsn(IASTORE);
        }
        super.visitInsn(RETURN);
        if (isACase) {
            if (state < labels.length) {
                super.visitLabel(labels[state++]);
            }
        }
    }

    private void debugState() {
        if (DEBUG) {
            mv.visitFieldInsn(GETSTATIC, JAVA_LANG_SYSTEM, OUT, PRINT_STREAM_DESCRIPTOR);
            mv.visitLdcInsn(STATE_TEMPLATE_STRING);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, JAVA_LANG_OBJECT);
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            super.visitVarInsn(ALOAD, getThisVar());
            super.visitFieldInsn(GETFIELD, newOwner, stateField, INT_ARRAY_DESCRIPTOR);
            mv.visitInsn(ICONST_0);
            super.visitInsn(IALOAD);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_INTEGER, VALUE_OF, INT_BOX_METHOD_DESCRIPTOR, false);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_STRING, FORMAT, FORMAT_METHOD_DESCRIPTOR, false);
            mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_IO_PRINT_STREAM, PRINTLN, PRINTLN_DESCRIPTOR, false);
        }
    }
}
