package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Future;
import java.util.function.Function;


/**
 * Listenable Future extends regular Future by allowing registering listeners for task completion and additionally allowing delay of cancel method.
 *
 * @param <V>
 *         Future result type
 */
public interface ListenableFuture<V> extends Future<V>
{
	/**
	 * Requests potential cancel notification to be postponed until the task actually finishes.
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             setDelayedCancel();

	/**
	 * Registers new listener as Runnable instance.
	 *
	 * @param listener
	 *      listener to be called when future is done
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>		addListener(Runnable listener);

	/**
	 * Registers new listener as Function instance.
	 *
	 * @param listener
	 *      listener to be called when future is done, getting the Future as a parameter
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addListener(Function<Future<V>, Void> listener);

	/**
	 * Registers new listener as FutureListener instance.
	 *
	 * @param listener
	 *      listener to be called when future is done
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addListener(FutureListener<V> listener);
}
