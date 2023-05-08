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
import net.dryuf.base.function.ThrowingConsumer;
import net.dryuf.base.function.ThrowingFunction;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * Executor sequencing the results.
 *
 * The results are completed in submission order and passed completion handlers executed sequentially in this order.
 *
 * Note that there is no guarantee about order of CompletableFuture callbacks calls as the futures may be completed
 * before the submit() returns and callback being added executed immediately from caller thread.  Therefore, only the
 * completion handler should be used to sequentially completed the results.  This is why this class does not provide
 * the execute() and simple submit() method and always requires explicit completor handler.
 *
 * Usage:
 *
 * <pre>
 *         try (ResultSequencingExecutor executor = new ResultSequencingExecutor()) {
 *            	CompletableFuture future = executor.submit(
 *              	() -> { Thread.sleep(100); return 5*5; }
 *                      (r) -> System.out.println("Completed: " + r));
 *            	CompletableFuture future = executor.submit(
 *              	() -> 6*6,
 *                      (r) -> System.out.println("Completed: " + r));
 *         }
 *         // the above will always print 25 and then 36
 * </pre>
 */
public class ResultSequencingExecutor implements AutoCloseable
{
	/**
	 * Creates instance from executor, closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 */
	public <X extends Exception> ResultSequencingExecutor(CloseableExecutor executor)
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

	/**
	 * Executes the task and runs completor in order of original submissions.
	 *
	 * @param callable
	 *      the task to be executed
	 * @param completor
	 *      handler processing the result
	 *
	 * @return
	 *      future returning result from completor
	 *
	 * @param <T>
	 *      type of task result
	 * @param <R>
	 *      type of completor result
	 * @param <X>
	 *      type of exception thrown from completor
	 */
	public <T, R, X extends Exception> CompletableFuture<R> submit(
		Callable<T> callable,
		ThrowingFunction<T, R, X> completor)
	{
		Objects.requireNonNull(callable, "callable");
		Objects.requireNonNull(completor, "completor");
		ExecutionFuture<T, R, X> future = new ExecutionFuture<>(completor);

		try {
			orderedTasks.put(future);
			future.execute(callable, executor);
		}
		catch (InterruptedException e) {
			throw new RejectedExecutionException(e);
		}
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
				item = (ExecutionFuture<Object, Object, Exception>) orderedTasks.remove();
				try {
					Object taskResult = FutureUtil.sneakyGet(item);
					item.wrapping.complete(item.completor.apply(taskResult));
				}
				catch (Throwable e) {
					item.wrapping.completeExceptionally(e);
				}
			}
		}
	}

	@RequiredArgsConstructor
	private class ExecutionFuture<T, R, X extends Exception> extends CompletableFuture<T>
	{
		private CompletableFuture<Void> underlying;

		private final ThrowingFunction<T, R, X> completor;

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

	private final CloseableExecutor executor;

	private final LinkedBlockingDeque<ExecutionFuture<?, ?, ?>> orderedTasks = new LinkedBlockingDeque<>();

	private volatile int processingPending = 0;

	private final Object isEmptySync = new Object();

	private static final AtomicIntegerFieldUpdater<ResultSequencingExecutor>  PROCESSING_PENDING_UPDATER =
		AtomicIntegerFieldUpdater.newUpdater(ResultSequencingExecutor.class, "processingPending");
}
