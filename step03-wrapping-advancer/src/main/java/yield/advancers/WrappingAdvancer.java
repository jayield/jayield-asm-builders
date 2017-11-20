package yield.advancers;

import yield.SimpleSeries;
import yield.boxes.BoolBox;
import yield.Yield;

public class WrappingAdvancer<T> implements Advancer<T> {

    private BoolBox box;
    private SimpleSeries<T> source;

    public WrappingAdvancer(SimpleSeries<T> source) {
        box = new BoolBox();
        this.source = source;
    }

    @Override
    public final boolean tryAdvance(Yield<T> yield) {
        box.reset();
        while(box.isFalse() && source.tryAdvance(item ->{
            yield.ret(item);
            box.set();
        }));
        return box.isTrue();
    }
}
