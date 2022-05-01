package net.dryuf.concurrent.executor;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


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
}
