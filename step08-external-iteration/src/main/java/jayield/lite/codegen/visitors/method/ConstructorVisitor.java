package jayield.lite.codegen.visitors.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Iterator;

import static jayield.lite.codegen.GeneratorUtils.getLoadCode;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;

public class ConstructorVisitor extends ChangeOwnersMethodVisitor implements Opcodes {

    public static final String INIT_METHOD_NAME = "<init>";
    public static final String STATE_FIELD_NAME = "$state";
    public static final String YIELD_VARIABLE_NAME = "yield";

    private final String stateFieldName;

    public ConstructorVisitor(MethodVisitor mv, String originalOwner, String newOwner, String stateFieldName) {
        super(mv, originalOwner, newOwner);
        this.stateFieldName = stateFieldName;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            initializeState(this, newOwner, stateFieldName);
        }
        super.visitInsn(opcode);
    }

    private static void initializeState(MethodVisitor mv, String owner, String fieldName) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IASTORE);
        mv.visitFieldInsn(PUTFIELD, owner, fieldName, INT_ARRAY_DESCRIPTION);
    }

    public static MethodVisitor generateConstructor(ClassVisitor visitor, Iterable<LocalVariable> localVariables, String desc, String owner) {
        MethodVisitor emptyConstructor = visitor.visitMethod(ACC_PUBLIC,
                INIT_METHOD_NAME + owner,
                "()V",
                null,
                null);
        callSuper(emptyConstructor);
        emptyConstructor.visitInsn(RETURN);
        emptyConstructor.visitMaxs(1, 1);

        MethodVisitor constructor = visitor.visitMethod(ACC_PUBLIC,
                INIT_METHOD_NAME + owner,
                extractConstructorParameters(desc),
                null,
                null);
        callSuper(constructor);
        initializeFields(constructor, localVariables, owner);
        initializeState(constructor, owner, STATE_FIELD_NAME);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 3);
        return constructor;
    }

    private static void callSuper(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", INIT_METHOD_NAME, "()V", false);
    }

    public static String extractConstructorParameters(String desc) {
        return String.format("%s%s", desc.substring(0, desc.lastIndexOf('L')), ")V");
    }

    private static void initializeFields(MethodVisitor mv, Iterable<LocalVariable> localVariables, String owner) {
        Iterator<LocalVariable> iterator = localVariables.iterator();
        int parameterIndex = 1;
        LocalVariable var;
        while (iterator.hasNext()) {
            var = iterator.next();
            if(!var.getName().equals(YIELD_VARIABLE_NAME)){
                initializeField(mv, var, parameterIndex++, owner);
            } else {
                return;
            }
        }
    }

    private static void initializeField(MethodVisitor mv, LocalVariable localVariable, int parameterIndex, String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitFieldInsn(PUTFIELD, owner, localVariable.getName(), localVariable.getDesc());
    }
}
