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

package net.dryuf.base.concurrent.future;

import lombok.SneakyThrows;
import net.dryuf.base.concurrent.executor.CompletableFutureTask;
import net.dryuf.base.function.ThrowingBiConsumer;
import net.dryuf.base.function.ThrowingConsumer;
import net.dryuf.base.function.ThrowingFunction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * Future utilities for CompletableFuture.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class FutureUtil
{
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
		return new CompletableFutureTask<T>(callable) {
			{
				try {
					CompletableFuture.runAsync(this);
				}
				catch (Throwable ex) {
					completeExceptionally(ex);
				}
			}
		};
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
		return new CompletableFutureTask<T>(callable) {
			{
				try {
					executor.execute(this);
				}
				catch (Throwable ex) {
					completeExceptionally(ex);
				}
			}
		};
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
	public static <T, X extends Throwable, E extends Exception> BiConsumer<T, X> whenException(ThrowingConsumer<X, E> consumer)
	{
		return ThrowingBiConsumer.sneaky((v, ex) -> {
			if (ex != null)
				consumer.accept(ex);
		});
	}

	/**
	 * Gets future result or throws exception cause.
	 *
	 * @param future
	 * 	the future
	 * @param <V>
	 *      type of future
	 *
	 * @return
	 * 	future result.
	 *
	 * @apiNote
	 * 	throws causing exception or InterruptedException if waiting was interrupted.
	 *
	 */
	@SneakyThrows
	public static <V, X extends Exception> V sneakyGet(CompletableFuture<V> future) throws X
	{
		try {
			return future.get();
		}
		catch (ExecutionException e) {
			throw e.getCause();
		}
		catch (InterruptedException e) {
			throw e;
		}
	}

	/**
	 * Gets future result or throws exception cause.
	 *
	 * @param future
	 * 	the future
	 * @param <V>
	 *      type of future
	 *
	 * @return
	 * 	future result.
	 *
	 * @apiNote
	 * 	throws causing exception or InterruptedException if waiting was interrupted.
	 *
	 */
	@SneakyThrows
	public static <V, X extends Exception> V sneakyGetNow(CompletableFuture<V> future, V valueIfAbsent) throws X
	{
		try {
			return future.getNow(valueIfAbsent);
		}
		catch (CompletionException e) {
			throw e.getCause();
		}
	}

	/**
	 * Propagates CompletableFuture to existing CompletableFuture.
	 *
	 * @param source
	 * 	source future
	 * @param target
	 * 	target future
	 * @param <V>
	 *      type of future
	 */
	public static <V> void copy(CompletableFuture<V> source, CompletableFuture<V> target)
	{
		source.whenComplete((v, ex) -> FutureUtil.completeOrFail(target, v, ex));
	}

	/**
	 * Joins two CompletableFuture objects, propagating first exception or last result.
	 *
	 * @param one
	 * 	first future
	 * @param two
	 * 	second future
	 * @param cancelling
	 * 	whether to propagate cancellation to original futures
	 * @param <V>
	 *      type of futures
	 *
	 * @return
	 * 	type of futures
	 */
	public static <V> CompletableFuture<V> join(CompletableFuture<V> one, CompletableFuture<V> two, boolean cancelling)
	{
		return new CompletableFuture<V>() {
			{
				AtomicInteger count = new AtomicInteger(2);
				BiConsumer<V, Throwable> listener = (v, ex) -> {
					if (ex == null) {
						if (count.decrementAndGet() == 0)
							complete(v);
					}
					else {
						completeExceptionally(ex);
						cancel(true);
					}
				};
				one.whenComplete(listener);
				two.whenComplete(listener);
			}

			@Override
			public boolean cancel(boolean interrupt)
			{
				if (cancelling) {
					try {
						return one.cancel(interrupt)|two.cancel(interrupt);
					}
					finally {
						super.cancel(interrupt);
					}
				}
				else {
					return super.cancel(interrupt);
				}
			}
		};
	}

	/**
	 * Adds handler to CompletableFuture chain, no matter whether it is successful or failed.
	 *
	 * @param source
	 * 	original future
	 * @param handler
	 * 	result processing handler
	 * @param <V>
	 *      type of original future
	 * @param <R>
	 *      type of returned future
	 *
	 * @return
	 * 	CompletableFuture representing either the original exception or return value from handler.
	 */
	public static <V, R> CompletableFuture<R> composeAlways(CompletableFuture<V> source, Callable<CompletableFuture<R>> handler)
	{
		return new CompletableFuture<R>()
		{
			{
				source.whenComplete((v, ex) -> {
					try {
						handler.call().whenComplete((v2, ex2) -> {
							if (ex != null) {
								completeExceptionally(ex);
							}
							else if (ex2 != null) {
								completeExceptionally(ex2);
							}
							else {
								complete(v2);
							}
						});
					}
					catch (Throwable ex2) {
						completeExceptionally(ex != null ? ex : ex2);
					}
				});
			}
		};
	}

	/**
	 * Adds handler to CompletableFuture chain, no matter whether it is successful or failed.  If original future
	 * fails, the value passed to handler will be null.
	 *
	 * @param source
	 * 	original future
	 * @param handler
	 * 	result processing handler
	 * @param <V>
	 *      type of original future
	 * @param <R>
	 *      type of returned future
	 * @param <X>
	 *      type of thrown exception by handler
	 *
	 * @return
	 * 	CompletableFuture representing either the original exception or return value from handler.
	 */
	public static <V, R, X extends Exception> CompletableFuture<R> composeAlways(CompletableFuture<V> source, ThrowingFunction<V, CompletableFuture<R>, X> handler)
	{
		return new CompletableFuture<R>()
		{
			{
				source.whenComplete((v, ex) -> {
					try {
						handler.apply(v).whenComplete((v2, ex2) -> {
							if (ex != null) {
								completeExceptionally(ex);
							}
							else if (ex2 != null) {
								completeExceptionally(ex2);
							}
							else {
								complete(v2);
							}
						});
					}
					catch (Throwable ex2) {
						completeExceptionally(ex != null ? ex : ex2);
					}
				});
			}
		};
	}

	/**
	 * Waits for object notification uninterruptibly, reporting interrupt via return value.  The object must be
	 * synchronized upon entry.
	 *
	 * @param lock
	 * 	the lock object
	 *
	 * @return
	 * 	true if wait was interrupted, false otherwise.
	 */
	public static boolean waitUninterruptibly(Object lock)
	{
		boolean interrupted = false;
		for (;;) {
			try {
				lock.wait();
				break;
			}
			catch (InterruptedException e) {
				interrupted = true;
			}
		}
		return interrupted;
	}

	/**
	 * Waits for object notification uninterruptibly, setting interrupt via Thread.interrupt.  The object must be
	 * synchronized upon entry.
	 *
	 * @param lock
	 * 	the lock object
	 */
	public static void waitUninterruptiblyKeepInterrupt(Object lock)
	{
		if (waitUninterruptibly(lock)) {
			Thread.currentThread().interrupt();
		}
	}


	/**
	 * Converts List of CompletableFuture objects into one CompletableFuture completing once all original futures
	 * are completed.  It cancels all original futures or closes underlying AutoCloseable when some of them fails.
	 *
	 * @param futures
	 * 	list of futures
	 * @param <T>
	 *      type of futures result
	 *
	 * @return
	 * 	future of list containing results of original futures.
	 */
	public static <T extends AutoCloseable> CompletableFuture<List<T>> nestedAllOrCancel(List<CompletableFuture<T>> futures)
	{
		if (futures.size() == 0) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		AtomicInteger remaining = new AtomicInteger(futures.size());

		return new CompletableFuture<List<T>>() {
			{
				futures.forEach(f -> {
					f.whenComplete((v, ex) -> {
						if (ex != null) {
							completeExceptionally(ex);
						}
						if (remaining.decrementAndGet() == 0) {
							stepInner();
						}
					});
				});
				whenComplete((v, ex) -> {
					if (ex != null) {
						futures.forEach(f -> {
							f.cancel(true);
							f.thenAccept(sf -> {
								try {
									sf.close();
								}
								catch (Exception e) {
									// ignore;
								}
							});
						});
					}
				});
			}

			private void stepInner()
			{
				complete(futures.stream()
						.map(CompletableFuture::join)
						.collect(Collectors.toList())
				);
			}

			@Override
			public boolean cancel(boolean interrupt)
			{
				futures.forEach((future) -> {
					future.cancel(interrupt);
					future.thenAccept(sf -> {
						try {
							sf.close();
						}
						catch (Exception e) {
						}
					});
				});
				return super.cancel(interrupt);
			}
		};
	}
}
