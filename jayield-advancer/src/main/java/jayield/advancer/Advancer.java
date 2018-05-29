package jayield.advancer;

import jayield.advancer.generator.Generator;
import org.jayield.Query;
import org.jayield.Traverser;
import org.jayield.Yield;

import java.io.Serializable;
import java.util.Iterator;

public interface Advancer<T> extends Serializable {
    boolean tryAdvance(Yield<T> yield);

    static <U> Advancer<U> from(Query<U> source) {
        return from(source.getTraverser());
    }

    static  <U> Advancer<U> from(Traverser<U> source) {
        return Generator.generateAdvancer(source);
    }

    static <U> Iterator<U> iterator(Query<U> source) {
        return new AdvancerIterator<>(from(source));
    }

    static <U> Iterator<U> iterator(Traverser<U> source) {
        return new AdvancerIterator<>(from(source));
    }

    static <U> Iterator<U> iterator(Advancer<U> source) {
        return new AdvancerIterator<>(source);
    }

    default Iterator<T> iterator() {
        return new AdvancerIterator<>(this);
    }


}
