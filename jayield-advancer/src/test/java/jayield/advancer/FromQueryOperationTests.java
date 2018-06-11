package jayield.advancer;

import org.jayield.Query;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class FromQueryOperationTests {

    @Test
    public void testPeek() {
        List<Integer> peeked = new ArrayList<>();
        List<Integer> yielded = new ArrayList<>();
        List<Integer> expected = asList(1);
        Query<Integer> peek = Query.of(1, 2)
                                   .peek(peeked::add);

        Advancer.from(peek).tryAdvance(yielded::add);

        assertEquals(expected.size(), yielded.size());
        assertEquals(expected.get(0), yielded.get(0));

        assertEquals(expected.size(), peeked.size());
        assertEquals(expected.get(0), peeked.get(0));

    }
}
