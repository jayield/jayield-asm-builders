import jayield.lite.Series;
import org.objectweb.asm.Opcodes;

public class App implements Opcodes {

    public static void main(String[] args) throws Exception {
        int thirtyTwo = 32;
        String its = "its ";
        Series.empty().traverseWith(source -> yield -> source.traverse(item -> yield.ret(item)));
        /*Series.empty().traverseWith(source -> {
            int x = thirtyTwo - 1;
            return yield -> {
                System.out.println(thirtyTwo);
                int[] negative = new int[]{0, -1};
                source.traverse(item -> {
                    System.out.println(its + "hello" + negative[1]);
                    yield.ret(item);
                });
            };
        });*/
    }
}
