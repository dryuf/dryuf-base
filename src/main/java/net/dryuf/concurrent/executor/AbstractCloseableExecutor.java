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

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * Closeable Executor which does not close, waiting for futures upon finish and closing associated resource.
 */
@RequiredArgsConstructor
public abstract class AbstractCloseableExecutor implements CloseableExecutor
{
	static int PENDING_MAX = Integer.MAX_VALUE;

	private final AutoCloseable resource;

	private volatile int pending = 0;

	private static final AtomicIntegerFieldUpdater<AbstractCloseableExecutor> PENDING_UPDATER =
		AtomicIntegerFieldUpdater.newUpdater(AbstractCloseableExecutor.class, "pending");

	protected AbstractCloseableExecutor()
	{
		this(null);
	}

	@Override
	public void execute(Runnable runnable)
	{
		for (;;) {
			int old = pending;
			if ((old&Integer.MAX_VALUE) == PENDING_MAX) {
				synchronized (this) {
					if ((pending&Integer.MAX_VALUE) == PENDING_MAX) {
						try {
							do {
								wait();
								old = pending;
							} while ((old&Integer.MAX_VALUE) == PENDING_MAX);
							if ((old&Integer.MIN_VALUE) != 0) {
								notifyAll();
								throw new RejectedExecutionException("Executor closed");
							}
							// Notify other threads if there is room for more executions:
							if (PENDING_UPDATER.compareAndSet(this, old, old+1)) {
								if (old+1 < PENDING_MAX) {
									notify();
								}
								break;
							}
							else {
								notify();
							}
						}
						catch (InterruptedException e) {
							throw new RejectedExecutionException(e);
						}
					}
				}
			}
			else if ((old&Integer.MIN_VALUE) != 0) {
				throw new RejectedExecutionException("Executor closed");
			}
			else if (PENDING_UPDATER.compareAndSet(this, old, old+1)) {
				break;
			}
		}
		try {
			execute0(() -> {
				try {
					runnable.run();
				}
				finally {
					int old = PENDING_UPDATER.getAndDecrement(this);
					if ((old&Integer.MIN_VALUE) != 0 || (old&Integer.MAX_VALUE) == PENDING_MAX) {
						synchronized (this) {
							this.notify();
						}
					}
				}
			});
		}
		catch (Throwable ex) {
			int old = PENDING_UPDATER.getAndDecrement(this);
			if ((old&Integer.MIN_VALUE) != 0 || (old&Integer.MAX_VALUE) == PENDING_MAX) {
				synchronized (this) {
					this.notify();
				}
			}
			throw ex;
		}
	}

	protected abstract void execute0(Runnable runnable);

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		CompletableFutureTask<T> task = new CompletableFutureTask<>(callable);
		execute(task);
		return task;
	}

	/**
	 * Closes this executor.
	 *
	 * @return
	 * 	true if executor was closed, false if it was closed already.
	 */
	protected boolean closeExecutor()
	{
		boolean interrupted = false;
		try {
			int old = PENDING_UPDATER.getAndUpdate(this, v -> v|Integer.MIN_VALUE);
			if ((old&Integer.MIN_VALUE) == 0) {
				synchronized (this) {
					for (;;) {
						if ((pending&Integer.MAX_VALUE) != 0) {
							try {
								wait();
								// Notify execute waiting for a slot (should not happen
								// unless there is close and execute in parallel)
								notify();
							}
							catch (InterruptedException ex) {
								interrupted = true;
							}
						}
						else {
							break;
						}
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
		finally {
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Waits for the current executions and closes associated resource (in this order).
	 * The close() calls will be executed only once, even when this method is called several times.
	 *
	 * If interrupted, the method sets interrupted flag on this thread.
	 */
	@Override
	public void close()
	{
		boolean closedNow = true;
		try {
			closedNow = closeExecutor();
		}
		finally {
			if (closedNow) {
				try {
					if (resource != null) {
						resource.close();
					}
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

}
