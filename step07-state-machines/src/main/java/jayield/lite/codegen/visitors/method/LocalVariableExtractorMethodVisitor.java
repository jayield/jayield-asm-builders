package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

public class LocalVariableExtractorMethodVisitor extends MethodVisitor implements Opcodes {

    private final Map<Integer, LocalVariable> localVariables;

    public LocalVariableExtractorMethodVisitor(MethodVisitor methodVisitor) {
        super(ASM6, methodVisitor);
        this.localVariables = new HashMap<>();
    }


    public Map<Integer, LocalVariable> getLocalVariables() {
        return localVariables;
    }


    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
        this.localVariables.put(index, new LocalVariable(name, desc, signature, start, end, index));
    }
}

