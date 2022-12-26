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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


/**
 * Closeable Executor, not actually closing underlying executor.
 *
 * <pre>
 *         try (CloseableExecutor executor = new NotClosingExecutor(sharedExecutor), closeableResource) {
 *                 executor.submit(() -> doCalculation(1));
 *                 executor.submit(() -> doCalculation(2));
 *         }
 *         // at this point, both calculations are executed and finished (successfully or unsuccessfully)
 *         // and closeableResource is closed after that.  sharedExecutor is still running.
 * </pre>
 */
public class NotClosingExecutor extends AbstractCloseableExecutor
{
	private final Executor executor;

	/**
	 * Constructs new instance from {@link ExecutorService} .
	 *
	 * @param executor
	 * 	delegated {@link ExecutorService}
	 */
	public NotClosingExecutor(Executor executor)
	{
		this(executor, null);
	}

	/**
	 * Constructs new instance from {@link CloseableExecutor} .
	 *
	 * @param executor
	 * 	delegated {@link CloseableExecutor}
	 * @param resource
	 * 	associated resource, closed upon close
	 */
	public NotClosingExecutor(Executor executor, AutoCloseable resource)
	{
		super(resource);
		this.executor = executor;
	}

	@Override
	protected void execute0(Runnable runnable)
	{
		executor.execute(runnable);
	}
}
