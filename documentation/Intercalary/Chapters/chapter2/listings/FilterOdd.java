import com.google.common.collect.FluentIterable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.collect.FluentIterable.from;

public class FilterOdd<T> implements Iterator<T> {
    public static  <T> FluentIterable<T> filterOdd(FluentIterable<T> source) {
        return from(() -> new FilterOdd<>(source.iterator()));
    }

    private final Iterator<T> source;
    private T current;

    public FilterOdd(Iterator<T> source){
        this.source = source;
    }

    @Override
    public boolean hasNext() {
        if(current == null && source.hasNext()) {
            source.next(); // ignore even
            if(source.hasNext()) {
                current = source.next(); // get odd
            }
        }
        return current != null;
    }

    @Override
    public T next() {
        if(hasNext()){
            T aux = current;
            current = null;
            return aux;
        } else {
            throw new NoSuchElementException();
        }
    }
}
