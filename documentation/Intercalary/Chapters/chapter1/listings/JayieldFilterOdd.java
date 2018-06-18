getOrangeFruitQuery(new Basket())
    .then(source -> {
        boolean[] isOdd = {false};
        return yield -> source.traverse(item -> {
            if(isOdd[0]) {
                yield.ret(item);
            }
            isOdd[0] = !isOdd[0];
        });
    });