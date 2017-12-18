package jayield.lite;

import jdk.internal.org.objectweb.asm.util.ASMifier;
import loaders.ByteArrayClassLoader;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import visitors.CustomClassVisitor;
import visitors.YieldWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

public class Series<T> {

    private static final Instrumentation instrumentation = ByteBuddyAgent.install();

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
            String outPath = Series.class.getProtectionDomain().getCodeSource().getLocation().getPath(); // get path
            byte[] bytes = getByteCodeOf(b.getClass());
            FileOutputStream fos = new FileOutputStream(outPath + "./" + "test" + ".class");
            fos.write(bytes);
            fos.close();
            ASMifier.main(new String[]{outPath + "test" + ".class"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    byte[] getByteCodeOf(Class<?> c) throws IOException {
        ClassFileLocator locator = ClassFileLocator.AgentBased.of(instrumentation, c);
        TypeDescription.ForLoadedType desc = new TypeDescription.ForLoadedType(c);
        ClassFileLocator.Resolution resolution = locator.locate(desc.getName());
        return resolution.resolve();
    }

}
