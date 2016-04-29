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

package net.dryuf.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Future utilities for CompletableFuture.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr000@gmail.com http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class FutureUtil
{
	public static <T> CompletableFuture<T> toCompletable(ListenableFuture<T> listenable)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		listenable.addListener(new FutureListener<T>()
		{
			@Override
			public void onCancelled()
			{
				future.cancel(true);
			}

			@Override
			public void onFailure(Throwable ex)
			{
				future.completeExceptionally(ex);
			}

			@Override
			public void onSuccess(T result)
			{
				future.complete(result);
			}
		});
		return future;
	}

	/**
	 * Returns future which is already completed exceptionally.
	 *
	 * @param ex
	 * 	exception cause the failure
	 * @param <T>
	 *      type of future
	 *
	 * @return
	 * 	exceptionally completed future.
	 */
	public static <T> CompletableFuture<T> exception(Throwable ex)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(ex);
		return future;
	}

	/**
	 * Runs futures and cancels them when any of them exits.
	 *
	 * @param futures
	 * 	list of futures to monitor and cancel
	 *
	 * @param <T>
	 * 	type of Future
	 *
	 * @return
	 * 	new Future monitoring all underlying futures.
	 */
	public static <T> CompletableFuture<T> anyAndCancel(List<CompletableFuture<T>> futures)
	{
		AtomicBoolean cancelledStatus = new AtomicBoolean();

		CompletableFuture<T> result = new CompletableFuture<T>() {
			@Override
			public boolean cancel(boolean interrupt)
			{
				if (cancelledStatus.compareAndSet(false, true)) {
					futures.forEach(future -> future.cancel(true));
					return super.cancel(interrupt);
				}
				return false;
			}
		};
		BiConsumer<T, Throwable> completor = (v, ex) -> {
			if (cancelledStatus.compareAndSet(false, true)) {
				futures.forEach(future -> future.cancel(true));
				completeOrFail(result, v, ex);
			}
		};
		futures.forEach(future -> future.whenComplete(completor));
		return result;
	}

	/**
	 * Submits callable directly.  Executes the Callable and creates a CompletableFuture from the result.
	 *
	 * @param callable
	 * 	code to execute
	 * @param <T>
	 *      type of result
	 *
	 * @return
	 * 	the future for result
	 */
	public static <T> CompletableFuture<T> submitDirect(Callable<T> callable)
	{
		try {
			return CompletableFuture.completedFuture(callable.call());
		}
		catch (Throwable ex) {
			return exception(ex);
		}
	}

	/**
	 * Submits callable asynchronously.  The method reports failure via future even when executor is closed.
	 *
	 * @param callable
	 * 	code to execute
	 * @param <T>
	 *      type of result
	 *
	 * @return
	 * 	the future for result
	 */
	public static <T> CompletableFuture<T> submitAsync(Callable<T> callable)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		try {
			CompletableFuture.runAsync(() -> {
				try {
					future.complete(callable.call());
				}
				catch (Throwable ex) {
					future.completeExceptionally(ex);
				}
			});
		}
		catch (Throwable ex) {
			return exception(ex);
		}
		return future;
	}

	/**
	 * Submits callable asynchronously.  The method reports failure via future even when executor is closed.
	 *
	 * @param callable
	 * 	code to execute
	 * @param executor
	 * 	executor to execute the code
	 * @param <T>
	 *      type of result
	 *
	 * @return
	 * 	the future for result
	 */
	public static <T> CompletableFuture<T> submitAsync(Callable<T> callable, Executor executor)
	{
		CompletableFuture<T> future = new CompletableFuture<>();
		try {
			executor.execute(() -> {
				try {
					future.complete(callable.call());
				}
				catch (Throwable ex) {
					future.completeExceptionally(ex);
				}
			});
		}
		catch (Throwable ex) {
			return exception(ex);
		}
		return future;
	}

	/**
	 * Completes or fails the other Future, based on provided result.
	 *
	 * @param future
	 * 	future to update
	 * @param value
	 * 	value to set as a result, in case ex is null
	 * @param ex
	 * 	exception to fail the future, in case ex is not null
	 * @param <T>
	 *      type of future
	 *
	 * @return
	 * 	the same future
	 */
	public static <T> CompletableFuture<T> completeOrFail(CompletableFuture<T> future, T value, Throwable ex)
	{
		if (ex != null)
			future.completeExceptionally(ex);
		else
			future.complete(value);
		return future;
	}

	/**
	 * Wraps exception consumer into {@link CompletableFuture#whenComplete(BiConsumer)} consumer.
	 *
	 * @param consumer
	 * 	exception consumer
	 * @param <T>
	 *      type of Future
	 * @param <X>
	 *      type of consumed exception
	 *
	 * @return
	 * 	Future.whenComplete BiConsumer calling provided consumer only in case of failure.
	 */
	public static <T, X extends Throwable> BiConsumer<T, X> whenException(Consumer<X> consumer)
	{
		return (v, ex) -> {
			if (ex != null)
				consumer.accept(ex);
		};
	}
}
