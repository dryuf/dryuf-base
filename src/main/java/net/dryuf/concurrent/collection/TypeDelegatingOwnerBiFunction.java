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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * {@link BiFunction} implementation, delegating the input to caller based on the input class. This is similar to
 * {@link TypeDelegatingFunction} but additionally allows passing {@code owner} parameter, therefore allowing statically
 * defined callback map and using it from multiple instances.
 *
 * The instantiator of this implementation must provide the mapping of input {@link Class} and processing
 * {@link BiFunction}. It is enough to pass mapping of just base classes or interfaces, this implementation will
 * determine the best match according to order of mappings in {@code callbacks} parameter (the earlier in the map takes
 * the priority - use insertion order preserving Map).
 *
 * @param <O>
 *      owner type
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
public class TypeDelegatingOwnerBiFunction<O, T, R> implements BiFunction<O, T, R>
{
	/**
	 * Creates new instance of {@link TypeDelegatingOwnerBiFunction}, initialized by list of callbacks.
	 *
	 * @param callbacks
	 * 	Map of (potentially) interfaces or superclasses to callback functions.
	 */
	public				TypeDelegatingOwnerBiFunction(
			Map<Class<? extends T>, BiFunction<O, ? super T, ? extends R>> callbacks
	)
	{
		this((Class<? extends T> clazz) -> {
			for (Map.Entry<Class<? extends T>, BiFunction<O, ? super T, ? extends R>> callback :
					callbacks.entrySet()) {
				if (callback.getKey().isAssignableFrom(clazz)) {
					return callback.getValue();
				}
			}
			throw new IllegalArgumentException("Class unsupported by this caller: "+clazz);
		});
	}

	/**
	 * Creates new instance of {@link TypeDelegatingOwnerBiFunction}, initialized by callbacks provider.
	 *
	 * @param callbacksProvider
	 * 	callback to provide processing callback based on the input class
	 */
	public				TypeDelegatingOwnerBiFunction(
			Function<Class<? extends T>, BiFunction<O, ? super T, ? extends R>> callbacksProvider
	)
	{
		this.typedCallbacks = new LazilyBuiltLoadingCache<>(callbacksProvider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public R			apply(O owner, T input)
	{
		return typedCallbacks.apply((Class<T>)input.getClass())
				.apply(owner, input);
	}

	/**
	 * Creates a new Callbacks builder.
	 *
	 * @param <O>
	 *      type of owner
	 * @param <T>
	 *      type of input
	 * @param <R>
	 *      return type
	 *
	 * @return
	 * 	callback builder
	 */
	public static <O, T, R> CallbacksBuilder<O, T, R> callbacksBuilder()
	{
		return new CallbacksBuilder<>();
	}

	/**
	 * Callbacks builder temporary holder.
	 *
	 * @param <O>
	 *      type of owner
	 * @param <T>
	 *      type of input
	 * @param <R>
	 *      return type
	 */
	public static class CallbacksBuilder<O, T, R>
	{
		/**
		 * Builds a Map of callbacks mapping.
		 *
		 * @return
		 * 	new Map of callbacks mapping.
		 */
		public Map<Class<? extends T>, BiFunction<O, ? super T, ? extends R>> build()
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
		public <I extends T> CallbacksBuilder<O, T, R> add(
				Class<I> clazz,
				BiFunction<O, ? super I, ? extends R> callback
		)
		{
			this.callbacks.merge(
					clazz,
					(BiFunction<O, ? super T, ? extends R>)callback,
					(key, value) -> {
						throw new IllegalArgumentException("Callback already provided for: "+
								key);
					}
			);
			return this;
		}

		private Map<Class<? extends T>, BiFunction<O, ? super T, ? extends R>> callbacks
				= new LinkedHashMap<>();
	}

	private final Function<Class<? extends T>, BiFunction<O, ? super T, ? extends R>> typedCallbacks;
}
