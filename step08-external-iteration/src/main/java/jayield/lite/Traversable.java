package jayield.lite;

import jayield.lite.codegen.AdvancerGenerator;
import jayield.lite.traversables.TraversableEmpty;
import jayield.lite.traversables.TraversableIterate;
import jayield.lite.traversables.TraversableOf;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.UnaryOperator;


public interface Traversable<T> extends Serializable {


    void traverse(Yield<T> yield);

    static <T> Traversable<T> iterate(T seed, UnaryOperator<T> f) {
        return new TraversableIterate(seed, f);
    }

    default <R> Traversable<R> traverseWith(Function<Traversable<T>, Traversable<R>> then) {
        return then.apply(this);
    }

    static <T> Traversable<T> of(T... data) {
        return new TraversableOf(data);
    }

    static <T> Traversable<T> empty() {
        return new TraversableEmpty();
    }

    default Advancer<T> advancer() {
        return AdvancerGenerator.generateAdvancer(this);
    }

    default Iterator<T> iterator() {
        return new AdvancerIterator(advancer());
    }

}
