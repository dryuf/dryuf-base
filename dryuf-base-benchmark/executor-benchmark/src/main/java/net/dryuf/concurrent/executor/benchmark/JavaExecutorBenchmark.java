package net.dryuf.concurrent.executor.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Benchmark for Java Executor.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(Config.FORK_COUNT)
@Warmup(iterations = Config.WARMUP_COUNT, time = Config.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = Config.MEASURE_COUNT, time = Config.MEASURE_TIME, timeUnit = TimeUnit.SECONDS)
public class JavaExecutorBenchmark
{
	public final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@TearDown
	public void teardown()
	{
		executor.shutdown();
	}

	@Benchmark
	public void b0_execute() throws InterruptedException
	{
		CountDownLatch latch = new CountDownLatch(Config.EXECS);
		for (int i = 0; i < Config.EXECS; ++i) {
			executor.execute(() -> latch.countDown());
		}
		latch.await();
	}

	@Benchmark
	public void b0_submit() throws Exception
	{
		CompletableFuture<Void> futures = CompletableFuture.completedFuture(null);
		for (int i = 0; i < Config.EXECS; ++i) {
			futures = CompletableFuture.runAsync(() -> {}, executor)
				.runAfterBoth(futures, () -> {});
		}
		futures.get();
	}
}
