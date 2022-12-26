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

package net.dryuf.concurrent;

import java.util.concurrent.Callable;


/**
 * ListenableScheduledFuture implementation that is used to wrap {@link java.util.concurrent.ScheduledFuture} method results.
 *
 * @param <V>
 * 	future return type
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class RepeatingScheduledFutureTask<V> extends OneShotScheduledFutureTask<V>
{
	/**
	 * Constructs new instance with {@link Runnable} reference and provided {@code result}.
	 *
	 * @param runnable
	 * 	function to run
	 * @param result
	 * 	result of future
	 */
	public                          RepeatingScheduledFutureTask(final Runnable runnable, V result)
	{
		super(runnable, result);
	}

	/**
	 * Constructs new instance with {@link java.util.concurrent.Callable} reference.
	 *
	 * @param callable
	 * 	function to compute the result
	 */
	public                          RepeatingScheduledFutureTask(final Callable<V> callable)
	{
		super(callable);
	}

	@Override
	public void                     run()
	{
		try {
			myThread = Thread.currentThread();
			if (setRunning()) {
				callable.call();
				if (enforcedCancel()) {
					setCancelled();
					callable = null;
				}
				else {
					if (!setRestart())
						callable = null;
				}
			}
		}
		catch (Throwable ex) {
			callable = null;
			if (enforcedCancel())
				setCancelled();
			else
				setException(ex);
			if (ex instanceof Error)
				throw (Error)ex;
			throw ex instanceof RuntimeException ? (RuntimeException)ex : new RuntimeException(ex);
		}
	}
}
