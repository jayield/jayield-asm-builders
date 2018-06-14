public static List<String> getOrangeFruitList(Basket basket) {
    Iterator<Fruit> iterator = basket.fruits.iterator();
    List<String> result = new ArrayList<Fruit>();
    while (iterator.hasNext()) {
        Fruit current = iterator.next();
        if (current.color.equals("orange")) {
            result.add(current.name);
        }
    }
    return result;
}
