package jayield.advancer.generator.wrapper;

import jayield.Yield;
import jayield.boxes.BoolBox;

public class YieldWrapper<T> implements Yield<T> {

    private final Yield<T> source;
    private final BoolBox elementFound;

    public YieldWrapper(Yield<T> source, BoolBox elementFound){
        this.source = source;
        this.elementFound = elementFound;
    }

    @Override
    public void ret(T item) {
        source.ret(item);
        elementFound.set();
    }
}
