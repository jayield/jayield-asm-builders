package jayield.lite.codegen;

import jayield.lite.Advancer;
import org.objectweb.asm.*;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.SerializedLambda;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static jayield.lite.codegen.GeneratorUtils.classNameToPath;


public class LambdaToAdvancerMethodGenerator implements Opcodes {

    /* MAPPERS */
    private static final Map<Character, Consumer<MethodVisitor>> PRIMITIVE_TYPE_MAPPER;
    static {
        PRIMITIVE_TYPE_MAPPER = new HashMap<>();

        String booleanType = "java/lang/Boolean";
        String booleanValue = "booleanValue";
        String booleanDescription = "()Z";
        PRIMITIVE_TYPE_MAPPER.put('Z', mv -> {
            mv.visitTypeInsn(CHECKCAST, booleanType);
            mv.visitMethodInsn(INVOKEVIRTUAL, booleanType, booleanValue, booleanDescription, false);
        });

        String byteType = "java/lang/Byte";
        String byteValue = "byteValue";
        String byteDescription = "()B";
        PRIMITIVE_TYPE_MAPPER.put('B', mv -> {
            mv.visitTypeInsn(CHECKCAST, byteType);
            mv.visitMethodInsn(INVOKEVIRTUAL, byteType, byteValue, byteDescription, false);
        });

        String characterType = "java/lang/Character";
        String charValue = "charValue";
        String characterDescription = "()C";
        PRIMITIVE_TYPE_MAPPER.put('C', mv -> {
            mv.visitTypeInsn(CHECKCAST, characterType);
            mv.visitMethodInsn(INVOKEVIRTUAL, characterType, charValue, characterDescription, false);
        });

        String shortType = "java/lang/Short";
        String shortValue = "shortValue";
        String shortDescription = "()S";
        PRIMITIVE_TYPE_MAPPER.put('S', mv -> {
            mv.visitTypeInsn(CHECKCAST, shortType);
            mv.visitMethodInsn(INVOKEVIRTUAL, shortType, shortValue, shortDescription, false);
        });

        String integerType = "java/lang/Integer";
        String intValue = "intValue";
        String integerDescription = "()I";
        PRIMITIVE_TYPE_MAPPER.put('I', mv -> {
            mv.visitTypeInsn(CHECKCAST, integerType);
            mv.visitMethodInsn(INVOKEVIRTUAL, integerType, intValue, integerDescription, false);
        });

        String floatType = "java/lang/Float";
        String floatValue = "floatValue";
        String floatDescription = "()F";
        PRIMITIVE_TYPE_MAPPER.put('F', mv -> {
            mv.visitTypeInsn(CHECKCAST, floatType);
            mv.visitMethodInsn(INVOKEVIRTUAL, floatType, floatValue, floatDescription, false);
        });

        String longType = "java/lang/Long";
        String longValue = "longValue";
        String longDescription = "()J";
        PRIMITIVE_TYPE_MAPPER.put('J', mv -> {
            mv.visitTypeInsn(CHECKCAST, longType);
            mv.visitMethodInsn(INVOKEVIRTUAL, longType, longValue, longDescription, false);
        });

        String doubleType = "java/lang/Double";
        String doubleValue = "doubleValue";
        String doubleDescription = "()D";
        PRIMITIVE_TYPE_MAPPER.put('D', mv -> {
            mv.visitTypeInsn(CHECKCAST, doubleType);
            mv.visitMethodInsn(INVOKEVIRTUAL, doubleType, doubleValue, doubleDescription, false);
        });
    }
    /* KEY STRINGS */
    private static final String LAMBDA_TO_ADVANCER_INTERFACE_METHOD_NAME = "getAdvancer";
    private static final String SERIALIZED_LAMBDA_NAME = classNameToPath(SerializedLambda.class);
    private static final String LAMBDA_TO_ADVANCER_INTERFACE_METHOD_SIGNATURE = String.format("(%s)%s", getDescriptor(SerializedLambda.class), getDescriptor(Advancer.class));
    private static final String GET_CAPTURED_ARG_METHOD_NAME = "getCapturedArg";
    private static final String TRY_ADVANCE_METHOD_NAME = "tryAdvance";
    private static final String GET_CAPTURED_ARG_METHOD_SIGNATURE = "(I)Ljava/lang/Object;";
    private static final String ALT_METAFACTORY_METHOD_NAME = "altMetafactory";
    private static final String ALT_METAFACTORY_METHOD_SIGNATURE = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;";
    private static final String IN_YIELD_OUT_BOOLEAN_DESCRIPTOR = "(Ljayield/lite/Yield;)Z";
    private static final String RETURN_VOID = ")V";
    private static final String RETURN_BOOLEAN = ")Z";
    private static final String RETURN_ADVANCER = ")Ljayield/lite/Advancer;";
    private static final String RETURN_VOID_WITH_YIELD = "Ljayield/lite/Yield;)V";
    private static final char COMPLEX_TYPE_START = 'L';
    private static final char COMPLEX_TYPE_END = ';';
    private static final char SIGNATURE_START_CHARACTER = '(';

