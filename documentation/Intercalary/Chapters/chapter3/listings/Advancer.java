public interface Advancer<T> extends Serializable {

    static <U> Iterator<U> iterator(Query<U> source) {...}

    static <U> Advancer<U> from(Query<U> source) {...}

    static <U> Advancer<U> from(Traverser<U> source) {...}

    static <U> Iterator<U> iterator(Traverser<U> source) {...}

    default Iterator<T> iterator() {...}

    default <U> Advancer<U> then(Function<Advancer<T>,Advancer<U>> next) {...}

    boolean tryAdvance(Yield<T> yield);

}