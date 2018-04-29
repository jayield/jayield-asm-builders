package jayield.advancer.generator.visitor.captured.context;

import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

import static jayield.advancer.generator.InstrumentationUtils.isLoadOpcode;
import static jayield.advancer.generator.InstrumentationUtils.isStoreOpcode;


public class CapturedContextVisitor implements Opcodes {

    private final MethodVisitor mv;
    private final String newOwner;
    private final CapturedContextHandler handler;
    private final Map<Integer, LocalVariable> localVariables;

    public CapturedContextVisitor(MethodVisitor mv,
                                  String newOwner,
                                  CapturedContextHandler handler,
                                  Map<Integer, LocalVariable> localVariables) {
        this.mv = mv;
        this.newOwner = newOwner;
        this.handler = handler;
        this.localVariables = localVariables;
    }

    public void visitIincInsn(int var, int increment) {
        LocalVariable localVariable = this.localVariables.get(var);
        this.loadVarFromField(var);
        int actualVar = handler.getActualVar(var);
        mv.visitIincInsn(actualVar, increment);
        mv.visitVarInsn(ALOAD, handler.getThisVar());
        mv.visitVarInsn(ILOAD, actualVar);
        mv.visitFieldInsn(PUTFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
    }

    public void visitVarInsn(int opcode, int var) {
        int actualVar = handler.getActualVar(var);
        if (isLoadOpcode(opcode) && handler.isLocalVariable(var)) {
            LocalVariable localVariable = this.localVariables.get(var);
            mv.visitVarInsn(ALOAD, handler.getThisVar());
            mv.visitFieldInsn(GETFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
            mv.visitVarInsn(opcode + 33, actualVar);
            mv.visitVarInsn(opcode, actualVar);
        } else if (isStoreOpcode(opcode) && handler.isLocalVariable(var)) {
            LocalVariable localVariable = this.localVariables.get(var);
            mv.visitVarInsn(opcode, actualVar);
            mv.visitVarInsn(ALOAD, handler.getThisVar());
            mv.visitVarInsn(opcode - 33, actualVar);
            mv.visitFieldInsn(PUTFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
        } else {
            handler.handleNonLocalVariable(opcode, var);
        }
    }

    private void loadVarFromField(int var) {
        int actualVar = handler.getActualVar(var);
        LocalVariable localVariable = this.localVariables.get(var);
        mv.visitVarInsn(ALOAD, handler.getThisVar());
        mv.visitFieldInsn(GETFIELD, newOwner, localVariable.getName(), localVariable.getDesc());
        mv.visitVarInsn(ISTORE, actualVar);
    }
}
