package jayield.lite.codegen;

import jayield.lite.Advancer;
import jayield.lite.Series;
import jayield.lite.Traversable;
import jayield.lite.Yield;
import jayield.lite.boxes.BoolBox;
import jayield.lite.codegen.visitors.clazz.TraversableToAdvancerVisitor;
import jdk.internal.org.objectweb.asm.util.ASMifier;
import loaders.ByteArrayClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AdvancerGenerator {

    public static <R> Advancer<R> gernerateAdvancer(Traversable<R> source) {
        SerializedLambda lambda = getSerializedLambdaFromTraversable(source);
        byte[] bytecode = generateAdvancerClassByteCode(lambda);
        writeClassToFile(getGeneratedFilename(lambda, getOutputPath()), bytecode);
//        printASM(getGeneratedFilename(lambda, getOutputPath()));
        printASM(getOutputPath() + AdvancerGenerator.class.getName().replace('.', '\\') + ".class");
        Class<?> generatedClass = loadGeneratedClass(lambda, bytecode);
        return getAdvancerWithWrappedYield(lambda, generatedClass, getLambdaMethod(lambda, generatedClass));

    }

    private static <R> Advancer<R> getAdvancerWithWrappedYield(SerializedLambda lambda, Class<?> generatedClass, Method lambdaMethod) {
        Object[] arguments = getCapturedArguments(lambda);
        final BoolBox box = new BoolBox();
        return y -> {
            box.reset();
            Yield<R> wrap = wr -> {
                y.ret(wr);
                box.set();
            };
            arguments[arguments.length - 1] = wrap;
            try {
                while (box.isFalse() && (Boolean) lambdaMethod.invoke(generatedClass, arguments)) ;
                return box.isTrue();
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static Method getLambdaMethod(SerializedLambda lambda, Class<?> newClass) {
        Method[] methods = newClass.getDeclaredMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(lambda.getImplMethodName())) {
                method = m;
                method.setAccessible(true);
            }
        }
        return method;
    }

    private static Class<?> loadGeneratedClass(SerializedLambda lambda, byte[] bytecode) {
        return ByteArrayClassLoader.load(lambda.getImplMethodName(), bytecode);
    }

    private static String getGeneratedFilename(SerializedLambda lambda, String outPath) {
        return outPath + "./" + lambda.getImplMethodName() + ".class";
    }

    private static void writeClassToFile(String filename, byte[] targetBytes) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(targetBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void printASM(String filename) {
        try {
            ASMifier.main(new String[]{filename});
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static byte[] generateAdvancerClassByteCode(SerializedLambda lambda) {
        try {
            ClassReader cr = new ClassReader(lambda.getImplClass());
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            TraversableToAdvancerVisitor ccv = new TraversableToAdvancerVisitor(cw, lambda.getImplMethodName(), lambda.getCapturingClass()); // lambda generated name
            cr.accept(ccv, 0);
            return cw.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String getOutputPath() {
        return Series.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    private static Object[] getCapturedArguments(SerializedLambda lambda) {
        Object[] args = new Object[lambda.getCapturedArgCount() + 1];
        for (int i = 0; i < args.length - 1; i++) {
            args[i] = lambda.getCapturedArg(i);
        }
        return args;
    }

    private static SerializedLambda getSerializedLambdaFromTraversable(Serializable lambda) {
        final Method method;
        try {
            method = lambda.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (SerializedLambda) method.invoke(lambda);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
