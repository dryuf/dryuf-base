package net.dryuf.concurrent.function;

import lombok.SneakyThrows;

import java.util.function.Consumer;
import java.util.function.Function;


/**
 * {@link Consumer} throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <X>
 *	thrown exception
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
@FunctionalInterface
public interface ThrowingConsumer<T, X extends Exception>
{
	/**
	 * Consumes the value.
	 *
	 * @param input
	 * 	input parameter
	 *
	 * @throws X
	 * 	in case of error.
	 */
	void accept(T input) throws X;

	/**
	 * Consumes the value.
	 *
	 * @param input
	 * 	input parameter
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default void sneakyAccept(T input)
	{
		accept(input);
	}

	/**
	 * Converts this into {@link Consumer}, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link Consumer} object.
	 */
	default Consumer<T> sneaky()
	{
		return this::sneakyAccept;
	}

	/**
	 * Converts {@link Consumer} to {@link ThrowingConsumer} .
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	throwing function wrapper
	 *
	 * @param <T>
	 *     	type of parameter
	 * @param <X>
	 *      type of thrown exception
	 */
	static <T, X extends Exception> ThrowingConsumer<T, X> of(Consumer<T> function)
	{
		return function::accept;
	}

	/**
	 * Converts ThrowingConsumer into Consumer, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Consumer} object.
	 *
	 * @param <T>
	 *      type of function parameter
	 * @param <X>
	 *      potential exception thrown by original function
	 */
	static <T, X extends Exception> Consumer<T> sneaky(ThrowingConsumer<T, X> function)
	{
		return function.sneaky();
	}
}
