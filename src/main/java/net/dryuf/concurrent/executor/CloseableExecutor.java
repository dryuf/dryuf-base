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


/**
 * Closeable Executor, waiting for executions upon finish.
 *
 * <pre>
 *         try (CloseableExecutor executor = new ClosingExecutor(Executors.newCachedThreadPool())) {
 *                 executor.submit(() -> doCalculation(1));
 *                 executor.submit(() -> doCalculation(2));
 *         }
 *         // at this point, both calculations are executed and finished (successfully or unsuccessfully)
 *         try (CloseableExecutor executor = new NotClosingExecutor(sharedExecutor)) {
 *                 executor.submit(() -> doCalculation(1));
 *                 executor.submit(() -> doCalculation(2));
 *         }
 *         // at this point, both calculations are executed and finished (successfully or unsuccessfully)
 * </pre>
 */
public interface CloseableExecutor extends AutoCloseable, Executor
{
	@Override
	void execute(Runnable runnable);

	/**
	 * Submits new task.
	 *
	 * @param callable
	 * 	task callable
	 *
	 * @return
	 * 	CompletableFuture completing when task finishes
	 *
	 * @param <T>
	 *      type of result
	 */
	<T> CompletableFuture<T> submit(Callable<T> callable);

	/**
	 * Closes this {@link CloseableExecutor}, waits for current executions to be completed and potentially shuts
	 * down the underlying executor if specified by implementation.
	 *
	 * The method is always successful.  If interrupted, it will set interrupted flag on current thread.
	 */
	@Override
	void close();
}
