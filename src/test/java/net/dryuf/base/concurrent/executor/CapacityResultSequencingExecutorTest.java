package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.future.FutureUtil;
import net.dryuf.base.function.ThrowingFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;


/**
 * Executor serializing the results.
 */
public class CapacityResultSequencingExecutorTest
{
	@Test
	public void testNormal() throws Exception
	{
		try (CapacityResultSequencingExecutor rse = new CapacityResultSequencingExecutor(1, 1)) {
			CompletableFuture<Integer> future = rse.submit(1, () -> 0, v -> v+1);
			assertEquals(FutureUtil.sneakyGet(future), 1);
		}
	}

	@Test
	public void testTaskException() throws Exception
	{
		ThrowingFunction<Object, Object, Exception> completor = mock(ThrowingFunction.class);
		try (CapacityResultSequencingExecutor rse = new CapacityResultSequencingExecutor(1, 1)) {
			CompletableFuture<Object> future = rse.submit(1, () -> { throw new IOException(); }, completor);
			expectThrows(IOException.class, () -> FutureUtil.sneakyGet(future));
			verify(completor, times(0))
				.apply(any());
		}
	}

	@Test
	public void testCompletorException() throws Exception
	{
		ThrowingFunction<Object, Object, Exception> completor = mock(ThrowingFunction.class);
		when(completor.apply(0))
			.thenThrow(new IOException());
		try (CapacityResultSequencingExecutor rse = new CapacityResultSequencingExecutor(1, 1)) {
			CompletableFuture<Object> future = rse.submit(1, () -> 0, completor);
			expectThrows(IOException.class, () -> FutureUtil.sneakyGet(future));
			verify(completor, times(1))
				.apply(0);
		}
	}

	@Test(timeOut = 1800L)
	public void testParallel()
	{
		AtomicInteger pending = new AtomicInteger();
		Callable<Void> fun = () -> { Thread.sleep(500); pending.decrementAndGet(); return null; };
		try (CapacityResultSequencingExecutor executor = new CapacityResultSequencingExecutor(4, 4)) {
			pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
			pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
			if (Runtime.getRuntime().availableProcessors() >= 4) {
				pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
				pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
			}
		}
		Assert.assertEquals(pending.get(), 0);
	}

	@Test(timeOut = 1800L)
	public void testCapacity()
	{
		AtomicInteger pending = new AtomicInteger();
		Callable<Void> fun = () -> { Thread.sleep(200); pending.decrementAndGet(); return null; };
		long start = System.currentTimeMillis();
		try (CapacityResultSequencingExecutor executor = new CapacityResultSequencingExecutor(1, 2)) {
			pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
			pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
		}
		Assert.assertTrue(System.currentTimeMillis()-start >= 400, "time not parallel");
	}

	@Test(timeOut = 1800L)
	public void testCount()
	{
		AtomicInteger pending = new AtomicInteger();
		Callable<Void> fun = () -> { Thread.sleep(200); pending.decrementAndGet(); return null; };
		long start = System.currentTimeMillis();
		try (CapacityResultSequencingExecutor executor = new CapacityResultSequencingExecutor(2, 1)) {
			pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
			pending.addAndGet(2); executor.submit(1, fun, (r) -> pending.decrementAndGet());
		}
		Assert.assertTrue(System.currentTimeMillis()-start >= 400, "time not parallel");
	}
}
