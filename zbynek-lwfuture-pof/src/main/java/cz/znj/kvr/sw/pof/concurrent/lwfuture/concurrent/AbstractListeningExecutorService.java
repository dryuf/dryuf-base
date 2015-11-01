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


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Partial implementation of {@link ListeningExecutorService} that directs all tasks to {@link #execute(Runnable)} method.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public abstract class AbstractListeningExecutorService implements ListeningExecutorService
{
	@SuppressWarnings("unchecked")
	@Override
	public <V> List<Future<V>>      invokeAll(Collection<? extends Callable<V>> tasks) throws InterruptedException
	{
		List<ListenableFutureTask<V>> futures = wrapCallables(tasks);
		for (ListenableFutureTask<V> future: futures)
			execute(future);
		return (List<Future<V>>)(Object)futures;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> List<Future<V>>      invokeAll(Collection<? extends Callable<V>> tasks, long timeout, TimeUnit unit) throws InterruptedException
	{
		List<ListenableFutureTask<V>> futures = wrapCallables(tasks);
		final AtomicInteger counter = new AtomicInteger(tasks.size());
		synchronized (counter) {
			FutureNotifier<Future<V>> listener = new FutureNotifier<Future<V>>() {
				@Override
				public void accept(Future<V> future) {
					if (counter.decrementAndGet() == 0) {
						synchronized (counter) {
							counter.notifyAll();
						}
					}
				}
			};
			for (ListenableFutureTask<V> future : futures) {
				future.addListener(listener);
				execute(future);
			}
			counter.wait(unit.toMillis(timeout));
		}
		for (Future<V> future: futures) {
			future.cancel(true);
		}
		return (List<Future<V>>)(Object)futures;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V                    invokeAny(Collection<? extends Callable<V>> tasks) throws InterruptedException, ExecutionException
	{
		final List<ListenableFutureTask<V>> futures = wrapCallables(tasks);
		ListenableFuture<V> out = executeAny(futures);
		try {
			return out.get();
		}
		finally {
			Futures.cancelAll((List<Future<?>>)(Object)futures);
		}
	}

	@Override
	public <V> V                    invokeAny(Collection<? extends Callable<V>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		final List<ListenableFutureTask<V>> futures = wrapCallables(tasks);
		try {
			ListenableFuture<V> out = executeAny(futures);
			return out.get(timeout, unit);
		}
		finally {
			Futures.cancelAll((List<Future<?>>)(Object)futures);
		}
	}

	@Override
	public <V> ListenableFuture<V>  submit(Callable<V> callable)
	{
		final ListenableFutureTask<V> task = new ListenableFutureTask<V>(callable) {
			@Override
			protected boolean enforcedCancel() {
				return cancelled;
			}
		};
		execute(task);
		return task;
	}

	@Override
	public ListenableFuture<?>      submit(Runnable runnable)
	{
		final ListenableFutureTask<?> task = new ListenableFutureTask<Void>(runnable, null) {
			@Override
			protected boolean enforcedCancel() {
				return cancelled;
			}
		};
		execute(task);
		return task;
	}

	@Override
	public <V> ListenableFuture<V>  submit(Runnable runnable, V result)
	{
		final ListenableFutureTask<V> task = new ListenableFutureTask<V>(runnable, result) {
			@Override
			protected boolean enforcedCancel() {
				return cancelled;
			}
		};
		execute(task);
		return task;
	}

	@Override
	public List<Runnable>		shutdownCancelling()
	{
		cancelled = true;
		List<Runnable> list = shutdownNow();
		for (Runnable runnable: list) {
			if (runnable instanceof Future)
				((Future)runnable).cancel(true);
		}
		return list;
	}

	@Override
	public abstract void            execute(Runnable command);

	private <V> List<ListenableFutureTask<V>> wrapCallables(Collection<? extends Callable<V>> tasks)
	{
		List<ListenableFutureTask<V>> futures = new LinkedList<ListenableFutureTask<V>>();
		for (Callable<V> task: tasks) {
			futures.add(new ListenableFutureTask<V>(task));
		}
		return futures;
	}

	private <V> ListenableFuture<V> executeAny(List<? extends ListenableFutureTask<V>> tasks)
	{
		final SettableFuture<V> out = new SettableFuture<V>();
		final AtomicInteger failureCounter = new AtomicInteger(tasks.size());
		FutureListener<V> listener = new FutureListener<V>() {
			@Override
			public void onCancelled() {
			}

			@Override
			public void onFailure(Throwable ex) {
				if (failureCounter.decrementAndGet() == 0)
					out.setException(ex);
			}

			@Override
			public void onSuccess(V result) {
				out.set(result);
			}
		};
		for (ListenableFutureTask<V> task: tasks) {
			task.addListener(listener);
			execute(task);
		}
		return out;
	}

	protected boolean		cancelled = false;
}
