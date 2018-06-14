import com.google.common.collect.FluentIterable;

import static com.google.common.collect.FluentIterable.from;

public class GuavaExample {
    public FluentIterable<String> getOrangeFruits(Basket basket) {
        return from(basket.fruits)
                .filter(fruit -> fruit.color.equals("orange"))
                .transform(fruit -> fruit.name);
    }
}