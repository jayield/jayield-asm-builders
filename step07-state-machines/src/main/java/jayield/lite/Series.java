package jayield.lite;

import jayield.lite.boxes.BoolBox;
import jayield.lite.boxes.Box;
import jayield.lite.boxes.IntBox;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static jayield.lite.codegen.AdvancerGenerator.gernerateAdvancer;

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
            for (U i = seed; true; i = f.apply(i))
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

    public static <U> Series<U> of(U... data) {
        Traversable<U> b = yield -> {
            for (int i = 0; i < data.length; i++) {
                yield.ret(data[i]);
            }
        };
        IntBox index = new IntBox(-1);
        Advancer<U> a = yield -> {
            int i;
            if ((i = index.inc()) < data.length) yield.ret(data[i]);
            return i < data.length;
        };
        return new Series<>(b, a);
    }

    public <R> Series<R> advanceWith(Function<Series<T>, Advancer<R>> then) {
        Advancer<R> a = then.apply(this);
        Traversable<R> b = yield -> {
            while (a.tryAdvance(yield)) {
            }
        };
        return new Series<>(b, a);
    }

    public <R> Series<R> traverseWith(Function<Series<T>, Traversable<R>> then) {
        Traversable<R> b = then.apply(this);



        Advancer<R> ta = gernerateAdvancer(b);
        return new Series<>(b, ta);
    }

    public Series<T> takeWhile(Predicate<T> predicate) {
        final BoolBox passed = new BoolBox();
        return advanceWith(src -> yield -> {
            passed.reset();
            Yield<T> takeWhile = item -> {
                if (predicate.test(item)) {
                    passed.set();
                    yield.ret(item);
                }
            };
            return src.tryAdvance(takeWhile) && passed.isTrue();
        });
    }

}
