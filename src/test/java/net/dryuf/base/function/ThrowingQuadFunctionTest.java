package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.expectThrows;


public class ThrowingQuadFunctionTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingQuadFunction<Object, Object, Object, Object, Object, ExecutionException> runnable = ThrowingQuadFunction.sneakyThrowing((a, b, c, d) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.apply(5, 5, 5, 5));
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingQuadFunction<Object, Object, Object, Object, Object, RuntimeException> runnable = ThrowingQuadFunction.sneakyRuntime((a, b, c, d) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.apply(5, 5, 5, 5));
	}
}
