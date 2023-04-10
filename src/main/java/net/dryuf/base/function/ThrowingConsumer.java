package net.dryuf.base.function;

import lombok.SneakyThrows;

import java.util.function.Consumer;


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
		return sneaky(this);
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
		// Keep this expanded so mock instances still work correctly:
		return new Consumer<T>()
		{
			@Override
			@SneakyThrows
			public void accept(T v)
			{
				function.accept(v);
			}
		};
	}

	/**
	 * Converts ThrowingConsumer into ThrowingConsumer, propagating exceptions silently.
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
	 *      exception declared on returned function
	 * @param <OX>
	 *      potential exception thrown by original function
	 */
	@SuppressWarnings("unchecked")
	static <T, X extends Exception, OX extends Exception> ThrowingConsumer<T, X> sneakyThrowing(ThrowingConsumer<T, OX> function)
	{
		return (ThrowingConsumer<T, X>) function;
	}

	/**
	 * Converts ThrowingConsumer into ThrowingConsumer, propagating exceptions silently.
	 *
	 * @param function
	 * 	original function
	 *
	 * @return
	 * 	converted {@link Consumer} object.
	 *
	 * @param <T>
	 *      type of function parameter
	 * @param <OX>
	 *      potential exception thrown by original function
	 */
	@SuppressWarnings("unchecked")
	static <T, OX extends Exception> ThrowingConsumer<T, RuntimeException> sneakyRuntime(ThrowingConsumer<T, OX> function)
	{
		return sneakyThrowing(function);
	}
}
