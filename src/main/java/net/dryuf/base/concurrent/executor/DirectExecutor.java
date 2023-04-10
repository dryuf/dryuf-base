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
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Executor} executing {@link Runnable} directly in this thread.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class DirectExecutor implements CloseableExecutor
{
	/**
	 * Gets instance of {@link DirectExecutor}.
	 *
	 * @return
	 * 	single instance of {@link DirectExecutor}
	 */
	public static DirectExecutor	getInstance()
	{
		return instance;
	}

	public void			execute(Runnable runnable)
	{
		try {
			runnable.run();
		}
		catch (RuntimeException ex) {
			logger.log(Level.SEVERE, "DirectExecutor: Runnable raised RuntimeException while executing: " +
				runnable.getClass().getName(), ex);
		}
	}

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		CompletableFutureTask<T> task = new CompletableFutureTask<>(callable);
		execute(task);
		return task;
	}

	/**
	 * The function does nothing - DirectExecutor never closes.  If you want closeable instance, use
	 * {@link ClosingExecutor} wrapper.
	 */
	@Override
	public void close()
	{
	}

	private static DirectExecutor	instance = new DirectExecutor();

	private static Logger		logger = Logger.getLogger(DirectExecutor.class.getName());
}
