package net.dryuf.base.concurrent.executor;

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
	private static final int PROGRESS_INITIAL = 0;
	private static final int PROGRESS_EXECUTING = 1;
	private static final int PROGRESS_INTERRUPTING = 2;
	private static final int PROGRESS_COMPLETED = 3;
	private static final int PROGRESS_CANCELLED = 4;

	private final Callable<T> callable;

	private final boolean delayedCancel;

	/** Execution progress: see PROGRESS_* constants: */
	private volatile int progress = PROGRESS_INITIAL;

	/** Thread executing the task. */
	private Thread myThread;

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
		if (!PROGRESS_UPDATER.compareAndSet(this, PROGRESS_INITIAL, PROGRESS_EXECUTING)) {
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
				if (old == PROGRESS_INITIAL || old == PROGRESS_EXECUTING) {
					if (PROGRESS_UPDATER.compareAndSet(this, old, PROGRESS_INTERRUPTING)) {
						if (old == PROGRESS_EXECUTING) {
							synchronized (this) {
								if (progress == PROGRESS_INTERRUPTING) {
									myThread.interrupt();
									progress = PROGRESS_CANCELLED;
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
		int old = PROGRESS_UPDATER.getAndSet(this, PROGRESS_COMPLETED);
		if (old == PROGRESS_INTERRUPTING) {
			try {
				synchronized (this) {
					for (;;) {
						if (progress == PROGRESS_INTERRUPTING) {
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
		else if (old == PROGRESS_CANCELLED) {
			super.cancel(false);
			return false;
		}
		return true;
	}
}
