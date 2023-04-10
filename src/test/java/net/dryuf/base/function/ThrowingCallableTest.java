package net.dryuf.base.function;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.testng.Assert.expectThrows;


public class ThrowingCallableTest
{
	@Test
	public void sneaky_withException_thrown()
	{
		Callable<Integer> runnable = ThrowingCallable.sneaky(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::call);
	}

	@Test
	public void sneakySupplier_withException_thrown()
	{
		Supplier<Integer> runnable = ThrowingCallable.sneakySupplier(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::get);
	}

	@Test
	public void sneakyThrowing_withException_thrown()
	{
		ThrowingCallable<Object, ExecutionException> runnable = ThrowingCallable.sneakyThrowing(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::call);
	}

	@Test
	public void sneakyRuntime_withException_thrown()
	{
		ThrowingCallable<Object, RuntimeException> runnable = ThrowingCallable.sneakyRuntime(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::call);
	}

	@Test
	public void of_withRuntimeException_thrown()
	{
		ThrowingCallable<Object, Exception> runnable = ThrowingCallable.of(() -> {
			throw new IOException();
		});

		expectThrows(IOException.class, runnable::call);
	}

	@Test
	public void ofSupplier_withRuntimeException_thrown()
	{
		ThrowingCallable<Object, Exception> runnable = ThrowingCallable.ofSupplier(() -> {
			throw new NumberFormatException();
		});

		expectThrows(NumberFormatException.class, runnable::call);
	}

	@Test
	public void ofSupplier_withCustomException_thrown()
	{
		ThrowingCallable<Object, IllegalArgumentException> runnable = ThrowingCallable.ofSupplier(() -> {
			throw new IllegalArgumentException();
		});

		expectThrows(IllegalArgumentException.class, runnable::call);
	}

	@Test
	public void of_withAnyException_thrown()
	{
		ThrowingCallable<Object, IOException> runnable = ThrowingCallable.ofSupplier(() -> {
			throw new IllegalArgumentException();
		});

		expectThrows(IllegalArgumentException.class, runnable::call);
	}
}
