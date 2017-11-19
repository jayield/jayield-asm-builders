package yield.traversers;

import yield.SimpleSeries;
import yield.Yield;

import java.util.function.Predicate;

public class FilterTraversable<T> implements Traversable<T> {


    private final SimpleSeries<T> source;
    private final Predicate<T> p;

    public FilterTraversable(SimpleSeries<T> source, Predicate<T> p) {

        this.source = source;
        this.p = p;
    }

    @Override
    public void traverse(Yield<T> yield) {
        source.traverse(e -> {
            if(this.p.test(e))
                yield.ret(e);
        });
    }
}
