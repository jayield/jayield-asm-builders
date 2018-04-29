package jayield.traversable;

import jayield.advancer.Advancer;
import jayield.Yield;
import jayield.boxes.Box;

import java.util.function.UnaryOperator;

public class TraversableIterate<T> implements Traversable<T> {

    private final T seed;
    private final UnaryOperator<T> generator;

    public TraversableIterate(T seed, UnaryOperator<T> generator) {
        this.seed = seed;
        this.generator = generator;
    }

    @Override
    public void traverse(Yield<T> yield) {
        for (T i = seed; true; i = generator.apply(i)) {
            yield.ret(i);
        }
    }

    @Override
    public Advancer<T> advancer() {
        Box<T> box = new Box<>(seed, generator);
        return yield -> {
            yield.ret(box.getValue());
            box.inc();
            return true;
        };
    }
}
