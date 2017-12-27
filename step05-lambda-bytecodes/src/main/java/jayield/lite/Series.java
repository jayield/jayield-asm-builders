package jayield.lite;

import jayield.lite.boxes.BoolBox;
import loaders.ByteArrayClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import visitors.CustomClassVisitor;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class Series<T> {

    private final Traversable<T> bulk;
    private final Advancer<T> advancer;

    private static final Series EMPTY = new Series(
            yield -> {
            },
            yield -> false
    );

    public final void traverse(Yield<T> yield) {
        bulk.traverse(yield);
    }

    public final boolean tryAdvance(Yield<T> yield) {
        return advancer.tryAdvance(yield);
    }

    public static <U> Series<U> empty() {
        return EMPTY;
    }

    public Series(Traversable<T> bulk, Advancer<T> advancer) {
        this.bulk = bulk;
        this.advancer = advancer;
    }

    public <R> Series<R> advanceWith(Function<Series<T>, Advancer<R>> then) {
        Advancer<R> a = then.apply(this);
        Traversable<R> b = yield -> {while(a.tryAdvance(yield)){}};
        return new Series<>(b, a);
    }

    public <R> Series<R> traverseWith(Function<Series<T>, Traversable<R>> then) {
        Traversable<R> b = then.apply(this);


        Advancer<R> ta = inspect(b);
        return new Series<>(b, ta);
    }

    private <R> Advancer<R> inspect(Traversable<R> b) {
        try {
            SerializedLambda lambda = getSerializedLambda(b);
            String outPath = Series.class.getProtectionDomain().getCodeSource().getLocation().getPath(); // get path
            ClassReader cr = new ClassReader(lambda.getImplClass()); // "App"
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            CustomClassVisitor ccv = new CustomClassVisitor(cw, lambda.getImplMethodName()); // lambda generated name
            cr.accept(ccv, 0);
            FileOutputStream fos = new FileOutputStream(outPath + "./" + lambda.getImplMethodName() + ".class");
            byte[] targetBytes = cw.toByteArray();
            fos.write(targetBytes);
            fos.close();
            Object[] arguments = getCapturedArguments(lambda);
            Class<?>[] capturedArgumentClasses = new Class<?>[arguments.length];
            Class<?> newClass = ByteArrayClassLoader
                    .load(lambda.getImplMethodName(), targetBytes);
            Method[] methods = newClass.getDeclaredMethods();
            Method[] method = new Method[1];
            for(Method m : methods){
                if(m.getName().equals(lambda.getImplMethodName())){
                    method[0] = m;
                }
            }
            final BoolBox box = new BoolBox();
            return y -> {
                box.reset();
                Yield<R> wrap = wr -> {
                    y.ret(wr);
                    box.set();
                };
                arguments[arguments.length - 1] = wrap;
                try {
                    while(box.isFalse() && (Boolean) method[0].invoke(arguments));
                    return box.isTrue();
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    Object[] getCapturedArguments(SerializedLambda lambda){
        Object[] args = new Object[lambda.getCapturedArgCount() + 1];
        for(int i = 0; i < args.length - 1; i++) {
            args[i] = lambda.getCapturedArg(i);
        }
        return args;
    }

    SerializedLambda getSerializedLambda(Serializable lambda) throws Exception {
        final Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(lambda);
    }

}
