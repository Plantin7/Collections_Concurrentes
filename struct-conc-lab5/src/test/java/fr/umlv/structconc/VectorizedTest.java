package fr.umlv.structconc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jdk.incubator.vector.IntVector;

@SuppressWarnings("static-method")
public class VectorizedTest {
	private static Stream<Arguments> provideIntArraysSum() {
		return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
				.mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
				.map(array -> Arguments.of(array, Arrays.stream(array).reduce(0, Integer::sum)));
	}


	private static Stream<Arguments> provideIntArraysSub() {
		return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
				.mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
				.map(array -> Arguments.of(array, Arrays.stream(array).reduce(0, Integer::sum) * -1));
	}
	
	private static Stream<Arguments> provideIntArraysMinMax() {
		return IntStream.of(0, 1, 10, 100, 1000, 10_000, 100_000)
				.mapToObj(i -> new Random(0).ints(i, 0, 1000).toArray())
				.map(array -> Arguments.of(array, new int[] { Arrays.stream(array).max().orElseThrow(), Arrays.stream(array).min().orElseThrow()}));
	}

	@ParameterizedTest
	@MethodSource("provideIntArraysSum")
	public void sum(int[] array, int expected) {
		assertEquals(expected, Vectorized.sumLoop(array));
	}

	@ParameterizedTest
	@MethodSource("provideIntArraysSum")
	public void sum_lane(int[] array, int expected) {
		assertEquals(expected, Vectorized.sumReduceLanes(array));
	}

	@ParameterizedTest
	@MethodSource("provideIntArraysSum")
	public void sum_wise(int[] array, int expected) {
		assertEquals(expected, Vectorized.sumLanewise(array));
	}

	@ParameterizedTest
	@MethodSource("provideIntArraysSub")
	public void diff_wise(int[] array, int expected) {
		assertEquals(expected, Vectorized.differenceLanewise(array));
	}
	
	@ParameterizedTest
	@MethodSource("provideIntArraysMinMax")
	public void minmax(int[] array, int expected) {
		assertEquals(expected, Vectorized.minmax(array));
	}
}