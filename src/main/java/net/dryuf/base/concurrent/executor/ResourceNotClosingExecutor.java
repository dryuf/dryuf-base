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


/**
 * Closeable Executor, waiting for futures upon finish, closing associated resource, NOT closing delegate executor.
 *
 * <pre>
 *         try (CloseableExecutor executor = new ResourceClosingExecutor(delegateExecutor, closeableResource) {
 *                 executor.submit(() -> doCalculation(1));
 *                 executor.submit(() -> doCalculation(2));
 *         }
 *         // at this point, both calculations are executed and finished (successfully or unsuccessfully)
 *         // delegateExecutor is running and closeableResource is closed after that
 * </pre>
 */
public class ResourceNotClosingExecutor extends AbstractCloseableExecutor
{
	/** Underlying executor. */
	protected final CloseableExecutor executor;

	/**
	 * Constructs the executor with delegating executor and associated resource.
	 *
	 * @param executor
	 * 	delegating executor
	 * @param resource
	 * 	associated resource, to be closed after executor is closed.
	 */
	public ResourceNotClosingExecutor(CloseableExecutor executor, AutoCloseable resource)
	{
		super(resource);
		this.executor = executor;
	}

	@Override
	protected void execute0(Runnable r)
	{
		executor.execute(r);
	}
}
