/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dryuf.concurrent.collection;

import net.dryuf.concurrent.function.ThrowingFunction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * {@link ThrowingFunction} implementation, delegating the input to caller based on the input class.
 *
 * The instantiator of this implementation must provide the mapping of input {@link Class} and processing
 * {@link ThrowingFunction}. It is enough to pass mapping of just base classes or interfaces, this implementation will
 * determine the best match according to order of mappings in {@code callbacks} parameter (the earlier takes the
 * priority - use insertion order preserving Map).
 *
 * <pre>
	public static class MyProcessor
	{
		private static final TypeDelegatingFunction<Input, Result, RuntimeException> processingFunctions =
			TypeDelegatingFunction.<Input, Result, RuntimeException>callbacksBuilder()
				.add(First.class, MyProcessor::processFirst)
				.add(Second.class, MyProcessor::processSecond)
				.build();

		public Result process(Input input)
		{
			return processingFunctions.apply(input);
		}

		// The First can be also FirstImpl implements First
		private static Result processFirst(First input)
		{
			return new Result(input.getFirstValue());
		}

		// The Second can be also SecondImpl implements Second
		private static Result processSecond(Second input)
		{
			return new Result(input.getSecondValue());
		}
	}
 * </pre>
 *
 * @param <T>
 * 	common ancestor of input
 * @param <R>
 *      type of expected result
 *
 * @apiNote thread safe
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class TypeDelegatingFunction<T, R, X extends Exception> implements ThrowingFunction<T, R, X>
{
	private final Function<Class<? extends T>, ThrowingFunction<? super T, ? extends R, X>> typedCallbacks;

	/**
	 * Creates new instance of {@link TypeDelegatingFunction}, initialized by list of callbacks.
	 *
	 * @param callbacks
	 * 	Map of (potentially) interfaces or superclasses to callback functions.
	 */
	public TypeDelegatingFunction(
			Map<Class<? extends T>, ThrowingFunction<? super T, ? extends R, X>> callbacks
	)
	{
		this((Class<? extends T> clazz) -> {
			for (Map.Entry<Class<? extends T>, ThrowingFunction<? super T, ? extends R, X>> callback: callbacks.entrySet()) {
				if (callback.getKey().isAssignableFrom(clazz)) {
					return callback.getValue();
				}
			}
			throw new IllegalArgumentException("Class unsupported by this caller: "+clazz);
		});
	}

	/**
	 * Creates new instance of {@link TypeDelegatingFunction}, initialized by list of callbacks.
	 *
	 * @param callbacksProvider
	 * 	callback to provide processing callback based on the input class
	 */
	public TypeDelegatingFunction(
			Function<Class<? extends T>, ThrowingFunction<? super T, ? extends R, X>> callbacksProvider
	)
	{
		this.typedCallbacks = new LazilyBuiltLoadingCache<>(callbacksProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public R			apply(T input) throws X
	{
		return typedCallbacks.apply((Class<T>)input.getClass())
				.apply(input);
	}

	/**
	 * Creates a new Callbacks builder.
	 *
	 * @param <T>
	 *      type of input
	 * @param <R>
	 *      return type
	 *
	 * @return
	 * 	callback builder
	 */
	public static <T, R, X extends Exception> CallbacksBuilder<T, R, X> callbacksBuilder()
	{
		return new CallbacksBuilder<>();
	}

	/**
	 * Callbacks builder temporary holder.
	 *
	 * @param <T>
	 *      type of input
	 * @param <R>
	 *      return type
	 */
	public static class CallbacksBuilder<T, R, X extends Exception>
	{
		/**
		 * Builds a TypeDelegatingFunction from provided callbacks.
		 *
		 * @return
		 * 	new TypeDelegatingFunction based on callbacks.
		 */
		public TypeDelegatingFunction<T, R, X> build()
		{
			return new TypeDelegatingFunction<>(callbacks);
		}

		/**
		 * Builds a Map of callbacks mapping.
		 *
		 * @return
		 * 	new Map of callbacks mapping.
		 */
		public Map<Class<? extends T>, ThrowingFunction<? super T, ? extends R, X>> buildMap()
		{
			return callbacks;
		}

		/**
		 * Registers new callback mapping.
		 *
		 * @param clazz
		 * 	type of input
		 * @param callback
		 * 	callback to handle the type
		 * @param <I>
		 *      type of input
		 *
		 * @return
		 * 	this builder.
		 */
		@SuppressWarnings("unchecked")
		public <I extends T> CallbacksBuilder<T, R, X> add(
				Class<I> clazz,
				ThrowingFunction<? super I, ? extends R, X> callback
		)
		{
			this.callbacks.merge(
					clazz,
					(ThrowingFunction<? super T, ? extends R, X>) callback,
					(key, value) -> {
						throw new IllegalArgumentException("Callback already provided for: "+
								key);
					}
			);
			return this;
		}

		private Map<Class<? extends T>, ThrowingFunction<? super T, ? extends R, X>> callbacks =
			new LinkedHashMap<>();
	}
}
