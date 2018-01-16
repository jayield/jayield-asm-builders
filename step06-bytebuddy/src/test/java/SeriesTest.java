import jayield.lite.Series;
import jayield.lite.Traversable;
import jayield.lite.boxes.IntBox;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    public void testFilter(){
        List<Integer> actual= new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Series<Integer> series = Series.of(input).traverseWith(source -> (Traversable<Integer>) yield -> source.traverse(item -> {
            if (item % 2 == 0) {
                yield.ret(item);
            }
        }));
        while(series.tryAdvance(actual::add));
        Assert.assertEquals(actual.size(), expected.size());
        for(int i = 0; i < actual.size(); i++){
            Assert.assertEquals(actual.get(i), expected.get(i));
        }
    }

    @Test
    public void testMap(){
        List<Integer> actual= new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 2, 4, 6, 8);
        Integer[] input = new Integer[] {0, 1, 2, 3, 4};
        Series<Integer> series = Series.of(input)
                .traverseWith(source -> (Traversable<Integer>) yield -> source.traverse(item -> yield.ret(item * 2)));
        while(series.tryAdvance(actual::add));
        Assert.assertEquals(actual.size(), expected.size());
        for(int i = 0; i < actual.size(); i++){
            Assert.assertEquals(actual.get(i), expected.get(i));
        }
    }

    @Test
    public void testSkip(){
        List<Integer> actual= new ArrayList<>();
        int threshold = 8;
        List<Integer> expected = Arrays.asList(8, 9);
        Integer[] input = new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final IntBox box = new IntBox(-1);
        Series<Integer> series = Series.of(input)
                .traverseWith(source -> (Traversable<Integer>) yield -> source.traverse(item -> {
                    if(box.inc() >= threshold){
                        yield.ret(item);
                    }
                }));
        while(series.tryAdvance(actual::add));
        Assert.assertEquals(actual.size(), expected.size());
        for(int i = 0; i < actual.size(); i++){
            Assert.assertEquals(actual.get(i), expected.get(i));
        }
    }


    @Test
    public void testDistinct(){
        List<Integer> actual= new ArrayList<>();
        List<Integer> expected = Arrays.asList(0, 1, 2, 3, 4, 5);
        Integer[] input = new Integer[] {0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0};
        final HashSet<Integer> cache = new HashSet<>();
        Series<Integer> series = Series.of(input)
                .traverseWith(source -> (Traversable<Integer>) yield -> source.traverse(item -> {
                    if(cache.add(item)){
                        yield.ret(item);
                        return;
                    }
                }));
        while(series.tryAdvance(actual::add));
        Assert.assertEquals(actual.size(), expected.size());
        for(int i = 0; i < actual.size(); i++){
            Assert.assertEquals(actual.get(i), expected.get(i));
        }
    }
}
