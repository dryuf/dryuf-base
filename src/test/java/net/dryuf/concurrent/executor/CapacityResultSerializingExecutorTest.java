package net.dryuf.concurrent.executor;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Executor serializing the results.
 */
public class CapacityResultSerializingExecutorTest
{
	@Test(timeOut = 1800L)
	public void testParallel() throws InterruptedException
	{
		AtomicInteger pending = new AtomicInteger();
		Callable<Void> fun = () -> { Thread.sleep(500); pending.decrementAndGet(); return null; };
		try (CapacityResultSerializingExecutor executor = new CapacityResultSerializingExecutor(4, 4)) {
			pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
			pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
			if (Runtime.getRuntime().availableProcessors() >= 4) {
				pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
				pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
			}
		}
		Assert.assertEquals(pending.get(), 0);
	}

	@Test(timeOut = 1800L)
	public void testCapacity() throws InterruptedException
	{
		AtomicInteger pending = new AtomicInteger();
		Callable<Void> fun = () -> { Thread.sleep(200); pending.decrementAndGet(); return null; };
		long start = System.currentTimeMillis();
		try (CapacityResultSerializingExecutor executor = new CapacityResultSerializingExecutor(1, 2)) {
			pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
			pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
		}
		Assert.assertTrue(System.currentTimeMillis()-start >= 400, "time not parallel");
	}

	@Test(timeOut = 1800L)
	public void testCount() throws InterruptedException
	{
		AtomicInteger pending = new AtomicInteger();
		Callable<Void> fun = () -> { Thread.sleep(200); pending.decrementAndGet(); return null; };
		long start = System.currentTimeMillis();
		try (CapacityResultSerializingExecutor executor = new CapacityResultSerializingExecutor(2, 1)) {
			pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
			pending.addAndGet(2); executor.submit(1, fun).thenAccept((r) -> pending.decrementAndGet());
		}
		Assert.assertTrue(System.currentTimeMillis()-start >= 400, "time not parallel");
	}
}
