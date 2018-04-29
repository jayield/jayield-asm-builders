package jayield.advancer.generator.wrapper;

import jayield.advancer.Advancer;

import java.lang.invoke.SerializedLambda;

public interface Initializable<T> {
    Advancer<T> initialize(SerializedLambda source);
}
