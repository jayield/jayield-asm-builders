import jayield.lite.Series;
import org.objectweb.asm.Opcodes;

public class App implements Opcodes {

    public static void main(String[] args) throws Exception {
        int thirtyTwo = 32;
        Integer thirtyOne = 31;
        String its = " its ";
        Series<Integer> series = Series.of(1, 2, 3).traverseWith(source -> yield -> source.traverse(item -> yield.ret(item)));
/*        Series<Integer> series = Series.of(1, 2, 3).traverseWith( (Series<Integer> source) -> {
            int x = thirtyTwo - 1;
            return (Yield<Integer> yield) -> {
                System.out.println(thirtyTwo + thirtyOne);
                int[] negative = new int[]{0, -1};
                source.traverse((Integer item) -> {
                    System.out.println(item + its + "hello" + negative[1]);
                    yield.ret(item);
                });
            };
        });*/
        series.tryAdvance(item -> System.out.println(item * item));
    }
}
