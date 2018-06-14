public static Stream<String> getOrangeFruitStream(Basket basket) {
    return basket.fruits.stream()
                    .filter(current -> current.color.equals("orange"))
                    .map(current -> current.name);

}
