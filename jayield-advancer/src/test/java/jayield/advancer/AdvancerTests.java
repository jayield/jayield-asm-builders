package jayield.advancer;

import org.jayield.Query;
import org.jayield.Traverser;
import org.jayield.boxes.IntBox;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jayield.advancer.Advancer.from;
import static jayield.advancer.TestUtils.makeAssertions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdvancerTests {

    @Test
    public void testAdvancerAsValueGeneratorFromQuery() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int[] n = new int[]{0};

        Query<Integer> query = Query.of().then(q -> yield -> {
            yield.ret(n[0]++);
            yield.ret(n[0]++);
            yield.ret(n[0]++);
            yield.ret(n[0]++);
        });

        makeAssertions(expected, from(query));
    }

    @Test
    public void testAdvancerAsValueGeneratorFromTraverser() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int[] n = new int[]{0};

        Traverser<Integer> traverser = yield -> {
            yield.ret(n[0]++);
            yield.ret(n[0]++);
            yield.ret(n[0]++);
            yield.ret(n[0]++);
        };

        makeAssertions(expected, from(traverser));
    }

    @Test
    public void testAdvancerAsValueGeneratorInCycle() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int[] n = new int[]{0};
        int limit = 4;

        Query<Integer> query = Query.of().then(source -> yield -> {
            while (n[0] < limit) {
                yield.ret(n[0]++);
            }
        });

        makeAssertions(expected, from(query));
    }

    @Test
    public void testAdvancerBranching() {
        List<Integer> expected = Collections.singletonList(0);
        int[] n = new int[]{0};

        Query<Integer> query = Query.of().then(source -> yield -> {
            if (n[0] % 2 == 0) {
                yield.ret(n[0]++);
            } else {
                n[0] *= 2;
                yield.ret(n[0]);
            }
        });

        makeAssertions(expected, from(query));
    }

    @Test
    public void testAdvancingElementByElement() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4);
        Integer[] input = {0, 1, 2, 3, 4};

        Query<Integer> query = Query.of(input);

        makeAssertions(expected, from(query));
    }


    @Test
    public void testConditionalDuplicate() {
        List<Integer> expected = Arrays.asList(1, 1);
        Integer[] input = {0, 1, 2};

        Query<Integer> query = Query.of(input)
                                    .then(source -> yield -> source.traverse(item -> {
                                        if (item == 1) {
                                            yield.ret(item);
                                            yield.ret(item);
                                        }
                                    }));

        makeAssertions(expected, from(query));
    }

    @Test
    public void testConditionalWithExternalState() {
        List<Integer> expected = Arrays.asList(1, 1);
        Integer[] input = {0, 1, 2};
        IntBox box = new IntBox(-1);

        Query<Integer> query = Query.of(input).then(source -> yield -> source.traverse(item -> {
            if (box.inc() == 1) {
                yield.ret(item);
                yield.ret(item);
            }
        }));

        makeAssertions(expected, from(query));
    }

    @Test
    public void testDuplicate() {
        List<Integer> expected = Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3);
        Integer[] input = {0, 1, 2, 3};

        Query<Integer> query = Query.of(input).then(source -> yield -> source.traverse(item -> {
            yield.ret(item);
            yield.ret(item);
        }));

        makeAssertions(expected, from(query));
    }

    @Test
    public void testTriplicate() {
        List<Integer> expected = Arrays.asList(0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3);
        Integer[] input = {0, 1, 2, 3};

        Query<Integer> query = Query.of(input).then(source -> yield -> source.traverse(item -> {
            yield.ret(item);
            yield.ret(item);
            yield.ret(item);
        }));

        makeAssertions(expected, from(query));
    }

    @Test
    public void testTriplicateWithFor() {
        List<Integer> expected = Arrays.asList(0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3);
        Integer[] input = {0, 1, 2, 3};
        int limit = 3;

        Query<Integer> query = Query.of(input).then(source -> yield -> source.traverse(item -> {
            for (int i = 0; i < limit; i++) {
                yield.ret(item);
            }
        }));

        makeAssertions(expected, from(query));
    }

    @Test
    public void testTryAdvance() {
        Integer[] first = new Integer[1];

        Query<Integer> query = Query.iterate(1, i -> i)
                                    .then(source -> yield -> source.traverse(item -> yield.ret(item * 2)));

        from(query).tryAdvance(item -> first[0] = item);

        assertNotNull(first[0]);
        assertEquals(2, first[0].intValue());
    }
}
