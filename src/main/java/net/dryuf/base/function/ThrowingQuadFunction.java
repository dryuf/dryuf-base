package net.dryuf.base.function;

import lombok.SneakyThrows;


/**
 * Four arguments function throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <U>
 *	parameter type
 * @param <V>
 *	parameter type
 * @param <W>
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
public interface ThrowingQuadFunction<T, U, V, W, R, X extends Exception>
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
	 * @param p3
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @throws X
	 * 	in case of error.
	 */
	R apply(T p0, U p1, V p2, W p3) throws X;

	/**
	 * Calculates the result from input.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 * @param p2
	 * 	input parameter
	 * @param p3
	 * 	input parameter
	 *
	 * @return
	 * 	result.
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default R sneakyApply(T p0, U p1, V p2, W p3)
	{
		return apply(p0, p1, p2, p3);
	}

	/**
	 * Converts this into {@link ThrowingQuadFunction}, propagating exceptions silently.
	 *
	 * @param <X>
	 *      potential exception declared by returned function
	 *
	 * @return
	 * 	converted {@link ThrowingQuadFunction} object.
	 */
	@SneakyThrows
	default <X extends Exception> ThrowingQuadFunction<T, U, V, W, R, X> sneakyThrowing()
	{
		return sneakyThrowing(this);
	}

	/**
	 * Converts ThrowingQuadFunction into ThrowingQuadFunction, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link ThrowingQuadFunction} object.
	 *
	 * @param <T>
	 *	parameter type
	 * @param <U>
	 *	parameter type
	 * @param <V>
	 *	parameter type
	 * @param <W>
	 *	parameter type
	 * @param <R>
	 *      function return type
	 * @param <X>
	 *      potential exception declared by returned function
	 * @param <OX>
	 *      potential exception thrown by original function
	 */
	@SuppressWarnings("unchecked")
	static <T, U, V, W, R, X extends Exception, OX extends Exception> ThrowingQuadFunction<T, U, V, W, R, X> sneakyThrowing(ThrowingQuadFunction<T, U, V, W, R, OX> function)
	{
		return (ThrowingQuadFunction<T, U, V, W, R, X>) function;
	}

	/**
	 * Converts ThrowingQuadFunction into ThrowingQuadFunction, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link ThrowingQuadFunction} object.
	 *
	 * @param <T>
	 *	parameter type
	 * @param <U>
	 *	parameter type
	 * @param <V>
	 *	parameter type
	 * @param <W>
	 *	parameter type
	 * @param <R>
	 *      function return type
	 * @param <OX>
	 *      potential exception thrown by original function
	 */
	static <T, U, V, W, R, OX extends Exception> ThrowingQuadFunction<T, U, V, W, R, RuntimeException> sneakyRuntime(ThrowingQuadFunction<T, U, V, W, R, OX> function)
	{
		return sneakyThrowing(function);
	}
}
