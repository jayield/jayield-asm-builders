package yield;

import yield.advancers.Advancer;
import yield.traversers.Traversable;


public class SimpleSeries<T> {

    private static final SimpleSeries EMPTY = new SimpleSeries(
            yield -> {},
            yield -> false
    );

    private final Traversable<T> bulk;
    private final Advancer<T> advancer;

    public final void traverse(Yield<T> yield) {
        bulk.traverse(yield);
    }

    public final boolean tryAdvance(Yield<T> yield) {
        return advancer.tryAdvance(yield);
    }

    public SimpleSeries(Traversable<T> bulk, Advancer<T> advancer) {
        this.bulk = bulk;
        this.advancer = advancer;
    }

    public static <U> SimpleSeries<U> empty() {
        return EMPTY;
    }

}