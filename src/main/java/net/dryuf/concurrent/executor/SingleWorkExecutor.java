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

import net.dryuf.concurrent.function.ThrowingFunction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


/**
 * Executor accepting work items, processing each item in its run.
 *
 * <pre>
 *         try (WorkExecutor&lt;Integer, Integer&gt; executor = new SingleWorkExecutor&lt;&gt;(delegateExecutor, v -> v*v)) {
 *         	CompletableFuture&lt;Integer&gt; v1 = executor.submit(1);
 *         	CompletableFuture&lt;Integer&gt; v5 = executor.submit(5);
 *         	assertEquals(v1.get(), 1);
 *         	assertEquals(v5.get(), 25);
 *         }
 *         // at this point, all executions are finished (successfully or not), underlying executor closed if closeable
 * </pre>
 *
 * @param <T>
 *	work item
 * @param <R>
 *      result
 */
public class SingleWorkExecutor<T, R> implements WorkExecutor<T, R>
{
	private final CloseableExecutor executor;

	private final ThrowingFunction<T, R, ? extends Exception> processor;

	/**
	 * Creates instance from {@link CloseableExecutor}, closing it upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 * @param processor
	 * 	processing function
	 */
	public SingleWorkExecutor(CloseableExecutor executor, ThrowingFunction<T, R, ? extends Exception> processor)
	{
		this.executor = executor;
		this.processor = processor;
	}

	/**
	 * Creates instance from {@link ExecutorService}, not shutting it down upon close.
	 *
	 * @param executor
	 * 	underlying executor
	 * @param processor
	 * 	processing function
	 */
	public SingleWorkExecutor(ExecutorService executor, ThrowingFunction<T, R, ? extends Exception> processor)
	{
		this(new NotClosingExecutor(executor), processor);
	}

	@Override
	public CompletableFuture<R> submit(T work)
	{
		return executor.submit(() -> processor.apply(work));
	}

	@Override
	public void close()
	{
		executor.close();
	}
}
