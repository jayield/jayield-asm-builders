package jayield.advancer;

import org.jayield.boxes.IntBox;
import org.jayield.Query;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static jayield.advancer.TestUtils.makeAssertions;



@Ignore
public class StreamFunctionsTests {

    @Test
    public void testFilter() {
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        Query<Integer> traverser = Query.of(input)
                .then(source -> yield -> source.traverse(item -> {
                    if (item % 2 == 0) {
                        yield.ret(item);
                    }
                }));

        makeAssertions(expected, Advancer.from(traverser));
    }

    @Test
    public void testMap() {
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4};
        int times = 2;

        Query<Integer> traverser = Query.of(input)
                .then(source -> yield -> source.traverse(item -> yield.ret(item * times)));

        makeAssertions(expected, Advancer.from(traverser));
    }

    @Test
    public void testSkip() {
        List<Integer> expected = Arrays.asList(2, 3);
        Integer[] input = new Integer[]{0, 1, 2, 3};
        final IntBox box = new IntBox(-1);
        int threshold = 2;

        Query<Integer> traverser = Query.of(input)
                .then(source -> yield -> source.traverse(item -> {
                    if (box.inc() >= threshold) {
                        yield.ret(item);
                    }
                }));

        makeAssertions(expected, Advancer.from(traverser));
    }

    @Test
    public void testLimit() {
        List<Integer> expected = Arrays.asList(0, 1);
        Integer[] input = new Integer[]{0, 1, 2, 3};
        final IntBox box = new IntBox(-1);
        int threshold = 2;

        Query<Integer> traverser = Query.of(input)
                                                      .then(source -> yield -> source.traverse(item -> {
                                                          if (box.inc() < threshold) {
                                                              yield.ret(item);
                                                          }
                                                      }));

        makeAssertions(expected, Advancer.from(traverser));
    }

    @Test
    public void testPeek() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        List<Integer> peekExpectation = Arrays.asList(1, 1, 1, 1);
        List<Integer> peekActual = new ArrayList<>();
        Integer[] input = new Integer[]{0, 1, 2, 3};
        Consumer<Integer> action = peekActual::add;


        Query<Integer> traverser = Query.of(input)
                                                      .then(source -> yield -> source.traverse(item -> {
                                                          action.accept(1);
                                                          yield.ret(item);
                                                      }));

        makeAssertions(expected, Advancer.from(traverser));
        Assert.assertEquals(peekExpectation.size(), peekActual.size());
        for (int i = 0; i < peekActual.size(); i++) {
            Assert.assertEquals(peekExpectation.get(i), peekActual.get(i));
        }
    }

    @Test
    public void testDistinct() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4, 5);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0};
        final HashSet<Integer> cache = new HashSet<>();

        Query<Integer> traverser = Query.of(input)
                .then(source -> yield -> source.traverse(item -> {
                    if (cache.add(item)) {
                        yield.ret(item);
                    }
                }));

        makeAssertions(expected, Advancer.from(traverser));
    }

    @Ignore
    @Test
    public void testFlatmap() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        Integer[][] input = new Integer[][]{{0, 1}, {2, 3}};

        Function<Integer[], Query<Integer>> mapper = Query::of;

        Query<Integer> traverser = Query.of(input)
                .then(source -> yield ->
                        source.traverse(item -> mapper.apply(item).traverse(yield)));

        makeAssertions(expected, Advancer.from(traverser));
    }

}
