import jayield.lite.Series;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorTests {


    @Test
    public void testSimpleGenerator() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int step = 0;

        Series<Integer> series = Series.empty()
                .traverseWith(source -> yield -> {
                    System.out.println("Executing for 0");
                    yield.ret(0);
                    System.out.println("Executing for 1");
                    yield.ret(1);
                    System.out.println("Executing for 2");
                    yield.ret(2);
                    System.out.println("Executing for 2");
                    yield.ret(3);
                });


        while (series.tryAdvance(actual::add)) {
            step++;
            Assert.assertEquals(step, actual.size());
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
