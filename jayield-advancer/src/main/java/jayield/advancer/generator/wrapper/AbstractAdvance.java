package jayield.advancer.generator.wrapper;

import jayield.advancer.Advancer;
import jayield.Yield;
import jayield.boxes.BoolBox;

import java.util.Iterator;

import static jayield.advancer.generator.Constants.DEBUG;

public abstract class AbstractAdvance<T> implements Advancer<T> {


    protected final BoolBox hasElement;
    protected Iterator<T> iterator;
    protected T current;
    protected boolean validValue;
    protected boolean firstFailed;


    public AbstractAdvance() {
        this.hasElement = new BoolBox();
        this.validValue = false;
        this.firstFailed = true;
    }

    protected void advance(Yield<T> yield) {
        if (!validValue && iterator.hasNext()) {
            current = iterator.next();
            validValue = true;
        }
        yield.ret(current);
        debugState();
    }

    private void debugState() {
        if(DEBUG) {
            System.out.println(String.format("current: %o", current));
            System.out.println(String.format("hasElement: %b", this.hasElement.isTrue()));
            System.out.println(String.format("has next: %b", this.iterator.hasNext()));
            System.out.println(String.format("valid Value: %b", this.validValue));
            System.out.println();
        }
    }
}
