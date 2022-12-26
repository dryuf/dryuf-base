package net.dryuf.concurrent.executor;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.expectThrows;


public class NotClosingExecutorTest
{
	@Test
	public void testNotClosed()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		try (NotClosingExecutor notClosing = new NotClosingExecutor(executor)) {
			notClosing.execute(() -> {});
		}
		executor.execute(() -> {});
		new ClosingExecutor(executor).close();
	}
	@Test(timeOut = 1000L)
	public void testResourceClosed() throws Exception
	{
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		ExecutorService executor = Executors.newCachedThreadPool();
		try (CloseableExecutor closeableExecutor = new NotClosingExecutor(executor, resource)) {
			closeableExecutor.execute(() -> { });
		}
		verify(resource, times(1)).close();
		executor.execute(() -> {});
		new ClosingExecutor(executor).close();
	}

	@Test(timeOut = 1000L)
	public void testResourceClosedOnce() throws Exception
	{
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		ExecutorService executor = Executors.newCachedThreadPool();
		try (CloseableExecutor closeableExecutor = new NotClosingExecutor(executor, resource)) {
			closeableExecutor.execute(() -> { });
			closeableExecutor.close();
		}
		verify(resource, times(1)).close();
		executor.execute(() -> {});
		new ClosingExecutor(executor).close();
	}

	@Test(timeOut = 1000L)
	public void testThrowingResourceClosedOnce() throws Exception
	{
		AutoCloseable resource = Mockito.mock(AutoCloseable.class);
		doThrow(new NumberFormatException()).when(resource).close();
		ExecutorService executor = Executors.newCachedThreadPool();
		try (CloseableExecutor closeableExecutor = new NotClosingExecutor(executor, resource)) {
			closeableExecutor.execute(() -> { });
			expectThrows(RuntimeException.class, () -> closeableExecutor.close());
		}
		verify(resource, times(1)).close();
		executor.execute(() -> {});
		new ClosingExecutor(executor).close();
	}
}
