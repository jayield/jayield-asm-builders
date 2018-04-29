package jayield.advancer.generator.visitor.constructor;

import jayield.traversable.Traversable;
import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariable;
import jayield.advancer.generator.wrapper.AbstractAdvance;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static jayield.advancer.generator.Constants.CONSTRUCTOR_METHOD_NAME;
import static jayield.advancer.generator.Constants.INT_ARRAY_DESCRIPTOR;
import static jayield.advancer.generator.Constants.ITERATOR;
import static jayield.advancer.generator.Constants.ITERATOR_DESCRIPTOR;
import static jayield.advancer.generator.Constants.STATE_FIELD_NAME;
import static jayield.advancer.generator.Constants.TRAVERSABLE_DESCRIPTOR;
import static jayield.advancer.generator.InstrumentationUtils.METHOD_PARAMETERS_END;
import static jayield.advancer.generator.InstrumentationUtils.OBJECT;
import static jayield.advancer.generator.InstrumentationUtils.VOID;
import static jayield.advancer.generator.InstrumentationUtils.getClassPath;
import static jayield.advancer.generator.InstrumentationUtils.getLoadCode;
import static jayield.advancer.generator.InstrumentationUtils.getMethodDescriptor;

public class ConstructorVisitor implements Opcodes {

    private static void initializeState(MethodVisitor mv, String owner, List<String> ramifications) {
        initializeState(mv, owner, STATE_FIELD_NAME);
        ramifications.forEach(ramification -> initializeState(mv,
                                                              owner,
                                                              format("%s%s", STATE_FIELD_NAME, ramification)));
    }

    private static void initializeState(MethodVisitor mv, String owner, String fieldName) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IASTORE);
        mv.visitFieldInsn(PUTFIELD, owner, fieldName, INT_ARRAY_DESCRIPTOR);
    }

    public static MethodVisitor generateConstructor(ClassVisitor visitor,
                                                    Iterable<LocalVariable> localVariables,
                                                    String desc,
                                                    String owner,
                                                    List<String> ramifications,
                                                    Type[] argumentTypes) {
        MethodVisitor emptyConstructor = visitor.visitMethod(ACC_PUBLIC,
                                                             CONSTRUCTOR_METHOD_NAME + owner,
                                                             getMethodDescriptor(VOID),
                                                             null,
                                                             null);
        callSuper(emptyConstructor);
        emptyConstructor.visitInsn(RETURN);
        emptyConstructor.visitMaxs(1, 1);

        MethodVisitor constructor = visitor.visitMethod(ACC_PUBLIC,
                                                        CONSTRUCTOR_METHOD_NAME + owner,
                                                        extractConstructorParameters(desc),
                                                        null,
                                                        null);
        callSuper(constructor);
        initializeFields(constructor, localVariables, owner, argumentTypes.length -1);
        initializeState(constructor, owner, ramifications);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(2, 3);
        return constructor;
    }

    private static void callSuper(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                           getClassPath(AbstractAdvance.class),
                           CONSTRUCTOR_METHOD_NAME,
                           getMethodDescriptor(VOID),
                           false);
    }

    private static String extractConstructorParameters(String desc) {
        return format("%s%c%c", desc.substring(0, desc.lastIndexOf(OBJECT)), METHOD_PARAMETERS_END, VOID);
    }

    private static void initializeFields(MethodVisitor mv,
                                         Iterable<LocalVariable> localVariables,
                                         String owner,
                                         int yieldIndex) {
        Iterator<LocalVariable> iterator = localVariables.iterator();
        int parameterIndex = 1;
        LocalVariable var;
        while (iterator.hasNext()) {
            var = iterator.next();
            if (yieldIndex != parameterIndex - 1) {
                if (var.getDesc().equals(TRAVERSABLE_DESCRIPTOR)) {
                    LocalVariable variable = new LocalVariable(var.getName(),
                                                               var.getDesc(),
                                                               TRAVERSABLE_DESCRIPTOR,
                                                               var.getStart(),
                                                               var.getEnd(),
                                                               var.getIndex());
                    initializeField(mv, variable, parameterIndex, owner);
                    initializeIterator(mv, variable, parameterIndex++, owner);
                } else {
                    initializeField(mv, var, parameterIndex++, owner);
                }
            }
        }
    }

    private static void initializeIterator(MethodVisitor mv,
                                           LocalVariable localVariable,
                                           int parameterIndex,
                                           String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitMethodInsn(INVOKEINTERFACE,
                           getClassPath(Traversable.class),
                           ITERATOR,
                           getMethodDescriptor(ITERATOR_DESCRIPTOR),
                           true);
        mv.visitFieldInsn(PUTFIELD, owner, ITERATOR, ITERATOR_DESCRIPTOR);
    }

    private static void initializeField(MethodVisitor mv,
                                        LocalVariable localVariable,
                                        int parameterIndex,
                                        String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitFieldInsn(PUTFIELD, owner, localVariable.getName(), localVariable.getDesc());
    }
}
