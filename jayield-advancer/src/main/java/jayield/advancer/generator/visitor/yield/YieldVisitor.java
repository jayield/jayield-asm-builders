package jayield.advancer.generator.visitor.yield;

import jayield.advancer.generator.visitor.captured.context.CapturedContextHandler;
import jayield.advancer.generator.visitor.info.extractor.Info;
import jayield.advancer.generator.visitor.state.machine.StateMachineMethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static java.lang.String.valueOf;
import static jayield.advancer.generator.Constants.INT_ARRAY_DESCRIPTOR;
import static jayield.advancer.generator.Constants.VALID_VALUE;
import static jayield.advancer.generator.InstrumentationUtils.BOOLEAN;

public class YieldVisitor extends StateMachineMethodVisitor implements CapturedContextHandler {

    private final int thisIndex;


    public YieldVisitor(ClassVisitor klass,
                        MethodVisitor mv,
                        String originalOwner,
                        String newOwner,
                        String stateFieldName,
                        Info info,
                        Type[] argumentTypes) {
        super(mv, originalOwner, newOwner, stateFieldName, info, argumentTypes);
        this.isTryAdvance = false;
        this.thisIndex = argumentTypes.length - 1;
        createCases();
    }

    @Override
    public int getThisVar() {
        return thisIndex;
    }

    @Override
    public int getActualVar(int var) {
        if (var == thisIndex) {
            return thisIndex + 1;
        } else if (var > thisIndex) {
            return var + 1;
        } else {
            return var;
        }
    }

    @Override
    public boolean isLocalVariable(int var) {
        return var > thisIndex;
    }

    @Override
    public void handleNonLocalVariable(int opcode, int var) {
        mv.visitVarInsn(opcode, getActualVar(var));
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            mv.visitVarInsn(ALOAD, getThisVar());
            super.visitFieldInsn(GETFIELD, newOwner, stateField, INT_ARRAY_DESCRIPTOR);
            super.visitInsn(ICONST_0);
            super.visitInsn(DUP);
            super.visitInsn(IASTORE);
            mv.visitVarInsn(ALOAD, getThisVar());
            super.visitInsn(ICONST_0);
            super.visitFieldInsn(PUTFIELD, newOwner, VALID_VALUE, valueOf(BOOLEAN));
        }
        super.visitInsn(opcode);
    }

}
