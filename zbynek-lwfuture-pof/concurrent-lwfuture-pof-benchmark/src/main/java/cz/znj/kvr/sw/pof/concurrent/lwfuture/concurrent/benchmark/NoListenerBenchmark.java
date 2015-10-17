package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class NoListenerBenchmark
{
	public static final int         WARMUP_ITERATIONS = 5;
	public static final long        COUNT = 100000;

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkJdk() throws Exception
	{
		for (long i = 0; i < COUNT; ++i) {
			FutureTask<Integer> future = new FutureTask<Integer>(() -> {
				return 0;
			});
			future.run();
			future.get();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkLwfuture() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer> future = new cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer>(() -> {
				return 0;
			});
			future.run();
			future.get();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkGuava() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			com.google.common.util.concurrent.ListenableFutureTask<Integer> future = com.google.common.util.concurrent.ListenableFutureTask.create(() -> {
				return 0;
			});
			future.run();
			future.get();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkSpring() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			org.springframework.util.concurrent.ListenableFutureTask<Integer> future = new org.springframework.util.concurrent.ListenableFutureTask<Integer>(() -> {
				return 0;
			});
			future.run();
			future.get();
		}
	}
}
