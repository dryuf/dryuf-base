package net.dryuf.concurrent.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * CompletableFuture runnable task, interruptable.
 *
 * @param <T>
 *   	type of result
 */
public class CompletableFutureTask<T> extends CompletableFuture<T> implements Runnable
{
	private final Callable<T> callable;

	private final boolean delayedCancel;

	/** Thread executing the task. */
	private Thread myThread;

	/** Execution progress: 0 - not started, 1 - executing, 2 - interrupting, 3 - completed. */
	private volatile int progress = 0;

	@SuppressWarnings({ "rawtypes" })
	private static final AtomicIntegerFieldUpdater<CompletableFutureTask> PROGRESS_UPDATER = AtomicIntegerFieldUpdater.newUpdater(
		CompletableFutureTask.class, "progress"
	);

	/**
	 * Creates task from {@link Callable}.
	 *
	 * @param callable
	 * 	callable providing result
	 */
	public CompletableFutureTask(Callable<T> callable)
	{
		this(callable, false);
	}

	/**
	 * Creates task from {@link Callable}.
	 *
	 * @param callable
	 * 	callable providing result
	 * @param delayedCancel
	 * 	indicator whether future completion when cancelling should be delayed until task completes
	 */
	public CompletableFutureTask(Callable<T> callable, boolean delayedCancel)
	{
		this.callable = callable;
		this.delayedCancel = delayedCancel;
	}

	@Override
	public void run()
	{
		myThread = Thread.currentThread();
		if (!PROGRESS_UPDATER.compareAndSet(this, 0, 1)) {
			return;
		}
		try {
			T result = callable.call();
			if (canUpdate())
				complete(result);
		}
		catch (Throwable ex) {
			if (canUpdate()) {
				completeExceptionally(ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean cancel(boolean interrupt)
	{
		if (interrupt) {
			for (;;) {
				int old = progress;
				if (old == 0 || old == 1) {
					if (PROGRESS_UPDATER.compareAndSet(this, old, 2)) {
						if (old == 1) {
							synchronized (this) {
								if (progress == 2) {
									myThread.interrupt();
									progress = 4;
									notify();
								}
							}
							if (!delayedCancel) {
								return super.cancel(false);
							}
						}
						else {
							return super.cancel(false);
						}
					}
				}
				else if (!delayedCancel) {
					return super.cancel(false);
				}
				else {
					return false;
				}
			}
		}
		else {
			return super.cancel(false);
		}
	}

	private boolean canUpdate()
	{
		int old = PROGRESS_UPDATER.getAndSet(this, 3);
		if (old == 2) {
			try {
				synchronized (this) {
					for (;;) {
						if (progress == 2) {
							try {
								wait();
								break;
							}
							catch (InterruptedException e) {
								continue;
							}
						}
						else {
							break;
						}
					}
				}
				return false;
			}
			finally {
				super.cancel(false);
			}
		}
		else if (old == 4) {
			super.cancel(false);
			return false;
		}
		return true;
	}
}
