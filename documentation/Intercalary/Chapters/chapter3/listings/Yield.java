public interface Yield<T> {
    static final TraversableFinishError finishTraversal = new TraversableFinishError();

    static public void bye() { throw finishTraversal; }

    public void ret(T item);
}