package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.benchmark;

import com.google.common.util.concurrent.MoreExecutors;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.AbstractFutureListener;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class SinglePostListenerAsyncBenchmark
{
	public static final int         WARMUP_ITERATIONS = BenchmarkSupport.WARMUP_ITERATIONS;
	public static final int         COUNT = BenchmarkSupport.COUNT;

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkJdk() throws Exception
	{
		FutureTask[] array = BenchmarkSupport.populateJdkFutureArray(COUNT);
		for (FutureTask<Integer> f: array) {
			// nothing to add, no listenable
		}
		BenchmarkSupport.threadedRunFutures(array);
		for (FutureTask<Integer> f: array) {
			f.get();
		}
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkLwFuture() throws ExecutionException, InterruptedException
	{
		cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask[] array = BenchmarkSupport.populateLwFutureArray(COUNT);
		BenchmarkSupport.threadedRunFutures(array);
		for (cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask f: array) {
			f.addListener(new AbstractFutureListener<Integer>());
		}
		// skip futures.get() as we already handled in listeners
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkGuava() throws ExecutionException, InterruptedException
	{
		Executor directExecutor = MoreExecutors.directExecutor();
		com.google.common.util.concurrent.ListenableFutureTask[] array = BenchmarkSupport.populateGuavaFutureArray(COUNT);
		BenchmarkSupport.threadedRunFutures(array);
		for (com.google.common.util.concurrent.ListenableFutureTask f: array) {
			final Future<Integer> ff = f;
			f.addListener(() -> {
				try {
					ff.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
				}
			}, directExecutor);
		}
		// skip futures.get() as we already handled in listeners
	}

	@Benchmark
	@Warmup(iterations = WARMUP_ITERATIONS)
	@Measurement(iterations = 2, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkSpring() throws ExecutionException, InterruptedException
	{
		org.springframework.util.concurrent.ListenableFutureTask[] array = BenchmarkSupport.populateSpringFutureArray(COUNT);
		BenchmarkSupport.threadedRunFutures(array);
		for (org.springframework.util.concurrent.ListenableFutureTask f: array) {
			f.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
		}
		// skip futures.get() as we already handled in listeners
	}
}
