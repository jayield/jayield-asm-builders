package jayield.lite;

import java.io.Serializable;

public interface Yield<T> extends Serializable {
    void ret(T item);
}
