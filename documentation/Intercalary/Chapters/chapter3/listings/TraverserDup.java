public <T> Traverser<T> dup(Traverser<T> upstream) {
    return yield -> upstream.traverse(item -> {
        yield.ret(item);
        yield.ret(item);
    });
}