package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.expectThrows;


public class ThrowingQuadConsumerTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingQuadConsumer<Object, Object, Object, Object, ExecutionException> runnable = ThrowingQuadConsumer.sneakyThrowing((a, b, c, d) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5, 5, 5, 5));
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingQuadConsumer<Object, Object, Object, Object, RuntimeException> runnable = ThrowingQuadConsumer.sneakyRuntime((a, b, c, d) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.accept(5, 5, 5, 5));
	}
}
