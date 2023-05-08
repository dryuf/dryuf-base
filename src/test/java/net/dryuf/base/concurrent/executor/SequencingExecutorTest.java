package net.dryuf.base.concurrent.executor;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;


/**
 * Tests for {@link SequencingExecutor}.
 */
public class SequencingExecutorTest
{
	@Test(timeOut = 100_000L)
	public void execute_sequence_ok() throws InterruptedException
	{
		for (int t = 0; t < 1000; ++t) {
			try (SequencingExecutor executor = new SequencingExecutor()) {
				AtomicInteger sequence = new AtomicInteger();
				for (int i = 0; i < 10000; ++i) {
					int i0 = i;
					executor.execute(() -> {
						Assert.assertEquals(sequence.getAndIncrement(), i0);
					});
				}
			}
		}
	}

	@Test(timeOut = 100_000L)
	public void close_overloaded_ok() throws InterruptedException
	{
		for (int t = 0; t < 1000; ++t) {
			AtomicInteger counter = new AtomicInteger();
			try (SequencingExecutor executor = new SequencingExecutor()) {
				for (int i = 0; i < 10000; ++i) {
					executor.execute(counter::incrementAndGet);
				}
			}
			assertEquals(counter.get(), 10000);
		}
	}

	@Test(timeOut = 100_000L)
	public void close_doubleClose_ignore() throws InterruptedException
	{
		for (int t = 0; t < 100; ++t) {
			AtomicInteger counter = new AtomicInteger();
			try (SequencingExecutor executor = new SequencingExecutor()) {
				for (int i = 0; i < 10000; ++i) {
					executor.execute(counter::incrementAndGet);
				}
				executor.close();
			}
			assertEquals(counter.get(), 10000);
		}
	}

	@Test(timeOut = 100_000L)
	public void test_closed_rejected() throws InterruptedException
	{
		for (int t = 0; t < 100; ++t) {
			AtomicInteger counter = new AtomicInteger();
			try (SequencingExecutor executor = new SequencingExecutor()) {
				for (int i = 0; i < 10000; ++i) {
					executor.execute(counter::incrementAndGet);
				}
				executor.close();
				expectThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));
			}
			assertEquals(counter.get(), 10000);
		}
	}

	@Test(timeOut = 100_000L)
	public void test_delegatedClosed_rejected() throws InterruptedException
	{
		ExecutorService delegate = Executors.newCachedThreadPool();
		try (SequencingExecutor executor = new SequencingExecutor(delegate)) {
			CountDownLatch latch = new CountDownLatch(10000);
			for (int i = 0; i < 10000; ++i) {
				executor.execute(latch::countDown);
			}
			latch.await();
			delegate.shutdown();
			// runnable may execute a bit later than latch is notified
			Thread.sleep(100);
			RejectedExecutionException ex1 = expectThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));
			assertNull(ex1.getCause());
			RejectedExecutionException ex2 = expectThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));
			assertThat(ex2.getCause(), instanceOf(RejectedExecutionException.class));
		}
	}

	@Test(timeOut = 100_000L)
	public void close_interrupted_interrupted() throws InterruptedException
	{
		try (SequencingExecutor executor = new SequencingExecutor()) {
			CountDownLatch latch = new CountDownLatch(10);
			for (int i = 0; i < 10; ++i) {
				executor.execute(latch::countDown);
			}
			latch.await();
			Thread.currentThread().interrupt();
		}
		assertTrue(Thread.interrupted());
	}
}
