void withStreams() {
    Basket basket = new Basket();
    basket.fruits.stream()
                    .filter(current -> current.color.equals("orange"))
                    .forEach(current -> System.out.println(current.name));
}
