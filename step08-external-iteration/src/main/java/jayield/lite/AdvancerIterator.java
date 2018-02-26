package jayield.lite;

import java.util.Iterator;

public class AdvancerIterator<T> implements Iterator<T> {

    private final Advancer<T> advancer;
    private T current;

    public AdvancerIterator(Advancer<T> advancer) {
        this.advancer = advancer;
    }

    @Override
    public boolean hasNext() {
        return current != null || advancer.tryAdvance(item -> current = item);
    }

    @Override
    public T next() {
        if (hasNext()) {
            T aux = current;
            current = null;
            return aux;
        } else {
            return null;
        }
    }
}
