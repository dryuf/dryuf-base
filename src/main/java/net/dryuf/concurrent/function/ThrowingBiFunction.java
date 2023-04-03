package net.dryuf.concurrent.function;

import lombok.SneakyThrows;

import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * {@link BiFunction} throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <U>
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
public interface ThrowingBiFunction<T, U, R, X extends Exception>
{
	/**
	 * Calculates the result from input.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @throws X
	 * 	in case of error.
	 */
	R apply(T p0, U p1) throws X;

	/**
	 * Calculates the result from input.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default R sneakyApply(T p0, U p1)
	{
		return apply(p0, p1);
	}

	/**
	 * Converts this into {@link BiFunction}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link BiFunction} object.
	 */
	@SneakyThrows
	default BiFunction<T, U, R> sneaky()
	{
		return sneaky(this);
	}

	/**
	 * Converts {@link BiFunction} to {@link ThrowingBiFunction} .
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	throwing function wrapper
	 *
	 * @param <T>
	 *     	type of parameter
	 * @param <U>
	 *     	type of parameter
	 * @param <R>
	 *      type of return
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, U, R, X extends Exception> ThrowingBiFunction<T, U, R, X> of(BiFunction<T, U, R> function)
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
	 * 	converted {@link BiFunction} object.
	 *
	 * @param <T>
	 *      type of function parameter
	 * @param <U>
	 *     	type of parameter
	 * @param <R>
	 *      function return type
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, U, R, X extends Exception> BiFunction<T, U, R> sneaky(ThrowingBiFunction<T, U, R, X> function)
	{
		// Keep this expanded so mock instances still work correctly:
		return new BiFunction<T, U, R>()
		{
			@Override
			@SneakyThrows
			public R apply(T t, U u)
			{
				return function.apply(t, u);
			}
		};
	}
}
