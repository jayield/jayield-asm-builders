import static java.util.Arrays.asList;

public class Basket {

    Fruit orange = new Fruit("Orange", "orange");
    Fruit lemon = new Fruit("Lemon", "yellow");
    ...
    Fruit mango = new Fruit("Mango", "orange");

    public final List<Fruit> fruits = asList(orange, lemon, mango, ...);
    
}