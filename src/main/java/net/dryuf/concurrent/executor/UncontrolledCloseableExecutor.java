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

package net.dryuf.concurrent.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * Closeable Executor, having no control over executions, just passing them through.
 *
 * This is useful for CloseableExecutor wrappers when no additional control is required over already executed tasks.
 *
 * <pre>
 *         try (CloseableExecutor executor = new UncontrolledCloseableExecutor(Executors.commonPool())) {
 *                 executor.submit(() -> doCalculation(1));
 *                 executor.submit(() -> doCalculation(2));
 *         }
 *         // at this point, both calculations are submitted but not necessarily executed or finished.
 * </pre>
 */
public class UncontrolledCloseableExecutor implements CloseableExecutor
{
	private static final AtomicIntegerFieldUpdater<UncontrolledCloseableExecutor> CLOSED_UPDATER = AtomicIntegerFieldUpdater.newUpdater(
		UncontrolledCloseableExecutor.class, "closed"
	);

	private final Executor executor;

	private volatile int closed = 0;

	/**
	 * Constructs the executor with delegating executor.
	 *
	 * @param executor
	 * 	delegating executor
	 */
	public UncontrolledCloseableExecutor(Executor executor)
	{
		this.executor = executor;
	}

	@Override
	public void execute(Runnable runnable)
	{
		executor.execute(runnable);
	}

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		CompletableFutureTask<T> task = new CompletableFutureTask<>(callable);
		execute(task);
		return task;
	}

	@Override
	public void close()
	{
	}
}
