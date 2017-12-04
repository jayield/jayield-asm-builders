package visitors;

import jayield.lite.Series;
import jayield.lite.Yield;

public interface YieldWrapper<T> {

    void tryAdvanceWrapper(Series<T> s, Yield<T> y);
}
