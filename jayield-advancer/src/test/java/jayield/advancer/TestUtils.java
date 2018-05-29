package jayield.advancer;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;


class TestUtils {

    static <T> void makeAssertions(List<T> expected, Advancer<T> advancer) {
        makeAssertions(expected, advancer, false);
    }

    static <T> void makeAssertions(List<T> expected,
                                   Advancer<T> advancer,
                                   boolean debug) {
        List<T> actual = new ArrayList<>();
        for (int step = 1; advancer.tryAdvance(actual::add); step++) {
            if (debug) {
                System.out.println("Executed");
            }
            Assert.assertEquals(step, actual.size());
            if (step < expected.size() && debug) {
                System.out.println(format("got value: %s", actual.get(step - 1)));
                System.out.println("Executing");
            }
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
