package net.dryuf.concurrent.executor;

import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
}
