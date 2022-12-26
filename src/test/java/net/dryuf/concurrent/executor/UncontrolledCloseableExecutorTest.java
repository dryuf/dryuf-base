package net.dryuf.concurrent.executor;

import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UncontrolledCloseableExecutorTest
{
	@Test(timeOut = 1000L)
	public void testNotClosed()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(1);
		try (CloseableExecutor uncontrolledExecutor = new UncontrolledCloseableExecutor(executor)) {
			uncontrolledExecutor.submit(() -> { latch.await(); return null; });
		}
		executor.execute(() -> {});
		latch.countDown();
		new ClosingExecutor(executor).close();
	}

}
