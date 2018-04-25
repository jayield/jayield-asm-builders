package jayield.advancer;

import jayield.Yield;

import java.io.Serializable;

public interface Advancer<T> extends Serializable {
    boolean tryAdvance(Yield<T> yield);
}
