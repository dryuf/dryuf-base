package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.expectThrows;

public class ThrowingRunnableTest
{
	@Test
	public void sneaky_withException_thrown()
	{
		Runnable runnable = ThrowingRunnable.sneaky(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::run);
	}

	@Test
	public void of_withRuntimeException_thrown()
	{
		ThrowingRunnable<RuntimeException> runnable = ThrowingRunnable.of(() -> {
			throw new NumberFormatException();
		});

		expectThrows(NumberFormatException.class, runnable::run);
	}
}
