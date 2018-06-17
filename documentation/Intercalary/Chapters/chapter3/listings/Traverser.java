public interface Traverser<T> extends Serializable {
    void traverse(Yield<? super T> yield);
}