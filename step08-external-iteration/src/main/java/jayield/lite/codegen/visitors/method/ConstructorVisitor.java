package jayield.lite.codegen.visitors.method;

import jayield.lite.codegen.wrappers.AbstractAdvance;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;
import static jayield.lite.codegen.GeneratorUtils.getLoadCode;
import static jayield.lite.codegen.visitors.Constants.INT_ARRAY_DESCRIPTION;

public class ConstructorVisitor extends ChangeOwnersMethodVisitor implements Opcodes {

    public static final String INIT_METHOD_NAME = "<init>";
    public static final String STATE_FIELD_NAME = "$state";
    public static final String YIELD_VARIABLE_NAME = "yield";
    public static final String TRAVERSABLE_TYPE = "Ljayield/lite/Traversable";

    private final String stateFieldName;

    public ConstructorVisitor(MethodVisitor mv, String originalOwner, String newOwner, String stateFieldName) {
        super(mv, originalOwner, newOwner);
        this.stateFieldName = stateFieldName;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            initializeState(this, newOwner, stateFieldName, new ArrayList<>());
        }
        super.visitInsn(opcode);
    }

    private static void initializeState(MethodVisitor mv, String owner, String fieldName, List<String> ramifications) {
        initializeState(mv, owner, fieldName);
        ramifications.forEach(ramification -> initializeState(mv, owner, fieldName + ramification));
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

    public static MethodVisitor generateConstructor(ClassVisitor visitor, Iterable<LocalVariable> localVariables, String desc, String owner, List<String> ramifications) {
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
        initializeState(constructor, owner, STATE_FIELD_NAME, ramifications);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 3);
        return constructor;
    }

    private static void callSuper(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, classNameToPath(AbstractAdvance.class), INIT_METHOD_NAME, "()V", false);
    }

    public static String extractConstructorParameters(String desc) {
        return String.format("%s%s", desc.substring(0, desc.lastIndexOf('L')), ")V");
    }

    private static void initializeFields(MethodVisitor mv, Iterable<LocalVariable> localVariables, String owner) {
        Iterator<LocalVariable> iterator = localVariables.iterator();
        int parameterIndex = 1;
        LocalVariable var;
//        initialize(mv, owner);
        while (iterator.hasNext()) {
            var = iterator.next();
            if(!var.getName().equals(YIELD_VARIABLE_NAME)){
                if(var.getDesc().equals(TRAVERSABLE_TYPE + ';')){
                    LocalVariable variable = new LocalVariable(var.getName(), var.getDesc(), TRAVERSABLE_TYPE + "<TT;>;", var.getStart(), var.getEnd(), var.getIndex());
                    initializeField(mv, variable, parameterIndex, owner);
                    initializeIterator(mv, variable, parameterIndex++, owner);
                } else {
                    initializeField(mv, var, parameterIndex++, owner);
                }
            } else {
                return;
            }
        }
    }

    private static void initialize(MethodVisitor mv, String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, "jayield/lite/boxes/BoolBox");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "jayield/lite/boxes/BoolBox", INIT_METHOD_NAME, "()V", false);
        mv.visitFieldInsn(PUTFIELD, owner, "hasElement", "Ljayield/lite/boxes/BoolBox;");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, owner, "validValue", "Z");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, owner, "firstFailed", "Z");
    }

    private static void initializeIterator(MethodVisitor mv, LocalVariable localVariable, int parameterIndex, String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, "jayield/lite/Traversable", "iterator", "()Ljava/util/Iterator;", true);
        mv.visitFieldInsn(PUTFIELD, owner, "iterator", "Ljava/util/Iterator;");
    }

    private static void initializeField(MethodVisitor mv, LocalVariable localVariable, int parameterIndex, String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitFieldInsn(PUTFIELD, owner, localVariable.getName(), localVariable.getDesc());
    }
}
