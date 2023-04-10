package net.dryuf.base.function;

import lombok.SneakyThrows;

/**
 * {@link Runnable} throwing an exception.
 *
 * @param <X>
 *	thrown exception
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
@FunctionalInterface
public interface ThrowingRunnable<X extends Exception>
{
	/**
	 * Executes runnable.
	 *
	 * @throws X
	 * 	in case of error.
	 */
	void run() throws X;

	/**
	 * Runs the runnable, silently propagating exceptions.
	 *
	 * @apiNote throws X
	 * 	in case of error.
	 */
	@SneakyThrows
	default void sneakyRun()
	{
		run();
	}

	/**
	 * Converts this into Runnable, propagating exceptions silently.
	 *
	 * @return
	 * 	converted {@link Runnable} object.
	 */
	default Runnable sneaky()
	{
		// Keep this expanded so mock instances still work correctly:
		return sneaky(this);
	}

	/**
	 * Converts {@link Runnable} to {@link ThrowingRunnable} .
	 *
	 * @param runnable
	 * 	original {@link Runnable}.
	 *
	 * @return
	 * 	throwing runnable wrapper
	 */
	static ThrowingRunnable<RuntimeException> of(Runnable runnable)
	{
		return runnable::run;
	}

	/**
	 * Converts ThrowingRunnable into Runnable, propagating exceptions silently.
	 *
	 * @param runnable
	 * 	original runnable
	 *
	 * @return
	 * 	converted {@link Runnable} object.
	 *
	 * @param <X>
	 *      potential exception thrown by original runnable
	 */
	static <X extends Exception> Runnable sneaky(ThrowingRunnable<X> runnable)
	{
		// Keep this expanded so mock instances still work correctly:
		return new Runnable()
		{
			@Override
			@SneakyThrows
			public void run()
			{
				runnable.run();
			}
		};
	}
}
