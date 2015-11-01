/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr@centrum.cz http://kvr.znj.cz/ http://github.com/kvr000/
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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;


/**
 * Listenable Future extends regular {@link Future} by allowing registering listeners for task completion
 * and additionally allowing delay of cancellation notification.
 *
 * @param <V>
 * 	Future result type
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public interface ListenableFuture<V> extends Future<V>
{
	/**
	 * Requests potential {@link Future#cancel(boolean) cancel} notification to be postponed
	 * until the task actually finishes.
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             setDelayedCancel();

	/**
	 * Rejects potential {@link Future#cancel(boolean) cancellations}.
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             setUncancellable();

	/**
	 * Registers new listener as {@link Runnable} instance.
	 *
	 * @param listener
	 * 	listener to be called when future is done
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addListener(Runnable listener);

	/**
	 * Registers new listener as {@link FutureNotifier} instance.
	 *
	 * @param listener
	 * 	listener to be called when future is done, getting the {@link Future} as a parameter
	 *
	 * @return this instance
	 */
	<FT extends Future<V>> ListenableFuture<V> addListener(FutureNotifier<FT> listener);

	/**
	 * Registers new listener as {@link FutureListener} instance.
	 *
	 * @param listener
	 * 	listener to be called when future is done
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addListener(FutureListener<V> listener);

	/**
	 * Registers new listener as separated success, failure and cancel callbacks.
	 *
	 * In case any of the callbacks is null, it will be skipped while executing the listeners
	 * for that particular notification.
	 *
	 * @param successListener
	 * 	listener to be called when future successfully finishes, can be null
	 * @param failureListener
	 * 	listener to be called when future finishes with failure, can be null
	 * @param cancelListener
	 * 	listener to be called when future is cancelled, can be null
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addListener(SuccessListener<V> successListener, FailureListener failureListener, CancelListener cancelListener);

	/**
	 * Registers new listener as {@link Runnable} instance.
	 *
	 * @param listener
	 * 	listener to be called when future is done
	 * @param executor
	 *      executor which will execute listener
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addAsyncListener(Runnable listener, Executor executor);

	/**
	 * Registers new listener as {@link FutureNotifier} instance.
	 *
	 * @param listener
	 * 	listener to be called when future is done, getting the {@link Future} as a parameter
	 * @param executor
	 *      executor which will execute listener
	 *
	 * @return this instance
	 */
	<FT extends Future<V>> ListenableFuture<V> addAsyncListener(FutureNotifier<FT> listener, Executor executor);

	/**
	 * Registers new listener as {@link FutureListener} instance.
	 *
	 * @param listener
	 * 	listener to be called when future is done
	 * @param executor
	 *      executor which will execute listener
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addAsyncListener(FutureListener<V> listener, Executor executor);

	/**
	 * Registers new listener as separated success, failure and cancel callbacks.
	 *
	 * In case any of the callbacks is null, it will be skipped while executing the listeners
	 * for that particular notification.
	 *
	 * @param successListener
	 * 	listener to be called when future successfully finishes, can be null
	 * @param failureListener
	 * 	listener to be called when future finishes with failure, can be null
	 * @param cancelListener
	 * 	listener to be called when future is cancelled, can be null
	 * @param executor
	 *      executor which will execute listener
	 *
	 * @return
	 *      this instance
	 */
	ListenableFuture<V>             addAsyncListener(SuccessListener<V> successListener, FailureListener failureListener, CancelListener cancelListener, Executor executor);
}
