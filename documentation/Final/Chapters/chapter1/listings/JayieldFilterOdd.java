boolean[] isOdd = {false};
getOrangeFruitQuery(new Basket())
        .then(source -> yield -> source.traverse(item -> {
            if(isOdd[0]) {
                yield.ret(item);
            }
            isOdd[0] = !isOdd[0];
        }));