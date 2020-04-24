package fr.umlv.structconc;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import jdk.incubator.vector.IntVector;

@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class VectorizedBenchMark {
  private int[] array = new Random(0).ints(1_000_000, 0,1_000_000).toArray();

  @Benchmark
  public int sum_loop() {
    return Vectorized.sumLoop(array);
  }
  
  @Benchmark
  public int sum_reduce_lanes_loop() {
	  return Vectorized.sumReduceLanes(array);
  }
  
  @Benchmark
  public int sum_lane_wise_loop() {
	  return Vectorized.sumLanewise(array);
  }
  
  @Benchmark
  public int difference_lane_wise_loop() {
	  return Vectorized.differenceLanewise(array);
  }
  
  @Benchmark
  public int[] minmax_loop() {
	  return Vectorized.minmax(array);
  }
}