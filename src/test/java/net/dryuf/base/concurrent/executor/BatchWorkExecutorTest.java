package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.future.FutureUtil;

import net.dryuf.base.concurrent.executor.BatchWorkExecutor;
import net.dryuf.base.concurrent.executor.ClosingExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;


public class BatchWorkExecutorTest
{
	@Test(timeOut = 10_000L)
	public void submit_successful_result() throws ExecutionException, InterruptedException
	{
		AtomicInteger executions = new AtomicInteger();
		CompletableFuture<Integer> f10000;
		try (BatchWorkExecutor<Integer, Integer> executor = new BatchWorkExecutor<>(
			new ClosingExecutor(Executors.newCachedThreadPool()),
			2,
			l -> {
				executions.incrementAndGet();
				try {
					Thread.sleep(l.get(0)/100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return l.stream().map(v -> CompletableFuture.completedFuture(v*v)).collect(Collectors.toList());
			}
		)) {
			CompletableFuture<Integer> f3 = executor.submit(3);
			CompletableFuture<Integer> f16 = executor.submit(16);
			CompletableFuture<Integer> f1000 = executor.submit(1000);
			f10000 = executor.submit(10000);
		}
		Assert.assertEquals((int) f10000.get(), 100_000_000);
		Assert.assertTrue(executions.get() >= 2);
	}

	@Test(timeOut = 10_000L)
	public void submit_exceptionally_exceptional() throws ExecutionException, InterruptedException
	{
		AtomicInteger executions = new AtomicInteger();
		try (BatchWorkExecutor<Integer, Integer> executor = new BatchWorkExecutor<>(
			new ClosingExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())),
			2,
			l -> {
				executions.incrementAndGet();
				return l.stream().map(v -> FutureUtil.<Integer>exception(new NumberFormatException())).collect(Collectors.toList());
			}
		)) {
			IntStream.range(0, 10*Runtime.getRuntime().availableProcessors())
				.mapToObj(executor::submit)
				.collect(Collectors.toList())
				.forEach(f -> {
					ExecutionException ex = Assert.expectThrows(ExecutionException.class, f::get);
					assertThat(ex.getCause(), instanceOf(NumberFormatException.class));
				});

		}
		Assert.assertTrue(executions.get() >= 2);
	}

	@Test(timeOut = 10_000L)
	public void submit_throw_exceptional() throws ExecutionException, InterruptedException
	{
		AtomicInteger executions = new AtomicInteger();
		try (BatchWorkExecutor<Integer, Integer> executor = new BatchWorkExecutor<>(
			new ClosingExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())),
			2,
			l -> {
				executions.incrementAndGet();
				throw new NumberFormatException();
			}
		)) {
			IntStream.range(0, 10*Runtime.getRuntime().availableProcessors())
				.mapToObj(executor::submit)
				.collect(Collectors.toList())
				.forEach(f -> {
					ExecutionException ex = Assert.expectThrows(ExecutionException.class, f::get);
					assertThat(ex.getCause(), instanceOf(NumberFormatException.class));
				});

		}
		Assert.assertTrue(executions.get() >= 2);
	}

	@Test(timeOut = 10_000L)
	public void run_overload_successful() throws ExecutionException, InterruptedException
	{
		AtomicInteger executions = new AtomicInteger();
		AtomicInteger itemsCount = new AtomicInteger();
		List<CompletableFuture<Long>> futures = new ArrayList<>();
		try (BatchWorkExecutor<Long, Long> executor = new BatchWorkExecutor<>(
			new ClosingExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())),
			10,
			l -> {
				executions.incrementAndGet();
				itemsCount.addAndGet(l.size());
				return l.stream().map(v -> CompletableFuture.completedFuture(v*v)).collect(Collectors.toList());
			}
		)) {
			for (long i = 0; i < 100_000; ++i) {
				futures.add(executor.submit(i));
			}
		}
		Assert.assertEquals(itemsCount.get(), 100_000);
		Assert.assertTrue(executions.get() >= futures.size() / (BatchWorkExecutor.PENDING_MAX + 1));
		futures.forEach(f -> {
			try {
				f.get(0, TimeUnit.SECONDS);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test(timeOut = 10_000L)
	public void run_breachLimits_successful() throws Exception
	{
		int old = BatchWorkExecutor.PENDING_MAX;
		BatchWorkExecutor.PENDING_MAX = 63;
		try {
			AtomicInteger executions = new AtomicInteger();
			AtomicInteger itemsCount = new AtomicInteger();
			try (BatchWorkExecutor<Long, Long> executor = new BatchWorkExecutor<>(
				new ClosingExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())),
				10,
				l -> {
					executions.incrementAndGet();
					itemsCount.addAndGet(l.size());
					return l.stream().map(v -> CompletableFuture.completedFuture(v*v)).collect(Collectors.toList());
				}
			)) {
				for (long i = 0; i < 100_000; ++i) {
					executor.submit(i);
				}
			}
			Assert.assertEquals(itemsCount.get(), 100_000);
			Assert.assertTrue(executions.get() >= 100_000 / (BatchWorkExecutor.PENDING_MAX + 1));
		}
		finally {
			BatchWorkExecutor.PENDING_MAX = old;
		}
	}
}
