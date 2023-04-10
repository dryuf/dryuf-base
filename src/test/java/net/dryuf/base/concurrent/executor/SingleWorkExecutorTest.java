package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.executor.CloseableExecutor;
import net.dryuf.base.concurrent.executor.SingleWorkExecutor;
import net.dryuf.base.function.ThrowingFunction;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class SingleWorkExecutorTest
{
	@Test
	public void testSubmit() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> f10000;
		try (SingleWorkExecutor<Integer, Integer> executor = new SingleWorkExecutor<>(
			Executors.newCachedThreadPool(),
			v -> {
				try {
					Thread.sleep(v/100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return v*v;
			}
		)) {
			CompletableFuture<Integer> f3 = executor.submit(3);
			CompletableFuture<Integer> f16 = executor.submit(16);
			CompletableFuture<Integer> f1000 = executor.submit(1000);
			f10000 = executor.submit(10000);
		}
		Assert.assertEquals((int) f10000.get(), 100_000_000);
	}

	@Test
	public void testOverload()
	{
		AtomicInteger executions = new AtomicInteger();
		List<CompletableFuture<Long>> futures = new ArrayList<>();
		try (SingleWorkExecutor<Long, Long> executor = new SingleWorkExecutor<>(
			Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
			l -> {
				executions.incrementAndGet();
				return l*l;
			}
		)) {
			for (long i = 0; i < 100_000; ++i) {
				futures.add(executor.submit(i));
			}
		}
		Assert.assertTrue(executions.get() == 100_000);
		futures.stream().forEach(f -> {
			try {
				f.get(0, TimeUnit.SECONDS);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	public void testClose()
	{
		CloseableExecutor underlying = Mockito.mock(CloseableExecutor.class);
		try (SingleWorkExecutor<Long, Long> executor = new SingleWorkExecutor<Long, Long>(
			underlying,
			ThrowingFunction.identity()
		)) {
		}
		verify(underlying, times(1)).close();
	}
}
