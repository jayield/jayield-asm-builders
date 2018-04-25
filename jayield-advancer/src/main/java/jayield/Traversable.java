package jayield;

import jayield.advancer.Advancer;
import jayield.advancer.AdvancerIterator;
import jayield.traversable.TraversableEmpty;
import jayield.traversable.TraversableIterate;
import jayield.traversable.TraversableOf;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.UnaryOperator;


public interface Traversable<T> extends Serializable {


    void traverse(Yield<T> yield);

    static <T> Traversable<T> iterate(T seed, UnaryOperator<T> f) {
        return new TraversableIterate<>(seed, f);
    }

    default <R> Traversable<R> traverseWith(Function<Traversable<T>, Traversable<R>> then) {
        return then.apply(this);
    }

    static <T> Traversable<T> of(T... data) {
        return new TraversableOf<>(data);
    }

    static <T> Traversable<T> empty() {
        return new TraversableEmpty<>();
    }

    default Advancer<T> advancer() {
        throw new UnsupportedOperationException();
    }

    default Iterator<T> iterator() {
        return new AdvancerIterator<>(advancer());
    }

}
