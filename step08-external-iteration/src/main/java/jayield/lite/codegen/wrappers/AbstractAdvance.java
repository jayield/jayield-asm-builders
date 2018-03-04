package jayield.lite.codegen.wrappers;

import jayield.lite.Advancer;
import jayield.lite.Traversable;
import jayield.lite.Yield;
import jayield.lite.boxes.BoolBox;
import jayield.lite.codegen.wrappers.YieldWrapper;

import java.util.Iterator;

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
        if(current == null && iterator.hasNext()){
            current = iterator.next();
            this.validValue = true;
            System.out.println(current);
        }
        yield.ret(current);
        if(hasElement.isFalse() && firstFailed){
            firstFailed = false;
            if(iterator.hasNext()){
                current = iterator.next();
            }else {
                this.validValue = false;
            }
            System.out.println(current);
        } else {
            firstFailed = true;
            this.validValue = hasElement.isTrue();
        }
    }
}
