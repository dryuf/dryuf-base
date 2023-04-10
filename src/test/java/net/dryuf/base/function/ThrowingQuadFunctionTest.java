package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.expectThrows;


public class ThrowingQuadFunctionTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingQuadFunction<Integer, Integer, Integer, Integer, Integer, RuntimeException> runnable =
			ThrowingQuadFunction.sneakyThrowing((a, b, c, d) -> {
				throw new IOException();
			});

		expectThrows(IOException.class, () -> runnable.apply(5, 5, 5, 5));
	}
}
