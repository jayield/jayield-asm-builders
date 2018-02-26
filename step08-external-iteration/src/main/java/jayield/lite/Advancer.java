package jayield.lite;

import java.io.Serializable;

public interface Advancer<T> extends Serializable {
    boolean tryAdvance(Yield<T> yield);
}
