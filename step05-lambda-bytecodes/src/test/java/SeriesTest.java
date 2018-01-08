import jayield.lite.Advancer;
import jayield.lite.Series;
import jayield.lite.Traversable;
import jayield.lite.Yield;
import jayield.lite.boxes.BoolBox;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SeriesTest {


    @Test
    public void traverseWithThenTryAdvance(){
        Integer[] first = new Integer[1];
        Series.iterate(1, i -> i)
                .traverseWith(source -> (Traversable<Integer>)  yield -> source.traverse(item -> yield.ret(item * 2)))
        .tryAdvance(item -> first[0] = item);
        Assert.assertEquals(first[0].intValue(), 2);
    }

    @Test
    public void traverseWithThenAdvanceWith(){
        List<Integer> list= new ArrayList<>();
        Integer[] val = new Integer[] {1};
        final BoolBox passed = new BoolBox();
        Series.iterate(val[0], i -> val[0] = val[0] + 1)
                .traverseWith(source -> (Traversable<Integer>)  yield -> source.traverse(item -> {
                    yield.ret(item * 2);
                }))
                .takeWhile(i -> i <= 10)
                .traverse(list::add);
        Assert.assertEquals(list.size(), 5);
    }
}
