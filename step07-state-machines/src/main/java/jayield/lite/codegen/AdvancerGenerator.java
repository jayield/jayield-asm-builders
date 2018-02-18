package jayield.lite.codegen;

import jayield.lite.Advancer;
import jayield.lite.Series;
import jayield.lite.Traversable;
import jayield.lite.codegen.visitors.clazz.LocalVariableExtractorClassVisitor;
import jayield.lite.codegen.visitors.clazz.TraversableToAdvancerVisitor;
import jayield.lite.codegen.visitors.clazz.YieldStateMachineVisitor;
import jayield.lite.codegen.wrappers.AdvancerWrapper;
import jayield.lite.codegen.wrappers.LambdaToAdvancer;
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
//        printASM(getOutputPath() + AdvancerGenerator.class.getName().replace('.','/') + ".class");
//        writeClassToFile(getGeneratedFilename(lambda,getOutputPath()), bytecode);
//        printASM(getOutputPath() + lambda.getImplMethodName() + ".class");
        Class<?> generatedClass = loadGeneratedClass(lambda, bytecode);
        return new AdvancerWrapper<>(getAdvancer(generatedClass, lambda));
    }

    @SuppressWarnings("unchecked")
    private static <R> Advancer<R> getAdvancer(Class<?> generatedClass, SerializedLambda lambda) {
        try {
            LambdaToAdvancer<R> lta = (LambdaToAdvancer<R>) generatedClass.newInstance();
            return lta.getAdvancer(lambda);
        } catch (InstantiationException | ClassCastException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Class<?> loadGeneratedClass(SerializedLambda lambda, byte[] bytecode) {
        return ByteArrayClassLoader.load(lambda.getImplMethodName(), bytecode);
    }

    private static byte[] generateAdvancerClassByteCode(SerializedLambda lambda) {
        try {
            ClassReader instrumentedClassReader = new ClassReader(lambda.getImplClass());
            ClassReader localVariableClassReader = new ClassReader(lambda.getImplClass());
            ClassWriter instrumentedClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassWriter localVariableClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            LocalVariableExtractorClassVisitor lvcv = new LocalVariableExtractorClassVisitor(localVariableClassWriter,lambda.getImplMethodName());
            localVariableClassReader.accept(lvcv, 0);

            YieldStateMachineVisitor ccv = new YieldStateMachineVisitor(instrumentedClassWriter, lambda.getImplMethodName(), lambda.getCapturingClass(), lambda, lvcv.getLocalVariables()); // lambda generated name
            instrumentedClassReader.accept(ccv, 0);
            return instrumentedClassWriter.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

    public static void printASM(String filename) {
        try {
            ASMifier.main(new String[]{"-debug", filename});
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String getOutputPath() {
        return Series.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
