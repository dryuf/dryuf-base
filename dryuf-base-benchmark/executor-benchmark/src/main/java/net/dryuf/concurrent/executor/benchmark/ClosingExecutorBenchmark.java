package net.dryuf.concurrent.executor.benchmark;

import net.dryuf.base.concurrent.executor.CloseableExecutor;
import net.dryuf.base.concurrent.executor.ClosingExecutor;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Benchmark for ClosingExecutor implementations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(Config.FORK_COUNT)
@Warmup(iterations = Config.WARMUP_COUNT, time = Config.WARMUP_TIME, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = Config.MEASURE_COUNT, time = Config.MEASURE_TIME, timeUnit = TimeUnit.SECONDS)
public class ClosingExecutorBenchmark
{
	public final CloseableExecutor executor = new ClosingExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

	@TearDown
	public void teardown()
	{
		executor.close();
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
			futures = executor.submit(() -> null)
				.runAfterBoth(futures, () -> {});
		}
		futures.get();
	}
}
