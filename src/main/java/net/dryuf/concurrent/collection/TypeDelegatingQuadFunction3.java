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

import net.dryuf.concurrent.function.ThrowingQuadFunction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * {@link ThrowingQuadFunction} implementation, delegating the input to caller based on the input class. This is similar to
 * {@link TypeDelegatingFunction} but allows passing parameters, therefore allowing statically defined callback map and
 * using it from multiple instances.  Typically, the first parameter is the {@code this} class, therefore this
 * implementation decides based on the second parameter.
 *
 * The instantiator of this implementation must provide the mapping of input {@link Class} and processing
 * {@link ThrowingQuadFunction}. It is enough to pass mapping of just base classes or interfaces, this implementation will
 * determine the best match according to order of mappings in {@code callbacks} parameter (the earlier in the map takes
 * the priority - use insertion order preserving Map).
 *
 * The callback is determined based on the type of the third parameter to function.
 *
 * <pre>
	public static class MyProcessor
	{
		private static final TypeDelegatingQuadFunction3<MyProcessor, Additional, Input, Additional2, Result, RuntimeException> processingFunctions =
				TypeDelegatingQuadFunction3.<MyProcessor, Additional, Input, Additional2, Result, RuntimeException>callbacksBuilder()
					.add(First.class, MyProcessor::processFirst)
					.add(Second.class, MyProcessor::processSecond)
					.build();

		public Result process(Additional p2, Input input, Additional2 p3)
		{
			return processingFunctions.apply(this, p2, input, p3);
		}

		// The First can be also FirstImpl implements First
		private Result processFirst(Additional p2, First input, Additional2 p3)
		{
			return new Result(input.getFirstValue());
		}

		// The Second can be also SecondImpl implements Second
		private Result processSecond(Additional p2, Second input, Additional2 p3)
		{
			return new Result(input.getSecondValue());
		}
	}
 * </pre>
 *
 * @param <T>
 * 	type of parameter 1
 * @param <U>
 * 	type of parameter 2
 * @param <V>
 * 	type of parameter 3 and also type of ancestor of determining classes
 * @param <W>
 * 	type of parameter 4
 * @param <R>
 *      type of expected result
 * @param <X>
 *      type of thrown exception
 *
 * @apiNote thread safe
 *
 * @author
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class TypeDelegatingQuadFunction3<T, U, V, W, R, X extends Exception> implements ThrowingQuadFunction<T, U, V, W, R, X>
{
	private final Function<Class<? extends V>, ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>> typedCallbacks;

	/**
	 * Creates new instance of {@link TypeDelegatingQuadFunction3}, initialized by list of callbacks.
	 *
	 * @param callbacks
	 * 	Map of (potentially) interfaces or superclasses to callback functions.
	 */
	public TypeDelegatingQuadFunction3(
		Map<Class<? extends V>, ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>> callbacks
	)
	{
		this((Class<? extends V> clazz) -> {
			for (Map.Entry<Class<? extends V>,
				ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>> callback :
					callbacks.entrySet()) {
				if (callback.getKey().isAssignableFrom(clazz)) {
					return callback.getValue();
				}
			}
			throw new IllegalArgumentException("Class unsupported by this caller: "+clazz);
		});
	}

	/**
	 * Creates new instance of {@link TypeDelegatingQuadFunction3}, initialized by callbacks provider.
	 *
	 * @param callbacksProvider
	 * 	callback to provide processing callback based on the input class
	 */
	public TypeDelegatingQuadFunction3(
		Function<Class<? extends V>, ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>> callbacksProvider
	)
	{
		this.typedCallbacks = new LazilyBuiltLoadingCache<>(callbacksProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public R apply(T p1, U p2, V p3, W p4) throws X
	{
		return typedCallbacks.apply((Class<V>)p3.getClass())
				.apply(p1, p2, p3, p4);
	}

	/**
	 * Creates a new Callbacks builder.
	 *
	 * @param <T>
	 * 	type of parameter 1
	 * @param <U>
	 * 	type of parameter 2
	 * @param <V>
	 * 	type of parameter 3 and also type of ancestor of determining classes
	 * @param <W>
	 * 	type of parameter 4
	 * @param <R>
	 *      type of expected result
	 * @param <X>
	 *      type of thrown exception
	 *
	 * @return
	 * 	callback builder
	 */
	public static <T, U, V, W, R, X extends Exception> CallbacksBuilder<T, U, V, W, R, X> callbacksBuilder()
	{
		return new CallbacksBuilder<>();
	}

	/**
	 * Callbacks builder temporary holder.
	 *
	 * @param <T>
	 * 	type of parameter 1
	 * @param <U>
	 * 	type of parameter 2
	 * @param <V>
	 * 	type of parameter 3 and also type of ancestor of determining classes
	 * @param <W>
	 * 	type of parameter 4
	 * @param <R>
	 *      type of expected result
	 * @param <X>
	 *      type of thrown exception
	 */
	public static class CallbacksBuilder<T, U, V, W, R, X extends Exception>
	{
		private Map<Class<? extends V>, ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>> callbacks =
			new LinkedHashMap<>();

		/**
		 * Builds a TypeDelegatingOwnerQuadFunction1 from provided callbacks.
		 *
		 * @return
		 * 	new TypeDelegatingOwnerQuadFunction1 based on callbacks.
		 */
		public TypeDelegatingQuadFunction3<T, U, V, W, R, X> build()
		{
			return new TypeDelegatingQuadFunction3<>(callbacks);
		}

		/**
		 * Builds a Map of callbacks mapping.
		 *
		 * @return
		 * 	new Map of callbacks mapping.
		 */
		public Map<Class<? extends V>, ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>> buildMap()
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
		public <I extends V> CallbacksBuilder<T, U, V, W, R, X> add(
				Class<I> clazz,
				ThrowingQuadFunction<? super T, ? super U, ? super I, ? super W, ? extends R, ? extends X> callback
		)
		{
			this.callbacks.merge(
					clazz,
					(ThrowingQuadFunction<? super T, ? super U, ? super V, ? super W, ? extends R, X>)callback,
					(key, value) -> {
						throw new IllegalArgumentException("Callback already provided for: "+key);
					}
			);
			return this;
		}
	}
}
