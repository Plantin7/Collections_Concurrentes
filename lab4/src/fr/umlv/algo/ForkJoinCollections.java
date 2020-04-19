package fr.umlv.algo;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinCollections {
	
	private static class Task<V, T> extends RecursiveTask<V> {
		private final Spliterator<T> spliterator;
		private final int threshold;
		private final V initialValue;
		private final BiFunction<T, V, V> acc;
		private final BinaryOperator<V> combiner;
		
		public Task(Spliterator<T> spliterator, int threshold, V initialValue,
				BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {
			this.spliterator = spliterator;
			this.threshold = threshold;
			this.initialValue = initialValue;
			this.acc = accumulator;
			this.combiner = combiner;
		}

		@Override
		protected V compute() {
			if(spliterator.estimateSize() < threshold) {
				return sequentialReduce(spliterator, initialValue, acc);
			}
			var sp = spliterator.trySplit();
			var part1 = new Task<>(spliterator, threshold, initialValue, acc, combiner);
			var part2 = new Task<>(sp, threshold, initialValue, acc, combiner);
			
			part1.fork();
			var result2 = part2.compute();
			var result1 = part1.join();
			
			return combiner.apply(result1, result2);
		}
		
	}

	public static <V, T> V forkJoinReduce(Collection<T> collection,  int threshold, V initialValue,
			BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {

		return forkJoinReduce(collection.spliterator(), threshold, initialValue, accumulator, combiner);
	}

	private static <V, T> V forkJoinReduce(Spliterator<T> spliterator, int threshold, V initialValue,
			BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {
		var pool = ForkJoinPool.commonPool();
		
		return pool.invoke(new Task<>(spliterator, threshold, initialValue, accumulator, combiner));
	}
	
	// Sequential
	public static <V, T> V sequentialReduce(Spliterator<T> spliterator, V initial,  BiFunction<T, V, V> accumulator) {
		var box = new Object() { private V acc = initial; };
		while(spliterator.tryAdvance(e -> {
			box.acc = accumulator.apply(e, box.acc);
		}));
		
		return box.acc ;
	}

	public static void main(String[] args) {
		// sequential
		System.out.println(IntStream.range(0, 10_000).sum());

		// fork/join
		var list = IntStream.range(0, 10_000).boxed().collect(Collectors.toList());
		var result = forkJoinReduce(list, 1_000, 0, (acc, value) -> acc + value, (acc1, acc2) -> acc1 + acc2);
		System.out.println(result); 
	}

}
