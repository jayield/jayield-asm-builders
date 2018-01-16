package jayield.lite;

import jayield.lite.boxes.BoolBox;
import jayield.lite.boxes.Box;
import jayield.lite.boxes.IntBox;
import jdk.internal.org.objectweb.asm.util.ASMifier;
import loaders.ByteArrayClassLoader;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import visitors.CustomClassVisitor;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Series<T> {

    private static final Instrumentation instrumentation = ByteBuddyAgent.install();
    private static final String ADVANCER = "Advancer";

    private final Traversable<T> bulk;
    private final Advancer<T> advancer;

    private static final Series EMPTY = new Series(yield -> {},yield -> false);

    public final void traverse(Yield<T> yield) {
        bulk.traverse(yield);
    }

    public final boolean tryAdvance(Yield<T> yield) {
        return advancer.tryAdvance(yield);
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
            String outPath = Series.class.getProtectionDomain().getCodeSource().getLocation().getPath(); // get path
            Class<?> clazz = b.getClass();
            String newName = getNewName(clazz);
            byte[] originByteCode = getByteCodeOf(clazz);
            ClassReader cr = new ClassReader(new ByteArrayInputStream(originByteCode));
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            CustomClassVisitor ccv = new CustomClassVisitor(cw, newName, clazz.getName()); // lambda generated name
            cr.accept(ccv, 0);
            byte[] targetBytes = cw.toByteArray();
            FileOutputStream fosOriginal = new FileOutputStream(outPath + "./original"  + ".class");
            fosOriginal.write(originByteCode);
            fosOriginal.close();
            FileOutputStream fos = new FileOutputStream(outPath + "./" + newName + ".class");
            fos.write(targetBytes);
            fos.close();
            Object[] arguments = getCapturedArguments(b);
            ASMifier.main(new String[]{outPath + newName + ".class"});
//            ASMifier.main(new String[]{outPath + "original.class"});
            Class<?>[] capturedArgumentClasses = new Class<?>[arguments.length];
            Class<?> newClass = ByteArrayClassLoader
                    .load(newName, targetBytes);
            Method[] methods = newClass.getDeclaredMethods();
            Method[] method = new Method[1];
            for(Method m : methods){
                if(m.getName().equals("get$Lambda")){
                    method[0] = m;
                    method[0].setAccessible(true);
                }
            }
            return (Advancer<R>) method[0].invoke(newClass, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private <R> Object[] getCapturedArguments(Traversable<R> b) throws IllegalAccessException {
        Class<?> clazz = b.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Object[] args = new Object[fields.length];
        for(int i = 0 ; i <fields.length; i++){
            fields[i].setAccessible(true);
            args[i] = fields[i].get(b);
            fields[i].setAccessible(false);
        }
        return args;
    }

    private String getNewName(Class<?> clazz){
        String currentName = clazz.getName();
        return currentName.substring(0, currentName.lastIndexOf('/'))
                .replace('/', '.') + ADVANCER;
    }

    byte[] getByteCodeOf(Class<?> c) throws IOException {
        ClassFileLocator locator = ClassFileLocator.AgentBased.of(instrumentation, c);
        TypeDescription.ForLoadedType desc = new TypeDescription.ForLoadedType(c);
        ClassFileLocator.Resolution resolution = locator.locate(desc.getName());
        return resolution.resolve();
    }

}
