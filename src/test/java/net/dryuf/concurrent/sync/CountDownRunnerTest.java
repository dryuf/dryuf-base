package net.dryuf.concurrent.sync;

import net.dryuf.concurrent.function.ThrowingRunnable;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CountDownRunnerTest
{
	@Test
	public void countDown_unfinished_nothing()
	{
		ThrowingRunnable<RuntimeException> handler = mock(ThrowingRunnable.class);
		CountDownRunner countdown = new CountDownRunner(2, handler);

		countdown.countDown();

		verify(handler, times(0))
			.run();
	}

	@Test
	public void countDown_finished_executed()
	{
		ThrowingRunnable<RuntimeException> handler = mock(ThrowingRunnable.class);
		CountDownRunner countdown = new CountDownRunner(2, handler);

		countdown.countDown();
		countdown.countDown();

		verify(handler, times(1))
			.run();
	}
}
