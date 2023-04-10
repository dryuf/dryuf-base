package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.executor.CloseableExecutor;
import net.dryuf.base.concurrent.executor.ClosingExecutor;
import org.testng.annotations.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;


public class ClosingExecutorTest
{
	@Test(expectedExceptions = RejectedExecutionException.class)
	public void testClosed()
	{
		try (CloseableExecutor executor = new ClosingExecutor(Executors.newCachedThreadPool())) {
			executor.execute(() -> {});
			executor.close();
			executor.execute(() -> {});
		}
	}
}
