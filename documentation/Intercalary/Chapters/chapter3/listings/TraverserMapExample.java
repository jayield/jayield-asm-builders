public Traverser<String> mapToName(Traverser<Fruit> upstream) {
    return yield -> upstream.traverse(fruit -> yield.ret(fruit.name));
}