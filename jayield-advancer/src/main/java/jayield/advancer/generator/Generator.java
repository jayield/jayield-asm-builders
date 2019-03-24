package jayield.advancer.generator;

import jayield.advancer.Advancer;
import jayield.advancer.generator.classloader.ByteArrayClassLoader;
import jayield.advancer.generator.visitor.traverser.TraverserVisitor;
import jayield.advancer.generator.wrapper.Initializable;
import org.jayield.Traverser;
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
import static jayield.advancer.generator.InstrumentationUtils.getCaller;
import static jayield.advancer.generator.InstrumentationUtils.getClassName;
import static jayield.advancer.generator.InstrumentationUtils.getOutputPath;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

public class Generator {

    private static final String WRITE_REPLACE_METHOD_NAME = "writeReplace";

    public static <R> Advancer<R> generateAdvancer(Traverser<R> source) {
        try {
            SerializedLambda lambda = getSerializedLambda(source);
            byte[] bytecode = generateAdvancerClassByteCode(lambda);
            debug(true, lambda, bytecode);
            Class<?> klass = loadGeneratedClass(lambda, bytecode);
            return getAdvancer(klass, lambda);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SerializedLambda getSerializedLambda(Serializable lambda) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = lambda.getClass().getDeclaredMethod(WRITE_REPLACE_METHOD_NAME);
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(lambda);
    }

    public static byte[] generateAdvancerClassByteCode(SerializedLambda lambda) throws IOException {
        ClassReader traversableClassReader = new ClassReader(lambda.getImplClass());
        ClassWriter advancerClassWriter = new ClassWriter(COMPUTE_FRAMES);
        TraverserVisitor visitor = new TraverserVisitor(advancerClassWriter, lambda);

        traversableClassReader.accept(visitor, 0);
        return advancerClassWriter.toByteArray();
    }

    public static void debug(boolean enabled, SerializedLambda lambda, byte[] bytecode) throws Exception {
        if(enabled) {
            printLambdaInfo(lambda);
        }
        String filename = getGeneratedFilename(lambda, getOutputPath());
        writeClassToFile(filename, bytecode);
        debugASM(enabled, filename);
    }

    private static void printLambdaInfo(SerializedLambda lambda) {
        System.out.println(format(" Lambda: %s",lambda));
        System.out.println(format("     Captured Arguments: %d", lambda.getCapturedArgCount()));
        for (int i = 0; i < lambda.getCapturedArgCount(); i++) {
            System.out.println(format("         Captured Argument at index (%d): %s", i, lambda.getCapturedArg(i)));
            Class<?> klass = lambda.getCapturedArg(i).getClass();
            System.out.println(format("             Class: %s", klass.getName()));
            Class<?>[] interfaces = klass.getInterfaces();
            if(interfaces.length > 0){
            System.out.println("             Interfaces:");
                for (int j = 0; j < interfaces.length; j++) {
                    System.out.println(format("                 %s", interfaces[j].getName()));
                }

            }
            System.out.println();
        }
        System.out.println(format("     Signature: %s", lambda.getImplMethodSignature()));
        System.out.println("\n#\n#\n#\nPRINT END\n");
    }

    @SuppressWarnings("unchecked")
    public static <R> Advancer<R> getAdvancer(Class<?> generatedClass,
                                               SerializedLambda lambda) throws IllegalAccessException, InstantiationException {
        Initializable<R> initializable = (Initializable<R>) generatedClass.newInstance();
        return initializable.initialize(lambda);
    }

    public static Class<?> loadGeneratedClass(SerializedLambda lambda, byte[] bytecode) {
        return ByteArrayClassLoader.load(getClassName(lambda).replace('/','.'), bytecode);
    }

    public static String getGeneratedFilename(SerializedLambda lambda, String outPath) {
        if (outPath.endsWith("/")) {
            return format("%s%s.class",
                          outPath,
                          removeInvalidChars(getClassName(lambda)));
        }
        return format("%s./%s.class",
                      outPath,
                      removeInvalidChars(getClassName(lambda)));
    }

    public static void writeClassToFile(String filename, byte[] targetBytes) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(targetBytes);
            fos.close();
        } catch (IOException e) {
            System.out.println(filename);
            e.printStackTrace();
        }
    }

    private static String removeInvalidChars(String token) {
        return token
                .replace("<", "")
                .replace("*", "")
                .replace(">", "");
    }
}
