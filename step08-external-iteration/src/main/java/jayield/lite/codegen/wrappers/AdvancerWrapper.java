package jayield.lite.codegen.wrappers;

import jayield.lite.Advancer;
import jayield.lite.Yield;
import jayield.lite.boxes.BoolBox;

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
        Yield<T> wrapper = new YieldWrapper<T>(yield, hasElement);
        while (hasElement.isFalse() && source.tryAdvance(wrapper)) ;
        return hasElement.isTrue();
    }
}
