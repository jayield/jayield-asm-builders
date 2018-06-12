public interface Advancer<T> extends Serializable {

    static <U> Iterator<U> iterator(Query<U> source) {
        return from(source).iterator();
    }

    static <U> Advancer<U> from(Query<U> source) {
        return from(source.getTraverser());
    }

    static <U> Advancer<U> from(Traverser<U> source) {
        return Generator.generateAdvancer(source);
    }

    static <U> Iterator<U> iterator(Traverser<U> source) {
        return from(source).iterator();
    }

    default Iterator<T> iterator() {
        return new AdvancerIterator<>(this);
    }

    default <U> Advancer<U> then(Function<Advancer<T>,Advancer<U>> next) {
        return next.apply(this);
    }

    boolean tryAdvance(Yield<T> yield);


}