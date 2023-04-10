package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.expectThrows;


public class ThrowingQuadConsumerTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingQuadConsumer<Integer, Integer, Integer, Integer, RuntimeException> runnable =
			ThrowingQuadConsumer.sneakyThrowing((a, b, c, d) -> {
				throw new IOException();
			});

		expectThrows(IOException.class, () -> runnable.accept(5, 5, 5, 5));
	}
}
