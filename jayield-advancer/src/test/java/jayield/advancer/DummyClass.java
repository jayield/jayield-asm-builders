package jayield.advancer;

import org.jayield.Query;

public class DummyClass {
    private Query<Integer> q;

    public DummyClass() {
    }

    public DummyClass(Query<Integer> q) {
        this.q = q;
    }

    @Override
    public String toString() {
        return "DummyClass{" +
                "q=" + q +
                '}';
    }

    public void cenas(Object o) {
        Query test = (Query) o;
        System.out.println(test);
    }
}
