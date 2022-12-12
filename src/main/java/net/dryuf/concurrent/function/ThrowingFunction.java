package net.dryuf.concurrent.function;


import java.util.function.Function;


/**
 * {@link java.util.function.Function} throwing an exception.
 *
 * @param <T>
 *	parameter type
 * @param <R>
 *      return type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R>
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
	 * @throws Exception
	 * 	in case of error.
	 */
	R apply(T input) throws Exception;

	/**
	 * Function returning its input parameter as a result.
	 *
	 * @return
	 * 	the input parameter
	 *
	 * @param <T>
	 *     	type of input and output parameter
	 */
	static <T> ThrowingFunction<T, T> identity()
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
	static <T, R> ThrowingFunction<T, R> of(Function<T, R> function)
	{
		return function::apply;
	}
}
