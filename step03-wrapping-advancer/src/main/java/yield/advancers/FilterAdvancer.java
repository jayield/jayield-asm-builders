package yield.advancers;

import yield.SimpleSeries;
import yield.Yield;
import java.util.function.Predicate;

public class FilterAdvancer<T> implements Advancer<T> {

    private final SimpleSeries<T> source;
    private final WrappingAdvancer<T> wrapper;
    private final Predicate<T> p;

    public FilterAdvancer(SimpleSeries<T> source, Predicate<T> p) {
        this.source = source;
        this.p = p;
        this.wrapper = new WrappingAdvancer<>(source);
    }

    @Override
    public boolean tryAdvance(Yield<T> yield) {
        return this.wrapper.tryAdvance(e -> {
            if(this.p.test(e))
                yield.ret(e);
        });
    }
}
