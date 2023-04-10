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

package net.dryuf.base.concurrent.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Executor sequencing the results and controlling pending items by capacity and count.
 *
 * Executor takes item capacity and count as parameters and blocks execution until sufficient resources are available.
 *
 * The results are completed in submission order and completion handlers executed sequentially in this order.
 *
 * Usage:
 *
 * <pre>
 *         // Controlling pending execution by size of available memory and maximum 128 items in queue
 *         try (CapacityResultSequencingExecutor executor = new CapacityResultSequencingExecutor(Runtime.getRuntime().maxMemory()*7/8, 128)) {
 *         	byte[] content = getContent();
 *              CompletableFuture future = executor.submit(content.length, 1);
 *         }
 * </pre>
 */
public class CapacityResultSequencingExecutor implements AutoCloseable
{
	/**
	 * Creates instance from executor, closing it at close.
	 *
	 * @param capacity
	 * 	max capacity of the executor
	 * @param count
	 * 	max number of pending items
	 * @param executor
	 * 	executor, closed at close
	 */
	public CapacityResultSequencingExecutor(long capacity, long count, CloseableExecutor executor)
	{
		this.capacity = capacity;
		this.count = count;
		this.executor = executor;
		this.orderedTasks = new LinkedBlockingDeque<>();
	}

	/**
	 * Creates instance from common pool.
	 *
	 * @param capacity
	 * 	max capacity of the executor
	 * @param count
	 * 	max number of pending items
	 */
	public CapacityResultSequencingExecutor(long capacity, long count)
	{
		this(capacity, count, CommonPoolExecutor.getInstance());
	}

	/**
	 * Creates instance from executor, not closing it at close.
	 *
	 * @param capacity
	 * 	max capacity of the executor
	 * @param count
	 * 	max number of pending items
	 * @param executor
	 * 	executor, closed at close
	 */
	public CapacityResultSequencingExecutor(long capacity, long count, Executor executor)
	{
		this(capacity, count, new NotClosingExecutor(executor));
	}

	public <T> CompletableFuture<T> submit(long capacity, Callable<T> callable)
	{
		ExecutionFuture<T> future = new ExecutionFuture<>(capacity);
		addFuture(capacity, future);
		future.execute(callable, executor);
		return future.wrapping;
	}

	public <T> CompletableFuture<T> submit(long capacity, Callable<T> callable, Executor executor)
	{
		ExecutionFuture<T> future = new ExecutionFuture<>(capacity);
		addFuture(capacity, future);
		future.execute(callable, executor);
		return future.wrapping;
	}

	@Override
	public void close()
	{
		boolean interrupted = false;
		try {
			synchronized (isEmptySync) {
				for (; ; ) {
					try {
						if (!orderedTasks.isEmpty())
							isEmptySync.wait();
						break;
					}
					catch (InterruptedException ex) {
						interrupted = true;
					}
				}
			}
		}
		finally {
			executor.close();
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	private synchronized void addFuture(long capacity, ExecutionFuture<?> future)
	{
		for (;;) {
			if ((this.count <= 0 || capacity > this.capacity) && !orderedTasks.isEmpty()) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				this.capacity -= capacity;
				this.count -= 1;
				try {
					orderedTasks.put(future);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				break;
			}
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
				ExecutionFuture<Object> item = (ExecutionFuture<Object>) orderedTasks.peek();
				if (item == null || !item.isDone()) {
					PROCESSING_PENDING_UPDATER.set(this, 0);
					item = (ExecutionFuture<Object>) orderedTasks.peek();
					if (item != null && item.isDone()) {
						break;
					}
					if (item == null) {
						synchronized (isEmptySync) {
							isEmptySync.notify();
						}
					}
					return;
				}
				try {
					item.wrapping.complete(item.get());
				}
				catch (ExecutionException e) {
					item.wrapping.completeExceptionally(e.getCause());
				}
				catch (InterruptedException e) {
					item.wrapping.completeExceptionally(e);
				}
				finally {
					synchronized (this) {
						this.capacity += item.capacity;
						this.count += 1;
						this.notify();
					}
					orderedTasks.remove();
				}
			}
		}
	}

	private class ExecutionFuture<T> extends CompletableFuture<T>
	{
		private final long capacity;

		private CompletableFuture<Void> underlying;

		private final CompletableFuture<T> wrapping = new CompletableFuture<T>() {
			@Override
			public boolean cancel(boolean interrupt)
			{
				return underlying.cancel(interrupt);
			}
		};

		public ExecutionFuture(long capacity)
		{
			this.capacity = capacity;
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

	private long capacity;

	private long count;

	private final CloseableExecutor executor;

	private final LinkedBlockingDeque<ExecutionFuture<?>> orderedTasks;

	private volatile int processingPending = 0;

	private final Object isEmptySync = new Object();

	private static final AtomicIntegerFieldUpdater<CapacityResultSequencingExecutor>  PROCESSING_PENDING_UPDATER =
			AtomicIntegerFieldUpdater.newUpdater(CapacityResultSequencingExecutor.class, "processingPending");
}

