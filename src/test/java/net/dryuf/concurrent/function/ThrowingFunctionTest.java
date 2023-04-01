package net.dryuf.concurrent.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class ThrowingFunctionTest
{
	@Test
	public void sneaky_withException_throw()
	{
		Function<Integer, Integer> runnable = ThrowingFunction.sneaky((Integer a) -> {
			throw new IOException();
		});

		expectThrows(IOException.class, () -> runnable.apply(5));
	}

	@Test
	public void of_withRuntimeException_throw()
	{
		ThrowingFunction<Integer, Integer, RuntimeException> runnable = ThrowingFunction.of((Integer a) -> {
			throw new NumberFormatException();
		});

		expectThrows(NumberFormatException.class, () -> runnable.apply(5));
	}

	@Test
	public void identity_always_itself() throws Exception
	{
		Object v = ThrowingFunction.identity().apply((Integer) 1);

		assertEquals(v, (Integer) 1);
	}
}
