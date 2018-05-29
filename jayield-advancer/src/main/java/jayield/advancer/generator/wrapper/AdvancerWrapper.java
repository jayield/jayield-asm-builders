package jayield.advancer.generator.wrapper;

import jayield.advancer.Advancer;
import org.jayield.Yield;
import org.jayield.boxes.BoolBox;

public class AdvancerWrapper<T> implements Advancer<T> {


    private final Advancer<T> source;
    private final BoolBox hasElement;

    public AdvancerWrapper(Advancer<T> source) {
        this.hasElement = new BoolBox();
        this.source = source;
    }

    @Override
    public boolean tryAdvance(Yield<T> yield) {
        hasElement.reset();
        Yield<T> wrapper = new YieldWrapper<>(yield, hasElement);
        while (hasElement.isFalse() && source.tryAdvance(wrapper)) ;
        return hasElement.isTrue();
    }
}
