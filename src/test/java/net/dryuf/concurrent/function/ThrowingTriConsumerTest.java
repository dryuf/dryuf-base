package net.dryuf.concurrent.function;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.expectThrows;


public class ThrowingTriConsumerTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingTriConsumer<Integer, Integer, Integer, RuntimeException> runnable =
			ThrowingTriConsumer.sneakyThrowing((a, b, c) -> {
				throw new IOException();
			});

		expectThrows(IOException.class, () -> runnable.accept(5, 5, 5));
	}
}
