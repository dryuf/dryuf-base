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

package net.dryuf.base.function.delegate;

import net.dryuf.base.collection.LazilyBuiltLoadingCache;
import net.dryuf.base.function.ThrowingTriFunction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * {@link ThrowingTriFunction} implementation, delegating the input to caller based on the input class. This is similar to
 * {@link TypeDelegatingFunction} but allows passing parameters, therefore allowing statically defined callback map and
 * using it from multiple instances.  Typically, the first parameter is the {@code this} class, therefore this
 * implementation decides based on the second parameter.
 *
 * The instantiator of this implementation must provide the mapping of input {@link Class} and processing
 * {@link ThrowingTriFunction}. It is enough to pass mapping of just base classes or interfaces, this implementation will
 * determine the best match according to order of mappings in {@code callbacks} parameter (the earlier in the map takes
 * the priority - use insertion order preserving Map).
 *
 * The callback is determined based on the type of the third parameter to function.
 *
 * <pre>
	public static class MyProcessor
	{
		private static final TypeDelegatingTriFunction3&lt;MyProcessor, Additional, Input, Result, RuntimeException&gt; processingFunctions =
				TypeDelegatingTriFunction3.&lt;MyProcessor, Additional, Input, Result, RuntimeException&gt;callbacksBuilder()
					.add(First.class, MyProcessor::processFirst)
					.add(Second.class, MyProcessor::processSecond)
					.build();

		public Result process(Additional p1, Input input)
		{
			return processingFunctions.apply(this, p1, input);
		}

		// The First can be also FirstImpl implements First
		private Result processFirst(Additional p1, First input)
		{
			return new Result(input.getFirstValue());
		}

		// The Second can be also SecondImpl implements Second
		private Result processSecond(Additional p1, Second input)
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
public class TypeDelegatingTriFunction3<T, U, V, R, X extends Exception> implements ThrowingTriFunction<T, U, V, R, X>
{
	private final Function<Class<? extends V>, ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>> typedCallbacks;

	/**
	 * Creates new instance of {@link TypeDelegatingTriFunction3}, initialized by list of callbacks.
	 *
	 * @param callbacks
	 * 	Map of (potentially) interfaces or superclasses to callback functions.
	 */
	public TypeDelegatingTriFunction3(
		Map<Class<? extends V>, ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>> callbacks
	)
	{
		this((Class<? extends V> clazz) -> {
			for (Map.Entry<Class<? extends V>,
				ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>> callback :
					callbacks.entrySet()) {
				if (callback.getKey().isAssignableFrom(clazz)) {
					return callback.getValue();
				}
			}
			throw new IllegalArgumentException("Class unsupported by this caller: "+clazz);
		});
	}

	/**
	 * Creates new instance of {@link TypeDelegatingTriFunction3}, initialized by callbacks provider.
	 *
	 * @param callbacksProvider
	 * 	callback to provide processing callback based on the input class
	 */
	public TypeDelegatingTriFunction3(
		Function<Class<? extends V>, ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>> callbacksProvider
	)
	{
		this.typedCallbacks = new LazilyBuiltLoadingCache<>(callbacksProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public R apply(T p1, U p2, V p3) throws X
	{
		return typedCallbacks.apply((Class<V>) p3.getClass())
				.apply(p1, p2, p3);
	}

	/**
	 * Creates a new Callbacks builder.
	 *
	 * @param <T>
	 *      type of parameter 1
	 * @param <U>
	 *      type of parameter 2
	 * @param <V>
	 *      type of parameter 3
	 * @param <R>
	 *      return type
	 * @param <X>
	 *      type of thrown exception
	 *
	 * @return
	 * 	callback builder
	 */
	public static <T, U, V, R, X extends Exception> CallbacksBuilder<T, U, V, R, X> callbacksBuilder()
	{
		return new CallbacksBuilder<>();
	}

	/**
	 * Callbacks builder temporary holder.
	 *
	 * @param <T>
	 *      type of parameter 1
	 * @param <U>
	 *      type of parameter 2
	 * @param <V>
	 *      type of parameter 3
	 * @param <R>
	 *      return type
	 * @param <X>
	 *      type of thrown exception
	 */
	public static class CallbacksBuilder<T, U, V, R, X extends Exception>
	{
		private Map<Class<? extends V>, ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>> callbacks =
			new LinkedHashMap<>();

		/**
		 * Builds a TypeDelegatingOwnerTriFunction1 from provided callbacks.
		 *
		 * @return
		 * 	new TypeDelegatingOwnerTriFunction1 based on callbacks.
		 */
		public TypeDelegatingTriFunction3<T, U, V, R, X> build()
		{
			return new TypeDelegatingTriFunction3<>(callbacks);
		}

		/**
		 * Builds a Map of callbacks mapping.
		 *
		 * @return
		 * 	new Map of callbacks mapping.
		 */
		public Map<Class<? extends V>, ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>> buildMap()
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
		public <I extends V> CallbacksBuilder<T, U, V, R, X> add(
				Class<I> clazz,
				ThrowingTriFunction<? super T, ? super U, ? super I, ? extends R, ? extends X> callback
		)
		{
			this.callbacks.merge(
					clazz,
					(ThrowingTriFunction<? super T, ? super U, ? super V, ? extends R, X>)callback,
					(key, value) -> {
						throw new IllegalArgumentException("Callback already provided for: "+key);
					}
			);
			return this;
		}
	}
}