    /* FIELDS */
    private final SerializedLambda lambda;
    private final ClassVisitor target;
    private final String targetName;

    public LambdaToAdvancerMethodGenerator(SerializedLambda lambda, ClassVisitor target, String targetName) {
        this.lambda = lambda;
        this.target = target;
        this.targetName = targetName;
    }


    public void generateGetAdvancerMethod() {
        MethodVisitor mv = target.visitMethod(ACC_PUBLIC,
                LAMBDA_TO_ADVANCER_INTERFACE_METHOD_NAME,
                LAMBDA_TO_ADVANCER_INTERFACE_METHOD_SIGNATURE,
                null,
                null);

        mv.visitCode();
        loadCapturedArguments(mv);
        generateAdvancer(mv);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(getMaxStack(), getMaxLocals());
        mv.visitEnd();

    }

    private int getMaxLocals() {
        return lambda.getCapturedArgCount() + 1;
    }

    private int getMaxStack() {
        return lambda.getCapturedArgCount() + 1;
    }

    private void generateAdvancer(MethodVisitor mv) {
        Handle bsm = new Handle(Opcodes.H_INVOKESTATIC, classNameToPath(LambdaMetafactory.class),
                ALT_METAFACTORY_METHOD_NAME,
                ALT_METAFACTORY_METHOD_SIGNATURE,
                false);

        Handle targetLambdaHandle = new Handle(Opcodes.H_INVOKESTATIC,
                targetName,
                targetName,
                getSignatureFromLambda(RETURN_VOID, RETURN_BOOLEAN),
                false);

        Object[] bsmArgs = {
                Type.getType(IN_YIELD_OUT_BOOLEAN_DESCRIPTOR),
                targetLambdaHandle,
                Type.getType(IN_YIELD_OUT_BOOLEAN_DESCRIPTOR),
                5,
                0
        };

        mv.visitInvokeDynamicInsn(TRY_ADVANCE_METHOD_NAME,
                getSignatureFromLambda(RETURN_VOID_WITH_YIELD, RETURN_ADVANCER),
                bsm,
                bsmArgs);
    }

    private String getSignatureFromLambda(String source, String replacement) {
        return lambda.getImplMethodSignature().replace(source, replacement);
    }

    private void loadCapturedArguments(MethodVisitor mv) {
        Consumer<MethodVisitor>[] castersToArgumentType = getCapturedArgumentTypeCasters();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 2); // initialize counter to 0
        for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
            mv.visitVarInsn(ALOAD, 1); // load SerializedLambda
            mv.visitVarInsn(ILOAD, 2); // load Counter
            mv.visitMethodInsn(INVOKEVIRTUAL, SERIALIZED_LAMBDA_NAME, GET_CAPTURED_ARG_METHOD_NAME, GET_CAPTURED_ARG_METHOD_SIGNATURE, false);
            castersToArgumentType[i].accept(mv); // cast argument to the method's declared type
            mv.visitIincInsn(2, 1);
        }
    }

    @SuppressWarnings("unchecked")
    private Consumer<MethodVisitor>[] getCapturedArgumentTypeCasters() {
        Consumer<MethodVisitor>[] result = new Consumer[lambda.getCapturedArgCount()];
        String[] signatureTokens = getSignatureTokens(lambda.getImplMethodSignature());
        for (int i = 0; i < signatureTokens.length; i++) {
            if(signatureTokens[i].length() > 1){
                result[i] = typeCast(signatureTokens[i]);
            } else {
                result[i] = PRIMITIVE_TYPE_MAPPER.get(signatureTokens[i].charAt(0));
            }
        }
        return result;
    }

    private Consumer<MethodVisitor> typeCast(String token) {
        return mv -> mv.visitTypeInsn(CHECKCAST, token);
    }

    private String[] getSignatureTokens(String signature) {
        String[] tokens = new String[lambda.getCapturedArgCount()];
        int tokenCount = 0;
        int charIndexer = 0;
        while(tokenCount < lambda.getCapturedArgCount()) {
            char currentCharacter = signature.charAt(charIndexer);
            if(currentCharacter == COMPLEX_TYPE_START){
                int typeDescriptionEndIndex = signature.indexOf(COMPLEX_TYPE_END, charIndexer);
                tokens[tokenCount++] = signature.substring(charIndexer + 1, typeDescriptionEndIndex); // + 1, Start after the L character
                charIndexer = typeDescriptionEndIndex;
            } else {
                if(currentCharacter != SIGNATURE_START_CHARACTER){
                    tokens[tokenCount++] = String.valueOf(currentCharacter);
                }
            }
            charIndexer++;
        }
        return tokens;
    }

    private static String getDescriptor(Class<?> clazz) {
        return String.format("L%s;", classNameToPath(clazz));
    }

}
