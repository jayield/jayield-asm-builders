public class Example {
	static <T> Stream <T> filterOdd (Stream <T> source ) { (*@\label{line:filterOdd}@*)
		return StreamSupport.stream(new OddFilter<>(source.spliterator()), false);(*@\label{line:newoddFilter}@*)
	}
	static class OddFilter <T> extends AbstractSpliterator <T> { (*@\label{line:oddFilter}@*)
		final Consumer <T> doNothing = item -> {};
		final Spliterator <T> source ;
		boolean isOdd = false;
		public Odd (Spliterator <T> source) {
			super (odd(source.estimateSize()), source.characteristics());
			this.source = source;
		}
		@Override
		public boolean tryAdvance(Consumer <? super T> action ) {
			if (!source.tryAdvance(doNothing)) return false ;
				return source.tryAdvance(action);
		}
		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			source.forEachRemaining(item -> {
				if(isOdd){
					action.accept(item);
				}
				isOdd = !isOdd;
			});
		}
		private static long odd( long l) {
			return l == Long.MAX_VALUE ? l : (l +1)/2;
		}
	}
	public static void main(String[] args) {
		filterOdd(getOrangeFruitStream(new Basket()))
            .forEach(System.out::println);
	}
}
