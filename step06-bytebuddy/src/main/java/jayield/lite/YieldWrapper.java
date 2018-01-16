package jayield.lite;

import jayield.lite.boxes.BoolBox;

public class YieldWrapper<T> implements Yield<T> {
    private final Yield<T> base;
    private final BoolBox elementFound;

    public YieldWrapper(Yield<T> base) {
        this.base = base;
        this.elementFound = new BoolBox();
        this.elementFound.reset();
    }

    @Override
    public void ret(T item) {
        base.ret(item);
        this.elementFound.set();
    }

    public boolean hasElement() {
        return this.elementFound.isTrue();
    }


}
