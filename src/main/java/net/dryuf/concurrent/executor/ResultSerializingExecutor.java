/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/
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

package net.dryuf.concurrent.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * Executor serializing the results.
 */
public class ResultSerializingExecutor implements CloseableExecutor
{
	public ResultSerializingExecutor(Executor defaultExecutor)
	{
		this.defaultExecutor = defaultExecutor;
	}

	public ResultSerializingExecutor()
	{
		this(CommonPoolExecutor.getInstance());
	}

	@Override
	public void execute(Runnable runnable)
	{
		this.defaultExecutor.execute(runnable);
	}

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		ExecutionFuture<T> future = new ExecutionFuture<>();

		try {
			orderedTasks.put(future);
			future.execute(callable, defaultExecutor);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return future.wrapping;
	}

	public <T> CompletableFuture<T> submit(Callable<T> callable, Executor executor)
	{
		ExecutionFuture<T> future = new ExecutionFuture<>();

		try {
			orderedTasks.put(future);
			future.execute(callable, executor);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return future.wrapping;
	}

	@Override
	public void close() throws InterruptedException
	{
		synchronized (isEmpty) {
			if (!orderedTasks.isEmpty())
				isEmpty.wait();
		}
	}

	@SuppressWarnings("unchecked")
	private void processPending(ExecutionFuture<?> future)
	{
		if (orderedTasks.peek() != future)
			return;
		for (;;) {
			if (!PROCESSING_PENDING_UPDATER.compareAndSet(this, 0, 1))
				return;

			for (;;) {
				ExecutionFuture<Object> item = (ExecutionFuture<Object>)orderedTasks.peek();
				if (item == null || !item.isDone()) {
					PROCESSING_PENDING_UPDATER.set(this, 0);
					item = (ExecutionFuture<Object>)orderedTasks.peek();
					if (item != null && item.isDone()) {
						break;
					}
					if (item == null) {
						synchronized (isEmpty) {
							isEmpty.notify();
						}
					}
					return;
				}
				item = (ExecutionFuture<Object>)orderedTasks.remove();
				try {
					item.wrapping.complete(item.get());
				}
				catch (ExecutionException e) {
					item.wrapping.completeExceptionally(e.getCause());
				}
				catch (InterruptedException e) {
					item.wrapping.completeExceptionally(e);
				}
			}
		}
	}

	private class ExecutionFuture<T> extends CompletableFuture<T>
	{
		private CompletableFuture<Void> underlying;

		private final CompletableFuture<T> wrapping = new CompletableFuture<T>() {
			@Override
			public boolean cancel(boolean interrupt)
			{
				return underlying.cancel(interrupt);
			}
		};

		public void execute(Callable<T> callable)
		{
			underlying = CompletableFuture.runAsync(() -> {
				try {
					complete(callable.call());
				}
				catch (Throwable e) {
					completeExceptionally(e);
				}
				finally {
					processPending(ExecutionFuture.this);
				}
			});
		}

		public void execute(Callable<T> callable, Executor executor)
		{
			underlying = CompletableFuture.runAsync(() -> {
				try {
					complete(callable.call());
				}
				catch (Throwable e) {
					completeExceptionally(e);
				}
				finally {
					processPending(ExecutionFuture.this);
				}
			}, executor);
		}
	}

	private final Executor defaultExecutor;

	private final LinkedBlockingDeque<ExecutionFuture<?>> orderedTasks = new LinkedBlockingDeque<>();

	private volatile int processingPending = 0;

	private final Object isEmpty = new Object();

	private static final AtomicIntegerFieldUpdater<ResultSerializingExecutor>  PROCESSING_PENDING_UPDATER =
		AtomicIntegerFieldUpdater.newUpdater(ResultSerializingExecutor.class, "processingPending");
}
