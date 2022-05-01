package net.dryuf.concurrent.executor;

import org.testng.annotations.Test;

import java.util.concurrent.Executors;


public class NotClosingExecutorTest
{
	@Test
	public void testNotClosed()
	{
		try (CloseableExecutor executor = new ClosingExecutor(Executors.newCachedThreadPool())) {
			try (NotClosingExecutor notClosing = new NotClosingExecutor(executor)) {
				notClosing.execute(() -> {});
			}
			executor.execute(() -> {});
		}
	}
}
