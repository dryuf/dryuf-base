package net.dryuf.base.function;

import lombok.SneakyThrows;


/**
 * Three arguments function throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <U>
 *	parameter type
 * @param <V>
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
public interface ThrowingTriFunction<T, U, V, R, X extends Exception>
{
	/**
	 * Calculates the result from input.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 * @param p2
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @throws X
	 * 	in case of error.
	 */
	R apply(T p0, U p1, V p2) throws X;

	/**
	 * Calculates the result from input.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 * @param p2
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default R sneakyApply(T p0, U p1, V p2)
	{
		return apply(p0, p1, p2);
	}

	/**
	 * Converts this into {@link ThrowingTriFunction}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link ThrowingTriFunction} object.
	 */
	@SneakyThrows
	default ThrowingTriFunction<T, U, V, R, RuntimeException> sneakyThrowing()
	{
		return sneakyThrowing(this);
	}

	/**
	 * Converts ThrowingFunction into Function, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link ThrowingTriFunction} object.
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
	static <T, U, V, R, X extends Exception> ThrowingTriFunction<T, U, V, R, RuntimeException> sneakyThrowing(ThrowingTriFunction<T, U, V,	R, X> function)
	{
		// Keep this expanded so mock instances still work correctly:
		return new ThrowingTriFunction<T, U, V, R, RuntimeException>()
		{
			@Override
			@SneakyThrows
			public R apply(T t, U u, V v)
			{
				return function.apply(t, u, v);
			}
		};
	}
}
