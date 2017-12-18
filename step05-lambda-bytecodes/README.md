On this step one should be able to understand how to get the bytcodes out of a lambda as well as their captured arguments.

To do so the Functional Interfaces whose bytecodes are relevant should extend Serializable.

``public interface Traversable<T>  extends Serializable{...}``

Then we obtain an instance of SerializedLambda by invoking the method writeReplace of said lambda.

```
    SerializedLambda getSerializedLambda(Serializable lambda) throws Exception {
        final Method method = lambda.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(lambda);
    }
``` 

And finally we instrument the lambda's code by running it through a custom ClassVisitor that makes the instrumented class Implement ``YieldWrapper`` and changes the name of the method that returns the lambda to ``tryAdvanceWrapper`` as well as changing it's code, where traverse would be called it now calls tryAdvance. 

The problem with this is that a lambda can have many captured arguments and with this solution the number of captured arguments are static.

To solve this we could try and possibly use a MethodInvoke, like so:

```
Method method = lambda.getClass().getDeclaredMethod(name);
method.invoke(instance, argArray);
```

Where name would be the name of the method that instantiates the lambda (as an ``Advancer``) and argArray would be the captured arguments of said lambda.
This would create the lambda so we would still need to call the ``tryAdvance`` Method of the ``Advancer`` Interface.