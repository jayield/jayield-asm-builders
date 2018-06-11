void withIterator() {
    Basket basket = new Basket();
    Iterator<Fruit> iterator = basket.fruits.iterator();
    while (iterator.hasNext()) {
        Fruit current = iterator.next();
        if (current.color.equals("orange")) {
            System.out.println(current.name);
        }
    }
}
