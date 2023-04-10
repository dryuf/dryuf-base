package net.dryuf.base.function;

import lombok.SneakyThrows;

import java.util.function.BiConsumer;


/**
 * {@link BiConsumer} throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <U>
 *	parameter type
 * @param <X>
 *	thrown exception
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
@FunctionalInterface
public interface ThrowingBiConsumer<T, U, X extends Exception>
{
	/**
	 * Consumes the value.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 *
	 * @throws X
	 * 	in case of error.
	 */
	void accept(T p0, U p1) throws X;

	/**
	 * Consumes the value.
	 *
	 * @param p0
	 * 	input parameter
	 * @param p1
	 * 	input parameter
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default void sneakyAccept(T p0, U p1)
	{
		accept(p0, p1);
	}

	/**
	 * Converts this into {@link BiConsumer}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link BiConsumer} object.
	 */
	default BiConsumer<T, U> sneaky()
	{
		return sneaky(this);
	}

	/**
	 * Converts {@link BiConsumer} to {@link ThrowingBiConsumer} .
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
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, U, X extends Exception> ThrowingBiConsumer<T, U, X> of(BiConsumer<T, U> function)
	{
		return function::accept;
	}

	/**
	 * Converts ThrowingFunction into Function, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link BiConsumer} object.
	 *
	 * @param <T>
	 *      type of function parameter
	 * @param <U>
	 *     	type of parameter
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, U, X extends Exception> BiConsumer<T, U> sneaky(ThrowingBiConsumer<T, U, X> function)
	{
		// Keep this expanded so mock instances still work correctly:
		return new BiConsumer<T, U>()
		{
			@Override
			@SneakyThrows
			public void accept(T t, U u)
			{
				function.accept(t, u);
			}
		};
	}
}
