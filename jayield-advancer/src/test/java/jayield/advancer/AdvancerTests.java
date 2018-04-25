package jayield.advancer;

import jayield.Traversable;
import jayield.boxes.IntBox;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jayield.advancer.TestUtils.makeAssertions;

public class AdvancerTests {

    @Test
    public void testAdvancingElementByElement() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4);
        Integer[] input = new Integer[]{0, 1, 2, 3, 4};

        Traversable<Integer> traversable = Traversable.of(input);

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testAdvancerAsValueGenerator() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int[] n = new int[]{0};

        Traversable<Integer> traversable = Traversable.<Integer>empty()
                .traverseWith(source -> yield -> {
                    yield.ret(n[0]++);
                    yield.ret(n[0]++);
                    yield.ret(n[0]++);
                    yield.ret(n[0]++);
                });

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testAdvancerAsValueGeneratorInCycle() {
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int[] n = new int[]{0};
        int limit = 4;

        Traversable<Integer> traversable = Traversable.<Integer>empty()
                .traverseWith(source -> yield -> {
                    while (n[0] < limit) {
                        yield.ret(n[0]++);
                    }
                });

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testTraverseWithThenTryAdvance() {
        Integer[] first = new Integer[1];

        Traversable.iterate(1, i -> i)
                .<Integer>traverseWith(source -> yield -> source.traverse(item -> yield.ret(item * 2)))
                .advancer()
                .tryAdvance(item -> first[0] = item);

        Assert.assertEquals(2, first[0].intValue());
    }

    @Test
    public void testDuplicate() {
        List<Integer> expected = Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3);
        Integer[] input = new Integer[]{0, 1, 2, 3};

        Traversable<Integer> traversable = Traversable.of(input)
                                                      .traverseWith(source -> yield -> source.traverse(item -> {
                                                          yield.ret(item);
                                                          yield.ret(item);
                                                      }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testTriplicate() {
        List<Integer> expected = Arrays.asList(0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3);
        Integer[] input = new Integer[]{0, 1, 2, 3};

        Traversable<Integer> traversable = Traversable.of(input)
                                                      .traverseWith(source -> yield -> source.traverse(item -> {
                                                          yield.ret(item);
                                                          yield.ret(item);
                                                          yield.ret(item);
                                                      }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testTriplicateWithFor() {
        List<Integer> expected = Arrays.asList(0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3);
        Integer[] input = new Integer[]{0, 1, 2, 3};
        int limit = 3;

        Traversable<Integer> traversable = Traversable.of(input)
                                                      .traverseWith(source -> yield -> source.traverse(item -> {
                                                          for (int i = 0; i < limit; i++) {
                                                              yield.ret(item);

                                                          }
                                                      }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testConditionalDuplicate() {
        List<Integer> expected = Arrays.asList(1, 1);
        Integer[] input = new Integer[]{0, 1, 2};

        Traversable<Integer> traversable = Traversable.of(input)
                                                      .traverseWith(source -> yield -> source.traverse(item -> {
                                                          if (item == 1) {
                                                              yield.ret(item);
                                                              yield.ret(item);
                                                          }
                                                      }));

        makeAssertions(expected, traversable.advancer());
    }

    @Test
    public void testConditionalWithExternalState() {
        List<Integer> expected = Arrays.asList(1, 1);
        Integer[] input = new Integer[]{0, 1, 2};
        IntBox box = new IntBox(-1);

        Traversable<Integer> traversable = Traversable.of(input)
                                                      .traverseWith(source -> yield -> source.traverse(item -> {
                                                          if (box.inc() == 1) {
                                                              yield.ret(item);
                                                              yield.ret(item);
                                                          }
                                                      }));

        makeAssertions(expected, traversable.advancer());
    }
}
