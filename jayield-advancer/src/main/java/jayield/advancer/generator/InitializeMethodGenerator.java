package jayield.advancer.generator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.SerializedLambda;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static jayield.advancer.generator.Constants.CONSTRUCTOR_METHOD_NAME;
import static jayield.advancer.generator.Constants.GET_CAPTURED_ARG_METHOD_NAME;
import static jayield.advancer.generator.Constants.GET_CAPTURED_ARG_METHOD_SIGNATURE;
import static jayield.advancer.generator.Constants.INITIALIZE;
import static jayield.advancer.generator.Constants.INITIALIZE_METHOD_DESCRIPTOR;
import static jayield.advancer.generator.Constants.PRIMITIVE_TYPE_MAPPER;
import static jayield.advancer.generator.Constants.SERIALIZED_LAMBDA;
import static jayield.advancer.generator.InstrumentationUtils.ARRAY;
import static jayield.advancer.generator.InstrumentationUtils.METHOD_PARAMETERS_END;
import static jayield.advancer.generator.InstrumentationUtils.OBJECT;
import static jayield.advancer.generator.InstrumentationUtils.OBJECT_DELIMITER;
import static jayield.advancer.generator.InstrumentationUtils.VOID;


public class InitializeMethodGenerator implements Opcodes {

    private final SerializedLambda lambda;
    private final ClassVisitor target;
    private final String targetName;

    InitializeMethodGenerator(SerializedLambda lambda, ClassVisitor target, String targetName) {
        this.lambda = lambda;
        this.target = target;
        this.targetName = targetName;
    }


    public static void generateInitializeMethod(SerializedLambda lambda, ClassVisitor target, String targetName) {
        new InitializeMethodGenerator(lambda, target, targetName).generate();
    }

    private void generate() {
        MethodVisitor mv = target.visitMethod(ACC_PUBLIC, INITIALIZE, INITIALIZE_METHOD_DESCRIPTOR, null, null);

        mv.visitCode();
        mv.visitTypeInsn(NEW, targetName);
        mv.visitInsn(DUP);
        loadCapturedArguments(mv);
        generateAdvancer(mv);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(getMaxStack(), getMaxLocals());
        mv.visitEnd();

    }

    private void loadCapturedArguments(MethodVisitor mv) {
        Consumer<MethodVisitor>[] castersToArgumentType = getCapturedArgumentTypeCasters();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 2); // initialize counter to
        for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
            mv.visitVarInsn(ALOAD, 1); // load SerializedLambda
            mv.visitVarInsn(ILOAD, 2); // load Counter
            mv.visitMethodInsn(INVOKEVIRTUAL,
                               SERIALIZED_LAMBDA,
                               GET_CAPTURED_ARG_METHOD_NAME,
                               GET_CAPTURED_ARG_METHOD_SIGNATURE,
                               false);
            castersToArgumentType[i].accept(mv); // cast argument to the method's declared type
            if ((i + 1) < lambda.getCapturedArgCount()) {
                mv.visitIincInsn(2, 1);
            }
        }
    }

    private void generateAdvancer(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESPECIAL, targetName, CONSTRUCTOR_METHOD_NAME, getSignatureFromLambda(), false);
    }

    private int getMaxStack() {
        return lambda.getCapturedArgCount() + 1;
    }

    private int getMaxLocals() {
        return lambda.getCapturedArgCount() + 1;
    }

    @SuppressWarnings("unchecked")
    private Consumer<MethodVisitor>[] getCapturedArgumentTypeCasters() {
        Consumer<MethodVisitor>[] result = new Consumer[lambda.getCapturedArgCount()];
        String[] signatureTokens = Stream.of(Type.getArgumentTypes(lambda.getImplMethodSignature()))
                                         .map(Type::getDescriptor)
                                         .toArray(String[]::new);
        for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
            if (signatureTokens[i].length() > 1) {
                result[i] = typeCast(signatureTokens[i]);
            } else {
                result[i] = PRIMITIVE_TYPE_MAPPER.get(signatureTokens[i].charAt(0));
            }
        }
        return result;
    }

    private String getSignatureFromLambda() {
        String signature = lambda.getImplMethodSignature();
        String parameters = signature.substring(0, signature.lastIndexOf(OBJECT));
        return format("%s%c%c", parameters, METHOD_PARAMETERS_END, VOID);
    }

    private Consumer<MethodVisitor> typeCast(String token) {
        return mv -> mv.visitTypeInsn(CHECKCAST, getTreatedToken(token));
    }

    private String getTreatedToken(String token) {
        if(token.charAt(0) == ARRAY) {
            return token;
        }
        return token.replace(String.valueOf(OBJECT), "")
                              .replace(String.valueOf(OBJECT_DELIMITER), "");
    }
}
