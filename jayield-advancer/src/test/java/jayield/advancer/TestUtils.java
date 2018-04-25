package jayield.advancer;

import jayield.Traversable;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;


class TestUtils {

    static <T> void makeAssertions(List<T> expected,
                                   Advancer<T> advancer) {
        List<T> actual = new ArrayList<>();
        for (int step = 1; advancer.tryAdvance(actual::add); step++) {
//            System.out.println("Executed");
            Assert.assertEquals(step, actual.size());
            if (step < expected.size()) {
//                System.out.println("Executing");
            }
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
