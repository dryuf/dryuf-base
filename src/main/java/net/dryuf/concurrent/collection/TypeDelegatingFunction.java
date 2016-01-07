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
		this.initialCallbacks = callbacks;
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

	private Function<? super T, ? extends R> mapper(Class<? extends T> clazz)
	{
		for (Map.Entry<Class<? extends T>, Function<? super T, ? extends R>> callback: initialCallbacks.entrySet()) {
			if (callback.getKey().isAssignableFrom(clazz)) {
				return callback.getValue();
			}
		}
		throw new IllegalArgumentException("Class unsupported by this caller: "+clazz);
	}

	private final Map<Class<? extends T>, Function<? super T, ? extends R>> initialCallbacks;

	private final Function<Class<? extends T>, Function<? super T, ? extends R>> typedCallbacks
			= new LazilyBuiltLoadingCache<>(this::mapper);
}
