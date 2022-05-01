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
import java.util.concurrent.ExecutorService;


/**
 * Closeable Executor, not actually closing underlying executor.
 */
public class NotClosingExecutor implements CloseableExecutor
{
	private final CloseableExecutor executor;

	/**
	 * Constructs new instance from {@link ExecutorService} .
	 *
	 * @param executor
	 * 	delegated {@link ExecutorService}
	 */
	public NotClosingExecutor(ExecutorService executor)
	{
		this(new ClosingExecutor(executor));
	}

	/**
	 * Constructs new instance from {@link CloseableExecutor} .
	 *
	 * @param executor
	 * 	delegated {@link CloseableExecutor}
	 */
	public NotClosingExecutor(CloseableExecutor executor)
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
		return executor.submit(callable);
	}

	@Override
	public void close()
	{
	}
}
