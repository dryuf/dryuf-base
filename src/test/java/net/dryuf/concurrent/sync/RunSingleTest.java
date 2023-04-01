package net.dryuf.concurrent.sync;

import lombok.SneakyThrows;
import net.dryuf.concurrent.function.ThrowingCallable;
import net.dryuf.concurrent.function.ThrowingRunnable;
import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RunSingleTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void run_nothing_executed()
	{
		ThrowingRunnable<RuntimeException> runnable = mock(ThrowingRunnable.class);
		RunSingle run = new RunSingle();

		run.run(runnable);

		verify(runnable, times(1)).run();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void run_running_skipped() throws InterruptedException
	{
		ThrowingRunnable<RuntimeException> runnable = mock(ThrowingRunnable.class);
		RunSingle run = new RunSingle();

		CountDownLatch wait = new CountDownLatch(1);
		new Thread(() -> {
			run.run(() -> {
				wait.countDown();
				sleep(1000);
			});
		}).start();
		wait.await();
		run.run(runnable);

		verify(runnable, times(0)).run();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void call_nothing_executed()
	{
		ThrowingCallable<Integer, RuntimeException> runnable = mock(ThrowingCallable.class);
		when(runnable.call()).thenReturn(5);
		RunSingle run = new RunSingle();

		Integer result = run.call(runnable);
		assertEquals(result, (Integer) 5);

		verify(runnable, times(1)).call();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void call_running_skipped() throws InterruptedException
	{
		ThrowingCallable<Integer, RuntimeException> runnable = mock(ThrowingCallable.class);
		when(runnable.call()).thenReturn(5);
		RunSingle run = new RunSingle();

		CountDownLatch wait = new CountDownLatch(1);
		new Thread(() -> {
			run.run(() -> {
				wait.countDown();
				sleep(1000);
			});
		}).start();
		wait.await();
		Integer result = run.call(runnable);
		assertEquals(result, null);

		verify(runnable, times(0)).call();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void compose_nothing_executed() throws Exception
	{
		CompletableFuture<Integer> future = new CompletableFuture<>();
		ThrowingCallable<CompletableFuture<Integer>, RuntimeException> runnable = mock(ThrowingCallable.class);
		when(runnable.call()).thenReturn(future);
		RunSingle run = new RunSingle();

		CompletableFuture<Integer> result = run.compose(runnable);
		assertFalse(result.isDone());
		future.complete(5);
		assertEquals(result.get(), (Integer) 5);

		verify(runnable, times(1)).call();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void compose_running_skipped() throws Exception
	{
		CompletableFuture<Integer> future = new CompletableFuture<>();
		ThrowingCallable<CompletableFuture<Integer>, RuntimeException> runnable = mock(ThrowingCallable.class);
		when(runnable.call()).thenReturn(future);
		ThrowingCallable<CompletableFuture<Integer>, RuntimeException> second = mock(ThrowingCallable.class);
		RunSingle run = new RunSingle();

		CompletableFuture<Integer> result = run.compose(runnable);
		CompletableFuture<Integer> result2 = run.compose(second);

		assertFalse(result.isDone());
		assertTrue(result2.isCancelled());
		future.complete(5);
		assertEquals(result.get(), (Integer) 5);

		verify(runnable, times(1)).call();
		verify(second, times(0)).call();
	}

	@SneakyThrows
	void sleep(long sleep)
	{
		Thread.sleep(sleep);
	}
}
