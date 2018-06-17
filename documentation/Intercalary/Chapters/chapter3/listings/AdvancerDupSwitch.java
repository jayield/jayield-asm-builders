yield -> {
    this.hasElement.reset();
    YieldWrapper wrapper = new YieldWrapper(yield, this.hasElement);
    Query source = this.source;
    Yield instrumentedYield = (item) -> {
        switch(this.state) {
            case 0:
                wrapper.ret(item);
                this.state = 1;
                return;
            case 1:
                wrapper.ret(item);
                this.state = 2;
                return;
            case 2:
                this.state = 0;
                this.validValue = false;
                return;
            default:
        }
    };

    while(this.hasElement.isFalse() && (this.iterator.hasNext() || this.validValue)) {
        if (!this.validValue && iterator.hasNext()) {
            this.current = iterator.next();
            this.validValue = true;
        }
        instrumentedYield.ret(this.current);
    }

    return this.hasElement.isTrue() ? true : this.hasElement.isTrue();
}