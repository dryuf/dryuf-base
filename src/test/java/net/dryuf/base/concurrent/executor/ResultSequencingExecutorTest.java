package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.future.FutureUtil;
import net.dryuf.base.function.ThrowingFunction;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;


/**
 * Tests for {@link ResultSequencingExecutor}.
 */
public class ResultSequencingExecutorTest
{
	@Test
	public void testNormal() throws Exception
	{
		try (ResultSequencingExecutor rse = new ResultSequencingExecutor()) {
			CompletableFuture<Object> future = rse.submit(() -> 0, v -> v+1);
			assertEquals(FutureUtil.sneakyGet(future), 1);
		}
	}

	@Test
	public void testTaskException() throws Exception
	{
		ThrowingFunction<Object, Object, Exception> completor = mock(ThrowingFunction.class);
		try (ResultSequencingExecutor rse = new ResultSequencingExecutor()) {
			CompletableFuture<Object> future = rse.submit(() -> { throw new IOException(); }, completor);
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
		try (ResultSequencingExecutor rse = new ResultSequencingExecutor()) {
			CompletableFuture<Object> future = rse.submit(() -> 0, completor);
			expectThrows(IOException.class, () -> FutureUtil.sneakyGet(future));
			verify(completor, times(1))
				.apply(0);
		}
	}

	@Test
	public void testShort() throws InterruptedException
	{
		for (int t = 0; t < 1024; ++t) {
			try (ResultSequencingExecutor rse = new ResultSequencingExecutor()) {
				for (int i = ThreadLocalRandom.current().nextInt(512); --i >= 0; ) {
					rse.submit(this::doLittle, ThrowingFunction.identity());
				}
			}
		}
	}

	@Test
	public void testSerialization() throws InterruptedException
	{
		List<Integer> expected = IntStream.rangeClosed(0, 1023).boxed().collect(Collectors.toList());
		List<Integer> result = new ArrayList<>();
		try (CloseableExecutor executor = new ClosingExecutor(Executors.newCachedThreadPool());
		     ResultSequencingExecutor rse = new ResultSequencingExecutor(executor)) {
			for (int i = 0; i < 1024; ++i) {
				final int i0 = i;
				rse.submit(
					() -> { Thread.sleep(ThreadLocalRandom.current().nextInt(10)); return i0; },
					result::add
				);


			}
		}
		assertEquals(result, expected);
	}

	private Void doLittle()
	{
		littleVar *= 17;
		return null;
	}

	private int littleVar = 13;
}
