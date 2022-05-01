package net.dryuf.concurrent.executor;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Log
public class BatchWorkExecutorTest
{
	@Test
	public void testSubmit() throws ExecutionException, InterruptedException
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
		log.info("Executions: " + executions);
	}
}
