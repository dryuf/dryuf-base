package net.dryuf.concurrent.executor;

import net.dryuf.concurrent.queue.SingleConsumerQueue;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class FinishingSerializingExecutorTest
{
	@Test(timeOut = 1000L)
	public void testExecutorWithFinisher() throws InterruptedException
	{
		CountDownLatch waiter = new CountDownLatch(3);
		Runnable finisher = () -> { waiter.countDown(); };
		Executor executor = FinishingSerializingExecutor.createFromFinisher(finisher);
		executor.execute(() -> {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			waiter.countDown(); });
		executor.execute(() -> { waiter.countDown(); });
		waiter.await();
	}

}
