package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingRunnable<ExecutionException> runnable = ThrowingRunnable.sneakyThrowing(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::run);
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingRunnable<RuntimeException> runnable = ThrowingRunnable.sneakyRuntime(() -> {
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

	@Test
	public void of_withCustomException_thrown()
	{
		ThrowingRunnable<IllegalArgumentException> runnable = ThrowingRunnable.of(() -> {
			throw new IllegalArgumentException();
		});

		expectThrows(IllegalArgumentException.class, runnable::run);
	}

	@Test
	public void of_withAnyException_thrown()
	{
		ThrowingRunnable<IOException> runnable = ThrowingRunnable.of(() -> {
			throw new IllegalArgumentException();
		});

		expectThrows(IllegalArgumentException.class, runnable::run);
	}
}
