package jayield.advancer.generator;

import jayield.advancer.Advancer;
import jayield.advancer.generator.classloader.ByteArrayClassLoader;
import jayield.advancer.generator.visitor.traversable.TraversableVisitor;
import jayield.advancer.generator.wrapper.Initializable;
import jayield.traversable.Traversable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;
import static jayield.advancer.generator.InstrumentationUtils.debugASM;
import static jayield.advancer.generator.InstrumentationUtils.getOutputPath;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class Generator {

    private static final String WRITE_REPLACE_METHOD_NAME = "writeReplace";

    public static <R> Advancer<R> generateAdvancer(Traversable<R> source) {
        try {
            SerializedLambda lambda = getSerializedLambdaFromTraversable(source);
            byte[] bytecode = generateAdvancerClassByteCode(lambda);
            debug(true, lambda, bytecode);
            return getAdvancer(loadGeneratedClass(lambda, bytecode), lambda);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void debug(boolean enabled, SerializedLambda lambda, byte[] bytecode) throws Exception {
        String filename = getGeneratedFilename(lambda, getOutputPath());
        writeClassToFile(filename, bytecode);
        debugASM(enabled, filename);
    }

    @SuppressWarnings("unchecked")
    private static <R> Advancer<R> getAdvancer(Class<?> generatedClass,
                                               SerializedLambda lambda) throws IllegalAccessException, InstantiationException {
        Initializable<R> initializable = (Initializable<R>) generatedClass.newInstance();
        return initializable.initialize(lambda);
    }

    private static Class<?> loadGeneratedClass(SerializedLambda lambda, byte[] bytecode) {
        return ByteArrayClassLoader.load(lambda.getImplMethodName(), bytecode);
    }

    private static byte[] generateAdvancerClassByteCode(SerializedLambda lambda) throws IOException {
        ClassReader traversableClassReader = new ClassReader(lambda.getImplClass());
        ClassWriter advancerClassWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        TraversableVisitor visitor = new TraversableVisitor(advancerClassWriter, lambda);

        traversableClassReader.accept(visitor, 0);
        return advancerClassWriter.toByteArray();
    }

    private static SerializedLambda getSerializedLambdaFromTraversable(Serializable lambda) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = lambda.getClass().getDeclaredMethod(WRITE_REPLACE_METHOD_NAME);
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(lambda);
    }


    private static String getGeneratedFilename(SerializedLambda lambda, String outPath) {
        return format("%s./%s___%s.class", outPath, getCaller(), lambda.getImplMethodName());
    }

    private static String getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            if(stackTraceElement.getClassName().equals(Traversable.class.getName()) &&
                    stackTraceElement.getMethodName().equals("advancer")) {
                return stackTrace[i + 1].getMethodName();
            }
        }

        return "not_found";
    }

    private static void writeClassToFile(String filename, byte[] targetBytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(targetBytes);
        fos.close();
    }
}
