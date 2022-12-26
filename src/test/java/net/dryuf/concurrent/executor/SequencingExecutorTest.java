package net.dryuf.concurrent.executor;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Tests for {@link ResultSequencingExecutor}.
 */
public class SequencingExecutorTest
{
	@Test
	public void testSequence() throws InterruptedException
	{
		for (int t = 0; t < 1024; ++t) {
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
}
