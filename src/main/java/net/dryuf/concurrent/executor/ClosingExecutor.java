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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Closeable Executor, waiting for futures upon finish.  Also closing optionally associated resource.
 *
 * <pre>
 *         try (CloseableExecutor executor = new ClosingExecutor(Executors.newCachedThreadPool()), closeableResource) {
 *                 executor.submit(() -> doCalculation(1));
 *                 executor.submit(() -> doCalculation(2));
 *         }
 *         // at this point, both calculations are executed and finished (successfully or unsuccessfully)
 *         // executor is shutdown and closeableResource is closed after that
 * </pre>
 */
public class ClosingExecutor extends AbstractCloseableExecutor
{
	private final ExecutorService executor;

	/**
	 * Constructs the executor with delegating executor and associated resource.
	 *
	 * @param executor
	 * 	delegating executor
	 * @param resource
	 * 	associated resource, to be closed after executor is closed.
	 */
	public ClosingExecutor(ExecutorService executor, AutoCloseable resource)
	{
		super(resource);
		this.executor = executor;
	}

	/**
	 * Constructs the executor with delegating executor.
	 *
	 * @param executor
	 * 	delegating executor
	 */
	public ClosingExecutor(ExecutorService executor)
	{
		this(executor, null);
	}

	@Override
	protected void execute0(Runnable runnable)
	{
		executor.execute(runnable);
	}

	/**
	 * Closes this executor.
	 *
	 * @return
	 * 	true if executor was closed, false if it was closed already.
	 */
	@Override
	protected boolean closeExecutor()
	{
		if (super.closeExecutor()) {
			executor.shutdown();
			boolean interrupted = false;
			try {
				for (;;) {
					try {
						if (executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
							break;
						}
					}
					catch (InterruptedException e) {
						interrupted = true;
					}
				}
			}
			finally {
				if (interrupted) {
					Thread.currentThread().interrupt();
				}
			}
			return true;
		}
		return false;
	}
}
