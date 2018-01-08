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

And finally we instrument a copy of class that declares the lambda's code by running it through a custom ClassVisitor that will change the method that instantiates the lambda by returning an Advancer instead of a Traverser.

To do so, we have to run all methods through a Method visitor that changes the invokeDynamic's handlers owners from the original class to the new one (The one we're instrumenting) and the method that instantiates the lambda through yet another Method visitor that changes the method called from traverse to tryAdvance.

After this we get a hold of the method that instantiates the lambda and wrap it in a way that resembles what was described in step03:
```
final BoolBox box = new BoolBox();
return y -> {
    box.reset();
    Yield<R> wrap = wr -> {
        y.ret(wr);
        box.set();
    };
    arguments[arguments.length - 1] = wrap;
    try {
        while(box.isFalse() && (Boolean) method[0].invoke(newClass, arguments));
        return box.isTrue();
    } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
    }
};
```


  