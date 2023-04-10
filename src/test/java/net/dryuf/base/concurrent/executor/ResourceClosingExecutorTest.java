package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.future.ScheduledUtil;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ResourceClosingExecutorTest
{
	@Test
	public void testClosed() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceClosingExecutor(delegate, resource)) {
			executor.execute(() -> {});
		}
		verify(delegate, times(1)).close();
		verify(resource, times(1)).close();
	}

	@Test
	public void testDelayedClosed() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceClosingExecutor(delegate, resource)) {
			executor.execute(() -> {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		}
		verify(delegate, times(1)).close();
		verify(resource, times(1)).close();
	}

	@Test
	public void testCrashedNotClosedResourceClosed() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());
		doThrow(new UnsupportedOperationException())
			.when(delegate).close();

		Assert.expectThrows(UnsupportedOperationException.class, () -> {
			try (CloseableExecutor executor = new ResourceClosingExecutor(delegate, resource)) {
				executor.execute(() -> {
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				});
			}
		});
		verify(delegate, times(1)).close();
		verify(resource, times(1)).close();
	}

	@Test
	public void testDoubleClose_singleClose() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceClosingExecutor(delegate, resource)) {
			executor.execute(() -> {});
			executor.close();
		}
		verify(delegate, times(1)).close();
		verify(resource, times(1)).close();
	}


	@Test
	public void testClose_executorInterrupt_resourceClose() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { CompletableFuture.runAsync(args.getArgument(0)); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceClosingExecutor(delegate, resource)) {
			executor.execute(() -> {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
			Thread.currentThread().interrupt();
		}
		verify(delegate, times(1)).close();
		verify(resource, times(1)).close();

		Assert.assertTrue(Thread.interrupted());
	}

	@Test
	public void testExecute_behindMax_suspends() throws Exception
	{
		ResourceNotClosingExecutor.PENDING_MAX = 63;
		try {
			CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
			AutoCloseable resource = Mockito.mock(AutoCloseable.class);

			List<Runnable> tasks = new ArrayList<>();

			doAnswer(args -> tasks.add(args.getArgument(0)))
				.when(delegate).execute(any());

			try (CloseableExecutor executor = new ResourceNotClosingExecutor(delegate, resource)) {
				for (long i = 0; i < ResourceNotClosingExecutor.PENDING_MAX; ++i) {
					executor.execute(() -> {});
				}
				List<Runnable> tasksCopy = new ArrayList<>(tasks);
				tasks.clear();
				long time = System.currentTimeMillis();
				ScheduledUtil.sharedExecutor()
					.schedule(() -> tasksCopy.forEach(Runnable::run), 100, TimeUnit.MILLISECONDS);
				CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> executor.execute(() -> {}));
				CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> executor.execute(() -> {}));
				f1.get();
				f2.get();
				long end = System.currentTimeMillis();
				Assert.assertTrue(end-time >= 90, "Call to overload expected to be suspended");
				tasks.forEach(Runnable::run);
			}
		}
		finally {
			ResourceNotClosingExecutor.PENDING_MAX = Integer.MAX_VALUE;
		}
	}
}
