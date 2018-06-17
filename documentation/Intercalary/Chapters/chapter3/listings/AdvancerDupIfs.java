yield -> {
    this.hasElement.reset();
    YieldWrapper wrapper = new YieldWrapper(yield, this.hasElement);
    Query source = this.source;
    Yield instrumentedYield = (item) -> {
        if(this.state == 0){
            this.state++;
            wrapper.ret(item);
            return;
        } else if (this.state == 1) {
            this.state++;
            wrapper.ret(item);
            return;
        } else if (this.state == 2) {
            this.state = 0;
            this.validValue = false;
            return;
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