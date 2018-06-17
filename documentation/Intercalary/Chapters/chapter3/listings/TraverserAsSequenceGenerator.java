public Traverser<Fruit> getFruitSequence() {
    return yield -> {
        yield.ret(Basket.orange);
        yield.ret(Basket.blueberry);
        ...
        yield.ret(Basket.lemon);
        yield.ret(Basket.mango);
        yield.ret(Basket.strawberry);
    };
}