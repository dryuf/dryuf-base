package net.dryuf.base.concurrent.future;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;


public class ScheduledUtilTest
{
	@Test(timeOut = 2000L)
	public void schedule_sleep_wait() throws Exception
	{
		@SuppressWarnings("unchecked")
		Callable<Integer> callback = Mockito.mock(Callable.class);
		Future<Integer> future = ScheduledUtil.sharedExecutor()
			.schedule(callback, 500, TimeUnit.MILLISECONDS);
		Mockito.verify(callback, Mockito.times(0))
			.call();
		future.get();
		Mockito.verify(callback, Mockito.times(1))
			.call();
	}

	@Test
	public void scheduleWithFixedDelayUntilSuccess_whenSuccess_end() throws Exception
	{
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		ArgumentCaptor<Runnable> toRun = ArgumentCaptor.forClass(Runnable.class);
		when(executor.scheduleWithFixedDelay(any(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS)))
			.thenReturn(null);

		CompletableFuture<Integer> future = ScheduledUtil.scheduleWithFixedDelayUntilSuccess(
			executor,
			() -> 1,
			1,
			TimeUnit.MILLISECONDS
		);

		verify(executor, times(1))
			.scheduleWithFixedDelay(toRun.capture(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS));

		expectThrows(CancellationException.class, toRun.getValue()::run);
		assertEquals(future.get(), 1);
	}

	@Test
	public void scheduleWithFixedDelayUntilSuccess_whenFailAndSuccess_end() throws Exception
	{
		Callable<Integer> producer = mock(Callable.class);
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		ArgumentCaptor<Runnable> toRun = ArgumentCaptor.forClass(Runnable.class);
		when(executor.scheduleWithFixedDelay(any(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS)))
			.thenReturn(null);

		when(producer.call())
			.thenThrow(new RuntimeException())
			.thenReturn(1);

		CompletableFuture<Integer> future = ScheduledUtil.scheduleWithFixedDelayUntilSuccess(
			executor,
			producer,
			1,
			TimeUnit.MILLISECONDS
		);

		verify(executor, times(1))
			.scheduleWithFixedDelay(toRun.capture(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS));

		toRun.getValue().run();
		assertEquals(future.isDone(), false);
		expectThrows(CancellationException.class, toRun.getValue()::run);
		assertEquals(future.get(), 1);
	}

	@Test
	public void scheduleWithFixedDelayUntilComposedSuccess_whenSuccess_end() throws Exception
	{
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		ArgumentCaptor<Runnable> toRun = ArgumentCaptor.forClass(Runnable.class);
		when(executor.scheduleWithFixedDelay(any(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS)))
			.thenReturn(null);

		CompletableFuture<Integer> future = ScheduledUtil.scheduleWithFixedDelayUntilComposedSuccess(
			executor,
			() -> CompletableFuture.completedFuture(1),
			1,
			TimeUnit.MILLISECONDS
		);

		verify(executor, times(1))
			.scheduleWithFixedDelay(toRun.capture(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS));

		toRun.getValue().run();
		assertEquals(future.get(), 1);

		expectThrows(CancellationException.class, toRun.getValue()::run);
	}

	@Test
	public void scheduleWithFixedDelayUntilComposedSuccess_whenFailAndSuccess_end() throws Exception
	{
		Callable<CompletableFuture<Integer>> producer = mock(Callable.class);
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		ArgumentCaptor<Runnable> toRun = ArgumentCaptor.forClass(Runnable.class);
		when(executor.scheduleWithFixedDelay(any(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS)))
			.thenReturn(null);

		when(producer.call())
			.thenReturn(FutureUtil.exception(new RuntimeException()), CompletableFuture.completedFuture(1));

		CompletableFuture<Integer> future = ScheduledUtil.scheduleWithFixedDelayUntilComposedSuccess(
			executor,
			producer,
			1,
			TimeUnit.MILLISECONDS
		);

		verify(executor, times(1))
			.scheduleWithFixedDelay(toRun.capture(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS));

		toRun.getValue().run();
		assertEquals(future.isDone(), false);
		toRun.getValue().run();
		assertEquals(future.get(), 1);

		expectThrows(CancellationException.class, toRun.getValue()::run);
	}

	@Test
	public void scheduleWithFixedDelayUntilComposedSuccess_whenDelayed_noReentrant() throws Exception
	{
		Callable<CompletableFuture<Integer>> producer = mock(Callable.class);
		ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
		ArgumentCaptor<Runnable> toRun = ArgumentCaptor.forClass(Runnable.class);
		when(executor.scheduleWithFixedDelay(any(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS)))
			.thenReturn(null);

		CompletableFuture<Integer> firstFuture = new CompletableFuture<>();
		when(producer.call())
			.thenReturn(firstFuture, CompletableFuture.completedFuture(1));

		CompletableFuture<Integer> future = ScheduledUtil.scheduleWithFixedDelayUntilComposedSuccess(
			executor,
			producer,
			1,
			TimeUnit.MILLISECONDS
		);

		verify(executor, times(1))
			.scheduleWithFixedDelay(toRun.capture(), eq(0L), eq(1L), eq(TimeUnit.MILLISECONDS));

		toRun.getValue().run();
		assertEquals(future.isDone(), false);
		toRun.getValue().run();
		assertEquals(future.isDone(), false);
		firstFuture.completeExceptionally(new RuntimeException());
		toRun.getValue().run();
		assertEquals(future.get(), 1);

		expectThrows(CancellationException.class, toRun.getValue()::run);
	}
}
