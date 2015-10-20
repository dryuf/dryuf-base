/*
 * Copyright 2015 Zbynek Vyskovsky http://kvr.znj.cz/ http://github.com/kvr000/
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
