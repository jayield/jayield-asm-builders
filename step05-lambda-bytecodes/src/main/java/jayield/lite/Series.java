package jayield.lite;

import jayield.lite.boxes.BoolBox;
import jayield.lite.boxes.Box;
import jayield.lite.boxes.IntBox;
import jdk.internal.org.objectweb.asm.util.ASMifier;
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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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

    public static <U> Series<U> iterate(U seed, UnaryOperator<U> f) {
        Traversable<U> b = yield -> {
            for(U i = seed; true; i = f.apply(i))
                yield.ret(i);
        };
        Box<U> box = new Box<>(seed, f);
        Advancer<U> a = yield -> {
                yield.ret(box.getValue());
                box.inc();
                return true;
        };
        return new Series<>(b, a);
    }

    public static <U> Series<U> of(U...data) {
        Traversable<U> b = yield -> {
            for (int i = 0; i < data.length; i++) { yield.ret(data[i]); }
        };
        IntBox index = new IntBox(-1);
        Advancer<U> a = yield -> {
            int i;
            if((i = index.inc()) < data.length) yield.ret(data[i]);
            return i < data.length;
        };
        return new Series<>(b, a);
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

    public Series<T> takeWhile(Predicate<T> predicate) {
        final BoolBox passed = new BoolBox();
        return advanceWith(src -> yield -> {
            passed.reset();
            Yield<T> takeWhile = item -> {
                if(predicate.test(item)){
                    passed.set();
                    yield.ret(item);
                }
            };
            return src.tryAdvance(takeWhile) && passed.isTrue();
        });
    }

    private <R> Advancer<R> inspect(Traversable<R> b) {
        try {
            SerializedLambda lambda = getSerializedLambda(b);
            String outPath = Series.class.getProtectionDomain().getCodeSource().getLocation().getPath(); // get path
            ClassReader cr = new ClassReader(lambda.getImplClass()); // "App"
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            CustomClassVisitor ccv = new CustomClassVisitor(cw, lambda.getImplMethodName(), lambda.getCapturingClass()); // lambda generated name
            cr.accept(ccv, 0);
            FileOutputStream fos = new FileOutputStream(outPath + "./" + lambda.getImplMethodName() + ".class");
            byte[] targetBytes = cw.toByteArray();
            fos.write(targetBytes);
            fos.close();
//            ASMifier.main(new String[]{outPath + "./" + lambda.getImplMethodName() + ".class"});
            Object[] arguments = getCapturedArguments(lambda);
            Class<?>[] capturedArgumentClasses = new Class<?>[arguments.length];
            Class<?> newClass = ByteArrayClassLoader
                    .load(lambda.getImplMethodName(), targetBytes);
            Method[] methods = newClass.getDeclaredMethods();
            Method[] method = new Method[1];
            for(Method m : methods){
                if(m.getName().equals(lambda.getImplMethodName())){
                    method[0] = m;
                    method[0].setAccessible(true);
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
                    while(box.isFalse() && (Boolean) method[0].invoke(newClass, arguments));
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
