import jayield.lite.Series;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorTests {

    private static int[] state = {0};


    @Test
    public void testSimpleGenerator() {
        List<Integer> actual = new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3);
        int step = 0;

        Series<Integer> series = Series.empty()
                .traverseWith(source -> yield -> {
                    int n = 0;
                    String template = "Executing for %d";
                    System.out.println(String.format(template, n));
                    yield.ret(n++);
                    System.out.println(String.format(template, n));
                    yield.ret(n++);
                    System.out.println(String.format(template, n));
                    yield.ret(n++);
                    System.out.println(String.format(template, n));
                    yield.ret(n++);
                });


        System.out.println("Executing");
        while (series.tryAdvance(actual::add)) {
            System.out.println("Executed");
            step++;
            Assert.assertEquals(step, actual.size());
            if (step < 3)
                System.out.println("Executing");
        }
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
