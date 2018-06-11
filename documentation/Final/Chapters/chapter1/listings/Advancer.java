public interface Advancer<T> extends Serializable {

        boolean tryAdvance(Yield<T> yield);
    
        static <U> Advancer<U> from(Query<U> source) {
            return from(source.getTraverser());
        }
    
        static <U> Advancer<U> from(Traverser<U> source) {
            return Generator.generateAdvancer(source);
        }
    
        static <U> Iterator<U> iterator(Traverser<U> source) {
            return from(source).iterator();
        }
    
        default Traverser<T> traverser() {
            return yield -> {
                while(this.tryAdvance(yield));
            };
        }
    
    
    }