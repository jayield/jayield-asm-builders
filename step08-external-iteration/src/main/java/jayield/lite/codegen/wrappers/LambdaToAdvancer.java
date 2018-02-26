package jayield.lite.codegen.wrappers;

import jayield.lite.Advancer;

import java.lang.invoke.SerializedLambda;

public interface LambdaToAdvancer<T> {
    Advancer<T> getAdvancer(SerializedLambda source);
}
