void printFirstName(Query<Fruit> fruits) {
    Advancer.from(fruits)
            .tryAdvance(fruit -> System.out.println(fruit.name));
}