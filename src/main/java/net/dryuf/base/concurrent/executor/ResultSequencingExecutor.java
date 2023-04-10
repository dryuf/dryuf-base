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
 * Executor sequencing the results.
 *
 * The results are completed in submission order and completion handlers executed sequentially in this order.
 *
 * Usage:
 *
 * <pre>
 *         try (ResultSequencingExecutor executor = new ResultSequencingExecutor()) {
 *            	CompletableFuture future = executor.submit(() -> 5*5))
 *            		.thenRun((v) -> System.out.println("Completed: " + v));
 *              CompletableFuture future = executor.submit(() -> 6*6))
 *                 	.thenRun((v) -> System.out.println("Completed: " + v));
 *              // the above will always print 25 and then 36
 *         }
 * </pre>
 */
public class ResultSequencingExecutor implements CloseableExecutor
{
	/**
	 * Creates instance from executor, closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 */
	public ResultSequencingExecutor(CloseableExecutor executor)
	{
		this.executor = executor;
	}

	/**
	 * Creates instance from executor, not closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 */
	public ResultSequencingExecutor(Executor executor)
	{
		this(new UncontrolledCloseableExecutor(executor));
	}

	/**
	 * Creates instance from common pool executor.
	 */
	public ResultSequencingExecutor()
	{
		this(CommonPoolExecutor.getInstance());
	}

	@Override
	public void execute(Runnable runnable)
	{
		this.executor.execute(runnable);
	}

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
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
	public void close()
	{
		boolean interrupted = false;
		try {
			synchronized (isEmpty) {
				for (; ; ) {
					try {
						if (!orderedTasks.isEmpty())
							isEmpty.wait();
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

	private final CloseableExecutor executor;

	private final LinkedBlockingDeque<ExecutionFuture<?>> orderedTasks = new LinkedBlockingDeque<>();

	private volatile int processingPending = 0;

	private final Object isEmpty = new Object();

	private static final AtomicIntegerFieldUpdater<ResultSequencingExecutor>  PROCESSING_PENDING_UPDATER =
		AtomicIntegerFieldUpdater.newUpdater(ResultSequencingExecutor.class, "processingPending");
}
