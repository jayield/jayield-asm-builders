package jayield.advancer;

import org.jayield.Query;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FromQueryOperationTests {

    @Test
    public void testOf() {
        List<Integer> yielded = new ArrayList<>();
        List<Integer> expected = asList(1, 2, 3);
        Query<Integer> of = Query.of(1, 2, 3);
        Advancer<Integer> advancer = Advancer.from(of);

        assertTrue(advancer.tryAdvance(yielded::add));
        assertTrue(advancer.tryAdvance(yielded::add));
        assertTrue(advancer.tryAdvance(yielded::add));
        assertFalse(advancer.tryAdvance(yielded::add));
        assertEquals(expected.size(), yielded.size());
        assertEquals(expected.get(0), yielded.get(0));
        assertEquals(expected.get(1), yielded.get(1));
        assertEquals(expected.get(2), yielded.get(2));
    }

    @Test
    public void testIterate() {
        List<Integer> yielded = new ArrayList<>();
        List<Integer> expected = asList(1, 2);
        Query<Integer> of = Query.iterate(1, i -> i + 1);
        Advancer<Integer> advancer = Advancer.from(of);

        assertTrue(advancer.tryAdvance(yielded::add));
        assertTrue(advancer.tryAdvance(yielded::add));
        assertEquals(expected.size(), yielded.size());
        assertEquals(expected.get(0), yielded.get(0));
        assertEquals(expected.get(1), yielded.get(1));
        assertTrue(advancer.tryAdvance(yielded::add));
    }

    @Test
    public void testMap() {
        Query<Integer> query = Query.of("1", "22", "333")
                                    .map(String::length);
        List<Integer> expected = asList(1, 2, 3);
        List<Integer> actual = new ArrayList<>();
        Advancer<Integer> advancer = Advancer.from(query);

        assertEquals(0, actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertTrue(advancer.tryAdvance(actual::add));
            assertEquals(i + 1, actual.size());
            assertEquals(expected.get(i), actual.get(i));
        }

        assertEquals(expected.size(), actual.size());
    }

    @Ignore
    @Test
    public void testFilter() {
        fail();
    }

    @Ignore
    @Test
    public void testSkip() {
        fail();
    }

    @Ignore
    @Test
    public void testLimit() {
        fail();
    }

    @Ignore
    @Test
    public void testDistinct() {
        fail();
    }

    @Ignore
    @Test
    public void testFlatMap() {
        fail();
    }

    @Ignore
    @Test
    public void testTakeWhile() {
        fail();
    }

    @Ignore
    @Test
    public void testThen() {
        fail();
    }

    @Ignore
    @Test
    public void testPeek() {
        List<Integer> peeked = new ArrayList<>();
        List<Integer> yielded = new ArrayList<>();
        List<Integer> expected = asList(1);
        final int expectedSize = 2;
        Query<Integer> peek = Query.of(1, 2)
                                   .peek(peeked::add);

        Advancer<Integer> advancer = Advancer.from(peek);

        assertTrue(advancer.tryAdvance(yielded::add));

        assertEquals(expected.size(), yielded.size());
        assertEquals(expected.get(0), yielded.get(0));

        assertEquals(expected.size(), peeked.size());
        assertEquals(expected.get(0), peeked.get(0));

        assertTrue(advancer.tryAdvance(yielded::add));

        assertEquals(peeked.size(), yielded.size());
        assertEquals(peeked.get(0), yielded.get(0));
        assertEquals(peeked.get(1), yielded.get(2));

        assertFalse(advancer.tryAdvance(yielded::add));
        assertEquals(expectedSize, yielded.size());
        assertEquals(expectedSize, peeked.size());

    }
}
