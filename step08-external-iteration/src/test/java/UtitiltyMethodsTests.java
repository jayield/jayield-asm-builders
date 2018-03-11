import jayield.lite.Advancer;
import jayield.lite.Traversable;
import jayield.lite.boxes.IntBox;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;


public class UtitiltyMethodsTests {


    @Test
    public void traverseWithThenTryAdvance() {
        Integer[] first = new Integer[1];
        Traversable.iterate(1, i -> i)
                .traverseWith(source -> (Traversable<Integer>) yield -> source.traverse(item -> yield.ret(item * 2)))
                .advancer()
                .tryAdvance(item -> first[0] = item);
        Assert.assertEquals(2, first[0].intValue());
    }

    @Test
    public void testFilter() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int step = 0;

        Traversable<Integer> series = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
            if (item % 2 == 0) {
                yield.ret(item);
            }
        }));

        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testMap() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4};
        int times = 2;
        int step = 0;

        Traversable<Integer> series = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    yield.ret(item * times);
                }));

        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add) && actual.size() < expected.size()) {
            step++;
            Assert.assertEquals(step, actual.size());
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void simpleTest() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4};
        int step = 0;

        Traversable<Integer> series = Traversable.of(input);
        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testSkip() {
        List<Integer> actual = new ArrayList<>();
        int threshold = 8;
        List<Integer> expected = Arrays.asList(8, 9);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final IntBox box = new IntBox(-1);
        int step = 0;

        Traversable<Integer> series = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    if (box.inc() >= threshold) {
                        yield.ret(item);
                    }
                }));

        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testDistinct() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4, 5);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0};
        final HashSet<Integer> cache = new HashSet<>();
        int step = 0;

        Traversable<Integer> series = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    if (cache.add(item)) {
                        yield.ret(item);
                    }
                }));

        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testDup() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3);
        Integer[] input = new Integer[]{0, 1, 2, 3};
        int step = 0;

        Traversable<Integer> series = Traversable.of(input)
                .traverseWith(source -> yield -> source.traverse(item -> {
                    yield.ret(item);
                    yield.ret(item);
                }));

        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Ignore
    @Test
    public void testFlatmap() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        Integer[][] input = new Integer[][]{{0, 1}, {2, 3}};
        int step = 0;

        Function<Integer[], Traversable<Integer>> mapper = Traversable::of;

        Traversable<Integer> series = Traversable.of(input)
                .traverseWith(source -> yield ->
                        source.traverse(item -> mapper.apply(item).traverse(yield)));

        final Advancer<Integer> advancer = series.advancer();
        while (advancer.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }

        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

}
