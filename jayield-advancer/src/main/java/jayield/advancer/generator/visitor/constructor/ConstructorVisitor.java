package jayield.advancer.generator.visitor.constructor;

import static java.lang.String.format;
import static jayield.advancer.generator.Constants.CONSTRUCTOR_METHOD_NAME;
import static jayield.advancer.generator.Constants.INT_ARRAY_DESCRIPTOR;
import static jayield.advancer.generator.Constants.ITERATOR;
import static jayield.advancer.generator.Constants.ITERATOR_DESCRIPTOR;
import static jayield.advancer.generator.Constants.ITERATOR_FROM_QUERY;
import static jayield.advancer.generator.Constants.ITERATOR_FROM_TRAVERSER;
import static jayield.advancer.generator.Constants.QUERY_DESCRIPTOR;
import static jayield.advancer.generator.Constants.STATE_FIELD_NAME;
import static jayield.advancer.generator.Constants.TRAVERSER_DESCRIPTOR;
import static jayield.advancer.generator.InstrumentationUtils.VOID;
import static jayield.advancer.generator.InstrumentationUtils.getClassPath;
import static jayield.advancer.generator.InstrumentationUtils.getLoadCode;
import static jayield.advancer.generator.InstrumentationUtils.getMethodDescriptor;

import java.lang.invoke.SerializedLambda;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import jayield.advancer.Advancer;
import jayield.advancer.generator.visitor.info.extractor.local.variable.LocalVariable;
import jayield.advancer.generator.wrapper.AbstractAdvance;

public class ConstructorVisitor implements Opcodes {

    public static MethodVisitor generateConstructor(ClassVisitor visitor,
                                                    Map<Integer, LocalVariable> localVariables,
                                                    String owner,
                                                    SerializedLambda traverser,
                                                    int yieldIndex,
                                                    List<String> ramifications) {
        if (yieldIndex > 0) {
            writeEmptyConstructor(visitor, owner);
        }
        return writeCapturedArgumentConstructor(visitor,
                                                localVariables,
                                                owner,
                                                ramifications,
                                                traverser,
                                                yieldIndex);
    }

    private static MethodVisitor writeCapturedArgumentConstructor(ClassVisitor visitor,
                                                                  Map<Integer, LocalVariable> localVariables,
                                                                  String owner,
                                                                  List<String> ramifications,
                                                                  SerializedLambda traverser,
                                                                  int yieldIndex) {
        MethodVisitor constructor = visitor.visitMethod(ACC_PUBLIC,
                                                        CONSTRUCTOR_METHOD_NAME + owner,
                                                        getConstructorSignature(traverser, localVariables.values()),
                                                        null,
                                                        null);
        callSuper(constructor);
        initializeFields(constructor, localVariables.values(), owner, yieldIndex);
        initializeState(constructor, owner, ramifications);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        return constructor;
    }

    private static String getConstructorSignature(SerializedLambda traverser,
                                                  Collection<LocalVariable> values) {
        String signatureWithoutYield = traverser.getImplMethodSignature()
                                                .replace("Lorg/jayield/Yield;)V", ")V");
        if (values.stream().anyMatch(lv -> LocalVariable.THIS_SYNTHETHIC_REPLACEMENT.equals(lv.getName()))) {
            return signatureWithoutYield.replace("(", "(Lorg/jayield/Query;");
        }
        return signatureWithoutYield;
    }

    private static MethodVisitor writeEmptyConstructor(ClassVisitor visitor, String owner) {
        MethodVisitor emptyConstructor = visitor.visitMethod(ACC_PUBLIC,
                                                             CONSTRUCTOR_METHOD_NAME + owner,
                                                             getMethodDescriptor(VOID),
                                                             null,
                                                             null);
        callSuper(emptyConstructor);
        emptyConstructor.visitInsn(RETURN);
        emptyConstructor.visitMaxs(0, 0);
        return emptyConstructor;
    }

    private static void callSuper(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                           getClassPath(AbstractAdvance.class),
                           CONSTRUCTOR_METHOD_NAME,
                           getMethodDescriptor(VOID),
                           false);
    }

    private static void initializeFields(MethodVisitor mv,
                                         Iterable<LocalVariable> localVariables,
                                         String owner,
                                         int yieldIndex) {
        System.out.println("\n\n#### CONSTRUCTOR LOCAL VARIABLES START ####\n");
        localVariables.forEach(System.out::println);
        System.out.println("\n\n#### CONSTRUCTOR LOCAL VARIABLES END ####\n");
        Iterator<LocalVariable> iterator = localVariables.iterator();
        int parameterIndex = 1;
        LocalVariable var;
        while (iterator.hasNext()) {
            var = iterator.next();
            if (yieldIndex != parameterIndex - 1) {
                if (var.getDesc().equals(TRAVERSER_DESCRIPTOR) || var.getDesc().equals(QUERY_DESCRIPTOR)) {
                    LocalVariable variable = new LocalVariable(var.getName(),
                                                               var.getDesc(),
                                                               var.getDesc(),
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

    private static void initializeState(MethodVisitor mv, String owner, List<String> ramifications) {
        initializeState(mv, owner, STATE_FIELD_NAME);
        ramifications.forEach(ramification -> initializeState(mv,
                                                              owner,
                                                              format("%s%s", STATE_FIELD_NAME, ramification)));
    }

    private static void initializeField(MethodVisitor mv,
                                        LocalVariable localVariable,
                                        int parameterIndex,
                                        String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitFieldInsn(PUTFIELD, owner, localVariable.getName(), localVariable.getDesc());
    }

    private static void initializeIterator(MethodVisitor mv,
                                           LocalVariable localVariable,
                                           int parameterIndex,
                                           String owner) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(getLoadCode(localVariable.getDesc()), parameterIndex);
        mv.visitMethodInsn(INVOKESTATIC,
                           getClassPath(Advancer.class),
                           ITERATOR,
                           localVariable.getDesc()
                                        .equals(TRAVERSER_DESCRIPTOR) ? ITERATOR_FROM_TRAVERSER : ITERATOR_FROM_QUERY,
                           false);
        mv.visitFieldInsn(PUTFIELD, owner, ITERATOR, ITERATOR_DESCRIPTOR);
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
}
