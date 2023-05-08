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

import lombok.RequiredArgsConstructor;
import net.dryuf.base.concurrent.future.FutureUtil;
import net.dryuf.base.function.ThrowingFunction;

import java.util.Objects;
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
 * The results are completed in submission order and completor executed sequentially in this order.
 *
 * While the completors are executed sequentially, the same is not true for returned CompletableFuture handlers, as
 * the handlers may be added after the future is already completed and executed in parallel with executor thread
 * pools.  Therefore this Executor does not provide standard submit and execute methods to avoid confusion.
 *
 * Usage:
 *
 * <pre>
 *         // Controlling pending execution by size of available memory and maximum 128 items in queue
 *         try (CapacityResultSequencingExecutor executor = new CapacityResultSequencingExecutor(Runtime.getRuntime().maxMemory()*7/8, 128)) {
 *         	byte[] content1 = getContent1();
 *              CompletableFuture future1 = executor.submit(content1.length, () -> processContent(content1), result -> writeResult(result));
 *         	byte[] content2 = getContent2();
 *              CompletableFuture future2 = executor.submit(content2.length, () -> processContent(content2), result -> writeResult(result));
 *         }
 *         // The above execution will be limited by content lengths - if both fit within the limit, they will execute in parallel.
 *         // While both tasks may execute in parallel, the writeResult calls will be called sequentially, with content1 first,
 *         // even if processContent for content1 completed later
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

	public <T, R, X extends Exception> CompletableFuture<R> submit(
		long capacity,
		Callable<T> callable,
		ThrowingFunction<T, R, X> completor)
	{
		Objects.requireNonNull(callable, "callable");
		Objects.requireNonNull(completor, "completor");
		ExecutionFuture<T, R, X> future = new ExecutionFuture<>(completor, capacity);
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

	private synchronized void addFuture(long capacity, ExecutionFuture<?, ?, ?> future)
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
	private void processPending(ExecutionFuture<?, ?, ?> future)
	{
		if (orderedTasks.peek() != future)
			return;
		for (;;) {
			if (!PROCESSING_PENDING_UPDATER.compareAndSet(this, 0, 1))
				return;

			for (;;) {
				ExecutionFuture<Object, Object, Exception> item =
					(ExecutionFuture<Object, Object, Exception>) orderedTasks.peek();
				if (item == null || !item.isDone()) {
					PROCESSING_PENDING_UPDATER.set(this, 0);
					item = (ExecutionFuture<Object, Object, Exception>) orderedTasks.peek();
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
					Object taskResult = FutureUtil.sneakyGet(item);
					item.wrapping.complete(item.completor.apply(taskResult));
				}
				catch (Throwable e) {
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

	@RequiredArgsConstructor
	private class ExecutionFuture<T, R, X extends Exception> extends CompletableFuture<T>
	{
		private final ThrowingFunction<T, R, X> completor;

		private final long capacity;

		private CompletableFuture<Void> underlying;

		private final CompletableFuture<R> wrapping = new CompletableFuture<R>() {
			@Override
			public boolean cancel(boolean interrupt)
			{
				return underlying.cancel(interrupt);
			}
		};

		public void execute(Callable<T> callable, CloseableExecutor executor)
		{
			underlying = executor.submit(() -> {
					try {
						complete(callable.call());
					}
					catch (Throwable e) {
						completeExceptionally(e);
					}
					finally {
						processPending(ExecutionFuture.this);
					}
					return null;
				});
		}
	}

	private long capacity;

	private long count;

	private final CloseableExecutor executor;

	private final LinkedBlockingDeque<ExecutionFuture<?, ?, ?>> orderedTasks;

	private volatile int processingPending = 0;

	private final Object isEmptySync = new Object();

	private static final AtomicIntegerFieldUpdater<CapacityResultSequencingExecutor>  PROCESSING_PENDING_UPDATER =
			AtomicIntegerFieldUpdater.newUpdater(CapacityResultSequencingExecutor.class, "processingPending");
}

