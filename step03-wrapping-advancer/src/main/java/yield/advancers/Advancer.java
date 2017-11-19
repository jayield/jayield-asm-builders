package yield.advancers;

import yield.Yield;

public interface Advancer<T> {

    boolean tryAdvance(Yield<T> yield);
}
