public class Example {
	static <T> Stream <T> filterOdd (Stream <T> source ) { (*@\label{line:filterOdd}@*)
		return StreamSupport.stream(new OddFilter<>(source.spliterator()), false);(*@\label{line:newoddFilter}@*)
	}
	static class OddFilter <T> extends AbstractSpliterator <T> { (*@\label{line:oddFilter}@*)
		final Consumer <T> doNothing = item -> {};
		final Spliterator <T> source ;
		public Odd (Spliterator <T> source ) {
			super (odd(source.estimateSize()), source.characteristics());
			this.source = source;
		}
		@Override
		public boolean tryAdvance(Consumer <? super T> action ) {
			if (!source.tryAdvance(doNothing)) return false ;
				return source.tryAdvance(action);
		}
		private static long odd( long l) {
			return l == Long.MAX_VALUE ? l : (l +1)/2;
		}
	}
	public static void main(String[] args) {
		filterOdd(Stream.of(new Integer[]{1, 2, 3, 4})
            .map(i -> i + 4))
            .forEach(System.out::println);
	}
}
