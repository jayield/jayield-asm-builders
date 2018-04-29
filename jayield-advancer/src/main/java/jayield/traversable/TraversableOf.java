package jayield.traversable;

import jayield.advancer.Advancer;
import jayield.Yield;
import jayield.boxes.IntBox;

public class TraversableOf<T> implements Traversable<T> {
    private final T[] data;

    public TraversableOf(T[] data) {
        this.data = data;
    }


    @Override
    public void traverse(Yield<T> yield) {
        for (T item: data) {
            yield.ret(item);
        }
    }

    @Override
    public Advancer<T> advancer() {
        IntBox index = new IntBox(-1);
        return yield -> {
            int i;
            if((i = index.inc()) < data.length) yield.ret(data[i]);
            return i < data.length;
        };
    }
}
