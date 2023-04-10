package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.expectThrows;


public class ThrowingTriFunctionTest
{
	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingTriFunction<Object, Object, Object, Object, ExecutionException> runnable = ThrowingTriFunction.sneakyThrowing((a, b, c) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.apply(5, 5, 5));
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingTriFunction<Object, Object, Object, Object, RuntimeException> runnable = ThrowingTriFunction.sneakyRuntime((a, b, c) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.apply(5, 5, 5));
	}
}
