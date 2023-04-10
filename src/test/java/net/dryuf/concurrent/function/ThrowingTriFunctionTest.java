package net.dryuf.concurrent.function;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.expectThrows;


public class ThrowingTriFunctionTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingTriFunction<Integer, Integer, Integer, Integer, RuntimeException> runnable =
			ThrowingTriFunction.sneakyThrowing((a, b, c) -> {
				throw new IOException();
			});

		expectThrows(IOException.class, () -> runnable.apply(5, 5, 5));
	}
}
