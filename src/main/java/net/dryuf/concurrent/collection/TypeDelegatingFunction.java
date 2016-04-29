/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr000@gmail.com http://kvr.znj.cz/ http://github.com/kvr000/
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * {@link Function} implementation, delegating the input to caller based on the input class.
 *
 * The instantiator of this implementation must provide the mapping of input {@link Class} and processing
 * {@link Function}. It is enough to pass mapping of just base classes or interfaces, this implementation will determine
 * the best match according to order of mappings in {@code callbacks} parameter (the earlier takes the priority - use
 * insertion order preserving Map).
 *
 * @param <T>
 * 	common ancestor of input
 * @param <R>
 *      type of expected result
 *
 * @apiNote thread safe
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr000@gmail.com http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class TypeDelegatingFunction<T, R> implements Function<T, R>
{
	/**
	 * Creates new instance of {@link TypeDelegatingFunction}, initialized by list of callbacks.
	 *
	 * @param callbacks
	 * 	Map of (potentially) interfaces or superclasses to callback functions.
	 */
	public				TypeDelegatingFunction(
			Map<Class<? extends T>, Function<? super T, ? extends R>> callbacks
	)
	{
		this((Class<? extends T> clazz) -> {
			for (Map.Entry<Class<? extends T>, Function<? super T, ? extends R>> callback: callbacks.entrySet()) {
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
	public				TypeDelegatingFunction(
			Function<Class<? extends T>, Function<? super T, ? extends R>> callbacksProvider
	)
	{
		this.typedCallbacks = new LazilyBuiltLoadingCache<>(callbacksProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public R			apply(T input)
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
	public static <T, R> CallbacksBuilder<T, R> callbacksBuilder()
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
	public static class CallbacksBuilder<T, R>
	{
		/**
		 * Builds a Map of callbacks mapping.
		 *
		 * @return
		 * 	new Map of callbacks mapping.
		 */
		public Map<Class<? extends T>, Function<? super T, ? extends R>> build()
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
		public <I extends T> CallbacksBuilder<T, R> add(
				Class<I> clazz,
				Function<? super I, ? extends R> callback
		)
		{
			this.callbacks.merge(
					clazz,
					(Function<T, ? extends R>)callback,
					(key, value) -> {
						throw new IllegalArgumentException("Callback already provided for: "+
								key);
					}
			);
			return this;
		}

		private Map<Class<? extends T>, Function<? super T, ? extends R>> callbacks = new LinkedHashMap<>();
	}

	private final Function<Class<? extends T>, Function<? super T, ? extends R>> typedCallbacks;
}
