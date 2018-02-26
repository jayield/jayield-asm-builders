package jayield.lite.traversables;

import jayield.lite.Advancer;
import jayield.lite.Traversable;
import jayield.lite.Yield;

public class TraversableEmpty<T> implements Traversable<T> {

    @Override
    public void traverse(Yield<T> yield) {}

    @Override
    public Advancer<T> advancer() {
        return yield -> false;
    }
}
