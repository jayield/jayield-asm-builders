package jayield.advancer;

import jayield.Traversable;
import jayield.boxes.IntBox;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import static jayield.advancer.TestUtils.makeAssertions;


public class StreamFunctionsTests {

    @Test
    public void testFilter() {
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        Traversable<Integer> traversable = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    if (item % 2 == 0) {
                        yield.ret(item);
                    }
                }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testMap() {
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4};
        int times = 2;

        Traversable<Integer> traversable = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    yield.ret(item * times);
                }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testSkip() {
        List<Integer> expected = Arrays.asList(2, 3);
        Integer[] input = new Integer[]{0, 1, 2, 3};
        final IntBox box = new IntBox(-1);
        int threshold = 2;

        Traversable<Integer> traversable = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    if (box.inc() >= threshold) {
                        yield.ret(item);
                    }
                }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testDistinct() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4, 5);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0};
        final HashSet<Integer> cache = new HashSet<>();

        Traversable<Integer> traversable = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    if (cache.add(item)) {
                        yield.ret(item);
                    }
                }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testFlatmap() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        Integer[][] input = new Integer[][]{{0, 1}, {2, 3}};

        Function<Integer[], Traversable<Integer>> mapper = Traversable::of;

        Traversable<Integer> traversable = Traversable.of(input)
                .traverseWith(source -> yield ->
                        source.traverse(item -> mapper.apply(item).traverse(yield)));

        makeAssertions(expected, traversable.advancer());
    }

}
