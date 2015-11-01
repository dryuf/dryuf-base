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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * {@link ListeningScheduledExecutorService} which forwards all its action to provided {@link ScheduledExecutorService}.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class ForwardingListeningScheduledExecutorService extends ForwardingListeningExecutorService implements ListeningScheduledExecutorService
{
	public                          ForwardingListeningScheduledExecutorService(ScheduledExecutorService executor)
	{
		super(executor);
		this.executor = executor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
	{
		OneShotScheduledFutureTask<Object> task = new OneShotScheduledFutureTask<Object>(command, null) {
			@Override
			public boolean enforcedCancel() {
				return cancelled;
			}
		};
		task.setScheduledDelegate((ScheduledFuture<Object>)executor.schedule(task, delay, unit));
		return task;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ListenableScheduledFuture<V> schedule(Runnable command, V result, long delay, TimeUnit unit)
	{
		OneShotScheduledFutureTask<V> task = new OneShotScheduledFutureTask<V>(command, result) {
			@Override
			public boolean enforcedCancel() {
				return cancelled;
			}
		};
		task.setScheduledDelegate((ScheduledFuture<V>)executor.schedule(task, delay, unit));
		return task;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
	{
		OneShotScheduledFutureTask<V> task = new OneShotScheduledFutureTask<V>(callable) {
			@Override
			public boolean enforcedCancel() {
				return cancelled;
			}
		};
		task.setScheduledDelegate((ScheduledFuture<V>)executor.schedule(task, delay, unit));
		return task;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
	{
		RepeatingScheduledFutureTask<Object> task = new RepeatingScheduledFutureTask<Object>(command, null) {
			@Override
			public boolean enforcedCancel() {
				return cancelled;
			}
		};
		task.setScheduledDelegate((ScheduledFuture<Object>)executor.scheduleAtFixedRate(task, initialDelay, period, unit));
		return task;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
	{
		RepeatingScheduledFutureTask<Object> task = new RepeatingScheduledFutureTask<Object>(command, null) {
			@Override
			public boolean enforcedCancel() {
				return cancelled;
			}
		};
		task.setScheduledDelegate((ScheduledFuture<Object>)executor.scheduleWithFixedDelay(task, initialDelay, delay, unit));
		return task;
	}

	private ScheduledExecutorService executor;
}
