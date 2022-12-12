package net.dryuf.concurrent.executor;

import net.dryuf.concurrent.FutureUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


public class CompletableFutureTaskTest
{
	private CloseableExecutor executor = new ClosingExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

	@AfterClass(timeOut = 1_000L)
	public void teardown()
	{
		executor.close();
	}

	@Test(timeOut = 1_000L)
	public void run_normal_success() throws Exception
	{
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> 1);
		executor.execute(task);
		assertEquals(task.get(), (Integer) 1);
	}

	@Test(timeOut = 1_000L)
	public void run_exception_exception() throws Exception
	{
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> { throw new NumberFormatException(); });
		executor.execute(task);
		ExecutionException ex = expectThrows(ExecutionException.class, () -> task.get());
		assertThat(ex.getCause(), instanceOf(NumberFormatException.class));
	}

	@Test(timeOut = 1_000L)
	public void run_cancel_cancelled() throws Exception
	{
		CountDownLatch latch = new CountDownLatch(1);
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> { latch.await(); return null; });
		executor.execute(task);
		task.cancel(false);
		CancellationException ex = expectThrows(CancellationException.class, () -> task.get());
		latch.countDown();
	}

	@Test(timeOut = 1_000L)
	public void run_interruptRunning_cancelled() throws Exception
	{
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> {
			started.countDown();
			try {
				new CountDownLatch(1).await();
			}
			finally {
				finished.countDown();
			}
			return null;
		});
		executor.execute(task);
		started.await();
		task.cancel(true);
		CancellationException ex = expectThrows(CancellationException.class, () -> task.get());
		finished.await();
	}

	@Test(timeOut = 1_000L)
	public void run_interruptUnstarted_cancelled() throws Exception
	{
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> {
			Assert.fail();
			return null;
		});
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
			FutureUtil.submitAsync(() -> { Thread.sleep(100); return null; }, executor);
		}
		executor.execute(task);
		assertTrue(task.cancel(true));
		CancellationException ex = expectThrows(CancellationException.class, () -> task.get());
	}

	@Test(timeOut = 1_000L)
	public void run_interruptDelayedRunning_cancelled() throws Exception
	{
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(1);
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> {
			started.countDown();
			try {
				new CountDownLatch(1).await();
			}
			finally {
				finished.countDown();
			}
			return null;
		}, true);
		executor.execute(task);
		started.await();
		task.cancel(true);
		CancellationException ex = expectThrows(CancellationException.class, () -> task.get());
		finished.await();
	}

	@Test(timeOut = 1_000L)
	public void run_interruptDelayedUnstarted_cancelled() throws Exception
	{
		CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> {
			Assert.fail();
			return null;
		}, true);
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
			FutureUtil.submitAsync(() -> { Thread.sleep(100); return null; }, executor);
		}
		executor.execute(task);
		assertTrue(task.cancel(true));
		CancellationException ex = expectThrows(CancellationException.class, () -> task.get());
	}

	@Test(timeOut = 10_000L)
	public void run_overloadInterrupt_cancelled() throws Exception
	{
		for (int r = 0; r < 1000; ++r) {
			int cnt = r < 10 ? 1<<r : 200;
			List<CompletableFuture<Integer>> futures = IntStream.rangeClosed(0, cnt)
				.mapToObj(i -> FutureUtil.submitAsync(() -> i*i, executor))
				.collect(Collectors.toList());
			futures.forEach(f -> f.cancel(true));
			futures.forEach(f -> assertTrue(f.isDone()));
		}
	}
}
