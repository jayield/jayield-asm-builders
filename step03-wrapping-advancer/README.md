The idea with this step was to create a class that could be used as a generic wrapper for every, or at least most, of 
the Advancers extracted from Traversables.

Say you have a FilterTraversable:
```
public class FilterTraverser<T> implements Traversable<T> {


    private final SimpleSeries<T> source;
    private final Predicate<T> p;
    
    ...
    

    @Override
    public void traverse(Yield<T> yield) {
        source.traverse(e -> {
            if(this.p.test(e))
                yield.ret(e);
        });
    }
}
```

To create an Advancer you would try to achieve something like this:
```
public class FilterAdvancer<T> implements Advancer<T> {


    private final SimpleSeries<T> source;
    private final WrappingAdvancer<T> wrapper;
    private final Predicate<T> p;
    
    public FilterTraversable(SimpleSeries<T> source, Predicate<T> p) {
        ...
        this.wrapper = new WrappingAdvancer<>(source);
    }
    

    @Override
    public void tryAdvance(Yield<T> yield) {
        this.wrapper.tryAdvance(e -> {
            if(this.p.test(e))
                yield.ret(e);
        });
    }
}
```

So basically you would:
- Copy the Traversable, but instead implement Advancer 
- On the constructor you would instantiate a new WrappingAdvancer with the source received
- The tryAdvance method would be calling the tryAdvance of the wrapper with the original method's lambda.