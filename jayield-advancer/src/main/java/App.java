import jayield.advancer.Advancer;
import jayield.advancer.generator.InstrumentationUtils;
import org.jayield.Query;
import org.jayield.Yield;
import org.objectweb.asm.Opcodes;

public class App<T> implements Opcodes, Advancer<T> {

    private static Query<Integer> q = Query.of(1);
    private T[] data;

    public static void main(String[] args) throws Exception {
        System.out.println(Query.class.getClassLoader());
        InstrumentationUtils.debugASM(false, InstrumentationUtils.getOutputPath() + "App.class");
    }

    private static void m1() {
        Advancer.iterator(q.getTraverser());
        Integer[] any = {1, 2, 3};
        Object[] data = (Object[])any;
        System.out.println(data);
    }

    @Override
    public boolean tryAdvance(Yield<T> yield) {
        return false;
    }

    private void m2() {
        System.out.println(data);
    }
}
