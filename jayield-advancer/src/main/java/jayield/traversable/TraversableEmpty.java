package jayield.traversable;

import jayield.advancer.Advancer;
import jayield.Yield;

public class TraversableEmpty<T> implements Traversable<T> {

    @Override
    public void traverse(Yield<T> yield) {}

    @Override
    public Advancer<T> advancer() {
        return yield -> false;
    }
}
