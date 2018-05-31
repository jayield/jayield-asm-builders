package jayield.advancer;

import org.jayield.Query;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GenerationFromQueryInitializersTests {

    @Test
    public void testGenerationFromQueryOf() {
        Query<Integer> of = Query.of(1, 2, 3);

        Advancer<Integer> from = Advancer.from(of);

        assertNotNull(from);
    }

    @Test
    public void testGenerationFromQueryIterate() {
        Query<Integer> iterate = Query.iterate(1, i -> i);

        Advancer<Integer> from = Advancer.from(iterate);

        assertNotNull(from);

    }
}
