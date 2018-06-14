import one.util.streamex.StreamEx;

import java.util.Spliterator;
import java.util.function.Consumer;

import static one.util.streamex.StreamEx.of;
import static one.util.streamex.StreamEx.produce;

public class StreamExExample {

    public static <T> StreamEx<T> filterOdd(StreamEx<T> source) {
        final Consumer<T> doNothing = item -> {};
        final Spliterator<T> iterator = source.spliterator();
        return produce(action -> {
            if (!iterator.tryAdvance(doNothing))
                return false;
            return iterator.tryAdvance(action);
        });
    }

    public StreamEx<String> getOrangeFruits(Basket basket) {
        return of(basket.fruits)
                .filter(fruit -> fruit.color.equals("orange"))
                .map(fruit -> fruit.name);
    }
}
