package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static org.testng.Assert.expectThrows;

public class ThrowingConsumerTest
{
	@Test
	public void sneaky_withException_thrown()
	{
		Consumer<Integer> runnable = ThrowingConsumer.sneaky((a) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5));
	}

	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingConsumer<Object, ExecutionException> runnable = ThrowingConsumer.sneakyThrowing((a) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5));
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingConsumer<Object, RuntimeException> runnable = ThrowingConsumer.sneakyRuntime((a) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5));
	}

	@Test
	public void of_withRuntimeException_thrown()
	{
		ThrowingConsumer<Integer, RuntimeException> runnable = ThrowingConsumer.of((a) -> {
			throw new NumberFormatException();
		});

		expectThrows(NumberFormatException.class, () -> runnable.accept(5));
	}
}
