/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr@centrum.cz http://kvr.znj.cz/ http://github.com/kvr000/
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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * ListenableScheduledFuture implementation that is used to wrap {@link java.util.concurrent.ScheduledFuture} method results.
 *
 * @param <V>
 * 	future return type
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class OneShotScheduledFutureTask<V> extends AbstractScheduledFuture<V> implements RunnableFuture<V>
{
	/**
	 * Constructs new instance with {@link Runnable} reference and provided {@code result}.
	 *
	 * @param runnable
	 * 	function to run
	 * @param result
	 * 	provided result
	 */
	public OneShotScheduledFutureTask(final Runnable runnable, final V result)
	{
		this(new Callable<V>()
		{
			@Override
			public V call() throws Exception
			{
				runnable.run();
				return result;
			}
		});
	}

	/**
	 * Constructs new instance with {@link java.util.concurrent.Callable} reference.
	 *
	 * @param callable
	 * 	function to compute the result
	 */
	public OneShotScheduledFutureTask(final Callable<V> callable)
	{
		this.callable = callable;
	}

	@Override
	public long                     getDelay(TimeUnit unit)
	{
		return scheduledDelegate.getDelay(unit);
	}

	/**
	 * Sets original JDK {@link ScheduledFuture}.
	 *
	 * @param scheduledDelegate
	 * 	JDK future
	 */
	public void                     setScheduledDelegate(ScheduledFuture<V> scheduledDelegate)
	{
		this.scheduledDelegate = scheduledDelegate;
	}

	@Override
	protected void                  interruptTask()
	{
		myThread.interrupt();
		this.scheduledDelegate.cancel(true);
	}

	protected boolean		enforcedCancel()
	{
		return false;
	}

	@Override
	public void                     run()
	{
		try {
			myThread = Thread.currentThread();
			if (setRunning()) {
				V result = callable.call();
				if (enforcedCancel())
					setCancelled();
				else
					set(result);
			}
		}
		catch (Throwable ex) {
			if (enforcedCancel())
				setCancelled();
			else
				setException(ex);
			if (ex instanceof Error)
				throw (Error)ex;
		}
	}

	private ScheduledFuture<V>	scheduledDelegate;

	/**
	 * The thread that executes the task.
	 *
	 * Volatile is not needed as this is surrounded with other memory barrier reads/writes.
	 */
	protected Thread    		myThread;

	/**
	 * Callable performing the task.
	 */
	protected final Callable<V>	callable;
}
