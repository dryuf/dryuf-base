package net.dryuf.concurrent.function;

import lombok.SneakyThrows;

import java.util.function.Function;


/**
 * {@link java.util.function.Function} throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <R>
 *      return type
 * @param <X>
 *	thrown exception
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Exception>
{
	/**
	 * Calculates the result from input.
	 *
	 * @param input
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @throws X
	 * 	in case of error.
	 */
	R apply(T input) throws X;

	/**
	 * Calculates the result from input, silently propagating exceptions.
	 *
	 * @param input
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default R sneakyApply(T input)
	{
		return apply(input);
	}

	/**
	 * Converts this into {@link Function}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link Function} object.
	 */
	default Function<T, R> sneaky()
	{
		// Keep this expanded so mock instances still work correctly:
		return sneaky(this);
	}

	/**
	 * Function returning its input parameter as a result.
	 *
	 * @return
	 * 	the input parameter
	 *
	 * @param <T>
	 *     	type of input and output parameter
	 * @param <X>
	 *      type of thrown exception
	 */
	static <T, X extends Exception> ThrowingFunction<T, T, X> identity()
	{
		return v -> v;
	}

	/**
	 * Converts {@link Function} to {@link ThrowingFunction} .
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	throwing function wrapper
	 *
	 * @param <T>
	 *     	type of parameter
	 * @param <R>
	 *      type of return
	 */
	static <T, R> ThrowingFunction<T, R, RuntimeException> of(Function<T, R> function)
	{
		return function::apply;
	}

	/**
	 * Converts ThrowingFunction into Function, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Function} object.
	 *
	 * @param <T>
	 *      type of function parameter
	 * @param <R>
	 *      function return type
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, R, X extends Exception> Function<T, R> sneaky(ThrowingFunction<T, R, X> function)
	{
		// Keep this expanded so mock instances still work correctly:
		return new Function<T, R>()
		{
			@Override
			@SneakyThrows
			public R apply(T v)
			{
				return function.apply(v);
			}
		};
	}
}
