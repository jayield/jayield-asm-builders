package visitors;

import jayield.lite.Series;
import jayield.lite.Yield;

public interface YieldWrapper<T> {

    boolean tryAdvanceWrapper(Series<T> s, Yield<T> y);
}
