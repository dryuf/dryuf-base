package net.dryuf.base.function;

import lombok.SneakyThrows;


/**
 * Four arguments consumer throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <U>
 *	parameter type
 * @param <V>
 *	parameter type
 * @param <W>
 *     	type of parameter
 * @param <X>
 *	thrown exception
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
@FunctionalInterface
public interface ThrowingQuadConsumer<T, U, V, W, X extends Exception>
{
	/**
	 * Consumes the value.
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
	 * @throws X
	 * 	in case of error.
	 */
	void accept(T p0, U p1, V p2, W p3) throws X;

	/**
	 * Consumes the value.
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
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default void sneakyAccept(T p0, U p1, V p2, W p3)
	{
		accept(p0, p1, p2, p3);
	}

	/**
	 * Converts this into {@link ThrowingQuadConsumer}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link ThrowingQuadConsumer} object.
	 */
	default ThrowingQuadConsumer<T, U, V, W, RuntimeException> sneakyThrowing()
	{
		return sneakyThrowing(this);
	}

	/**
	 * Converts ThrowingQuadConsumer into ThrowingQuadConsumer, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link ThrowingQuadConsumer} object.
	 *
	 * @param <T>
	 *      type of function parameter
	 * @param <U>
	 *     	type of parameter
	 * @param <V>
	 *     	type of parameter
	 * @param <W>
	 *     	type of parameter
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, U, V, W, X extends Exception> ThrowingQuadConsumer<T, U, V, W, RuntimeException> sneakyThrowing(ThrowingQuadConsumer<T, U, V, W, X> function)
	{
		// Keep this expanded so mock instances still work correctly:
		return new ThrowingQuadConsumer<T, U, V, W, RuntimeException>()
		{
			@Override
			@SneakyThrows
			public void accept(T t, U u, V v, W w)
			{
				function.accept(t, u, v, w);
			}
		};
	}
}
