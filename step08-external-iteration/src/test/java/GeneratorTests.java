import jayield.lite.Advancer;
import jayield.lite.Traversable;
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
        int[] n = new int[]{0};

        Traversable<Integer> traversable = Traversable.<Integer>empty()
                .traverseWith(source -> yield -> {
                    String template = "Executing for %d";
                    System.out.println(String.format(template, n[0]));
                    yield.ret(n[0]++);
                    System.out.println(String.format(template, n[0]));
                    yield.ret(n[0]++);
                    System.out.println(String.format(template, n[0]));
                    yield.ret(n[0]++);
                    System.out.println(String.format(template, n[0]));
                    yield.ret(n[0]++);
                });


        System.out.println("Executing");
        Advancer<Integer> advancer = traversable.advancer();
        while (advancer.tryAdvance(actual::add)) {
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
