package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.expectThrows;


public class ThrowingTriConsumerTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingTriConsumer<Object, Object, Object, ExecutionException> runnable = ThrowingTriConsumer.sneakyThrowing((a, b, c) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5, 5, 5));
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingTriConsumer<Object, Object, Object, RuntimeException> runnable = ThrowingTriConsumer.sneakyRuntime((a, b, c) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5, 5, 5));
	}
}
