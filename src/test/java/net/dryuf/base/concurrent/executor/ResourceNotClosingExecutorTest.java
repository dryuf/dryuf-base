package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.executor.CloseableExecutor;
import net.dryuf.base.concurrent.executor.ResourceNotClosingExecutor;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class ResourceNotClosingExecutorTest
{
	@Test
	public void testNotClosed() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceNotClosingExecutor(delegate, resource)) {
			executor.execute(() -> {});
		}
		verify(delegate, times(0)).close();
		verify(resource, times(1)).close();
	}

	@Test
	public void testDelayedNotClosed() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceNotClosingExecutor(delegate, resource)) {
			executor.execute(() -> {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		}
		verify(delegate, times(0)).close();
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

		try (CloseableExecutor executor = new ResourceNotClosingExecutor(delegate, resource)) {
			executor.execute(() -> {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		}

		verify(delegate, times(0)).close();
		verify(resource, times(1)).close();
	}

	@Test
	public void testDoubleClose_singleClose() throws Exception
	{
		CloseableExecutor delegate = Mockito.mock(CloseableExecutor.class);
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doAnswer(args -> { ((Runnable) args.getArgument(0)).run(); return null; })
			.when(delegate).execute(any());

		try (CloseableExecutor executor = new ResourceNotClosingExecutor(delegate, resource)) {
			executor.execute(() -> {});
			executor.close();
		}
		verify(delegate, times(0)).close();
		verify(resource, times(1)).close();
	}
}
