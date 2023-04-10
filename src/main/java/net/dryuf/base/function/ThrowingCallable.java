package net.dryuf.base.function;

import lombok.SneakyThrows;

import java.util.concurrent.Callable;
import java.util.function.Supplier;


/**
 * {@link Callable} throwing a specific exception.
 *
 * @param <X>
 *	thrown exception
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
@FunctionalInterface
public interface ThrowingCallable<R, X extends Exception> extends Callable<R>
{
	/**
	 * Executes callable.
	 *
	 * @return
	 * 	result of call
	 *
	 * @throws X
	 * 	in case of error.
	 */
	R call() throws X;

	/**
	 * Executes callable, silently propagating exception.
	 *
	 * @return
	 * 	result of call
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default R sneakyCall()
	{
		return call();
	}

	/**
	 * Converts this into {@link Callable}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link Callable} object.
	 */
	default Callable<R> sneaky()
	{
		return this;
	}

	/**
	 * Converts this into {@link Supplier}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link Supplier} object.
	 */
	default Supplier<R> sneakySupplier()
	{
		return sneakySupplier(this);
	}

	/**
	 * Converts {@link Callable} to {@link ThrowingCallable} .
	 *
	 * @param callable
	 * 	original {@link Callable}.
	 *
	 * @return
	 * 	throwing runnable wrapper
	 *
	 * @param <R>
	 *      return type
	 */
	static <R> ThrowingCallable<R, Exception> of(Callable<R> callable)
	{
		return callable::call;
	}

	/**
	 * Converts {@link Supplier} to {@link ThrowingCallable} .
	 *
	 * @param supplier
	 * 	original {@link Supplier}.
	 *
	 * @param <X>
	 *      potential exception declared by returned function
	 *
	 * @return
	 * 	throwing runnable wrapper
	 *
	 * @param <R>
	 *      return type
	 */
	static <R, X extends Exception> ThrowingCallable<R, X> ofSupplier(Supplier<R> supplier)
	{
		return supplier::get;
	}

	/**
	 * Converts ThrowingCallable into Callable, propagating exceptions silently.
	 *
	 * @param callable
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Callable} object.
	 *
	 * @param <R>
	 *      function return type
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <R, X extends Exception> Callable<R> sneaky(ThrowingCallable<R, X> callable)
	{
		// Keep this expanded so mock instances still work correctly:
		return callable;
	}

	/**
	 * Converts ThrowingCallable into Supplier, propagating exceptions silently.
	 *
	 * @param callable
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Callable} object.
	 *
	 * @param <R>
	 *      function return type
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <R, X extends Exception> Supplier<R> sneakySupplier(ThrowingCallable<R, X> callable)
	{
		// Keep this expanded so mock instances still work correctly:
		return new Supplier<R>()
		{
			@Override
			@SneakyThrows
			public R get()
			{
				return callable.call();
			}
		};
	}

	/**
	 * Converts ThrowingCallable into ThrowingCallable, propagating exceptions silently.
	 *
	 * @param callable
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Callable} object.
	 *
	 * @param <R>
	 *      function return type
	 * @param <X>
	 *      exception declared by returned function
	 */
	@SuppressWarnings("unchecked")
	static <R, X extends Exception> ThrowingCallable<R, X> sneakyThrowing(Callable<R> callable)
	{
		// Keep this expanded so mock instances still work correctly:
		return new ThrowingCallable<R, X>()
		{
			@Override
			@SneakyThrows
			public R call() throws X
			{
				return callable.call();
			}
		};
	}

	/**
	 * Converts ThrowingCallable into ThrowingCallable, propagating exceptions silently.
	 *
	 * @param callable
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Callable} object.
	 *
	 * @param <R>
	 *      function return type
	 */
	@SuppressWarnings("unchecked")
	static <R> ThrowingCallable<R, RuntimeException> sneakyRuntime(Callable<R> callable)
	{
		return sneakyThrowing(callable);
	}
}
