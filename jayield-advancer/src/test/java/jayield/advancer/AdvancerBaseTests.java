package jayield.advancer;

import org.jayield.Query;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static jayield.advancer.TestUtils.makeAssertions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AdvancerBaseTests {

    @Ignore
    @Test
    public void testIndividuallyDistinctCount() {
        String[] expected = {"a", "x", "v", "d", "g", "j", "y", "r", "w", "e"};
        String[] arrange =
                {"a", "x", "v", "d", "g", "x", "j", "x", "y", "r", "y", "w", "y", "a", "e"};
        Supplier<Query<String>> sup = () -> Query.of(arrange).distinct();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(Advancer.iterator(sup.get()).next(), expected[i]);
        }
    }

    @Ignore
    @Test
    public void testIndividuallyFirstOnEmpty() {
        String[] arrange = {};
        boolean hasNext = Advancer.iterator(Query.of(arrange))
                                  .hasNext();
        assertTrue(!hasNext);
    }

    @Ignore
    @Test
    public void testIndividuallyFlatMap() {
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Integer[] arrange = {2, 5, 8};
        Query<Integer> query = Query
                .of(arrange)
                .flatMap(nr -> Query.of(nr - 1, nr, nr + 1));
        makeAssertions(expected, Advancer.from(query));
    }

    @Ignore
    @Test
    public void testIndividuallyIterateLimit() {
        final int LIMIT = 7;
        Query<Integer> query = Query
                .iterate(1, n -> n + 2)
                .limit(LIMIT);
        Iterator<Integer> iter = Advancer.iterator(query);
        for (int i = 0; i < LIMIT - 1; i++)
            iter.next();
        int actual = iter.next();
        assertEquals(13, actual);
    }

    @Ignore
    @Test
    public void testIndividuallyMapFilter() {
        Integer[] arrange = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Query<String> nrs = Query.of(arrange)
                                 .filter(n -> n % 2 != 0)
                                 .map(Object::toString)
                                 .skip(2);
        String actual = Advancer.iterator(nrs)
                                .next();
        assertEquals(actual, "5");
    }


    @Ignore
    @Test
    public void testIndividuallyPeek() {
        Integer[] arrange = {1, 2, 3};
        List<Integer> actual = new ArrayList<>();
        Query<Integer> query = Query.of(arrange)
                                    .peek(item -> actual.add(item * 2));
        int value = Advancer.iterator(query)
                            .next();
        assertEquals(value, 1);
        assertEquals(actual.size(), 1);
        assertEquals(actual.get(0).intValue(), 2);
    }


    @Ignore
    @Test
    public void testIndividuallyTakeWhileCount() {
        String[] arrange = {"a", "x", "v"};
        List<String> helper = asList(arrange);
        List<String> actual = new ArrayList<>();
        Query<String> query = Query.of(arrange).takeWhile(item -> helper.indexOf(item) % 2 == 0)
                                   .peek(actual::add);
        Advancer<String> adv = Advancer.from(query);
        int count = 0;
        while (adv.tryAdvance(item -> {
        })) {
            count++;
        }
        assertEquals(count, 1);
        assertEquals(actual.size(), 1);
        assertFalse(actual.containsAll(asList("a", "x", "v")));
        assertEquals(actual.get(0), "a");
    }
}
