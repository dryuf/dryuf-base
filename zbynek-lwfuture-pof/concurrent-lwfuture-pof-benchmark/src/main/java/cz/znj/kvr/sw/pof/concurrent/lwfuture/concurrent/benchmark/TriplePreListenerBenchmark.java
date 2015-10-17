package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.benchmark;

import com.google.common.util.concurrent.MoreExecutors;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.DistinguishFutureListener;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TriplePreListenerBenchmark
{
	public static final long        COUNT = 1000000;

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = 1)
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
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkLwfuture() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer> future = new cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask<Integer>(() -> {
				return 0;
			});
			future.addListener(new DistinguishFutureListener<Integer>());
			future.addListener(new DistinguishFutureListener<Integer>());
			future.addListener(new DistinguishFutureListener<Integer>());
			future.run();
			future.get();
		}
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkGuava() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			com.google.common.util.concurrent.ListenableFutureTask<Integer> future = com.google.common.util.concurrent.ListenableFutureTask.create(() -> {
				return 0;
			});
			future.addListener(() -> {
				try {
					future.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}, MoreExecutors.directExecutor());
			future.addListener(() -> {
				try {
					future.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}, MoreExecutors.directExecutor());
			future.addListener(() -> {
				try {
					future.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}, MoreExecutors.directExecutor());
			future.run();
			future.get();
		}
	}

	@Benchmark
	@Warmup(iterations = 0)
	@Measurement(iterations = 1, batchSize = 1)
	@Fork(warmups = 1, value = 1)
	public void                     benchmarkSpring() throws ExecutionException, InterruptedException
	{
		for (long i = 0; i < COUNT; ++i) {
			org.springframework.util.concurrent.ListenableFutureTask<Integer> future = new org.springframework.util.concurrent.ListenableFutureTask<Integer>(() -> {
				return 0;
			});
			future.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
			future.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
			future.addCallback(new ListenableFutureCallback<Integer>() {
				@Override
				public void onFailure(Throwable ex) {
				}

				@Override
				public void onSuccess(Integer result) {
				}
			});
			future.run();
			future.get();
		}
	}
}
