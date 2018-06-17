yield -> {
    this.hasElement.reset();
    YieldWrapper wrapper = new YieldWrapper(yield, this.hasElement);
    Query source = this.source;

    arguments[arguments.length - 1] = wrapper;
    while(elementFound.isFalse() &&
        (Boolean) generatedTryAdvance.invoke(newClass, arguments));

    return this.hasElement.isTrue() ? true : this.hasElement.isTrue();
}