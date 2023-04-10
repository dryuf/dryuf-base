package net.dryuf.base.concurrent.sync;

import net.dryuf.base.function.ThrowingCallable;
import net.dryuf.base.function.ThrowingRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * Synchronization object ensuring there is only one execution running at the same time.
 * When new execution is attempted and there is already one running, the new one is skipped.
 *
 * This is typically useful when some activity is run regularly but the next should not start until previous attempt is
 * finished.
 */
public class RunSingle
{
	private static final AtomicIntegerFieldUpdater<RunSingle> RUNNING_UPDATER = AtomicIntegerFieldUpdater.newUpdater(RunSingle.class, "running");

	/** Running indicator. */
	private volatile int running;

	/**
	 * Runs a runnable if nothing else is running currently.  It rethrows original exception if supplier failed.
	 *
	 * @param runnable
	 * 	runnable to run
	 *
	 * @param <X>
	 *      type of exception thrown by runnable
	 *
	 * @throws X
	 * 	if runnable throws X
	 */
	public <X extends Exception> void run(ThrowingRunnable<X> runnable) throws X
	{
		if (RUNNING_UPDATER.compareAndSet(this, 0, 1)) {
			try {
				runnable.run();
			}
			finally {
				RUNNING_UPDATER.set(this, 0);
			}
		}
	}

	/**
	 * Runs a callable if nothing else is running currently.  It rethrows original exception if supplier failed.
	 *
	 * @param callable
	 * 	callable to run
	 *
	 * @return
	 * 	result of callable or null if nothing was run.
	 *
	 * @param <R>
	 *      return type
	 * @param <X>
	 *      exception type thrown by callable
	 *
	 * @throws X
	 * 	if callable throws an exception
	 */
	public <R, X extends Exception> R call(ThrowingCallable<R, X> callable) throws X
	{
		if (RUNNING_UPDATER.compareAndSet(this, 0, 1)) {
			try {
				return callable.call();
			}
			finally {
				RUNNING_UPDATER.set(this, 0);
			}
		}
		return null;
	}

	/**
	 * Runs future if there is nothing else running currently.  Returns cancelled future if there was something
	 * running.  It rethrows original exception if supplier failed.
	 *
	 * @param supplier
	 * 	future supplier
	 *
	 * @return
	 * 	CompletableFuture returned by supplier or cancelled future if there was something running.
	 *
	 * @param <R>
	 *      return type
	 * @param <X>
	 *      exception thrown by supplier
	 *
	 * @throws X
	 * 	if supplier threw an exception
	 */
	public <R, X extends Exception> CompletableFuture<R> compose(ThrowingCallable<CompletableFuture<R>, X> supplier) throws X
	{
		if (RUNNING_UPDATER.compareAndSet(this, 0, 1)) {
			try {
				CompletableFuture<R> future = supplier.call();
				future.whenComplete((v, ex) -> RUNNING_UPDATER.set(this, 0));
				return future;
			}
			catch (Throwable ex) {
				RUNNING_UPDATER.set(this, 0);
				throw ex;
			}
		}
		else {
			CompletableFuture<R> future = new CompletableFuture<>();
			future.cancel(true);
			return future;
		}
	}

	/**
	 * Runs future if there is nothing else running currently.  Returns cancelled future if there was something
	 * running.  It rethrows original exception if supplier failed.
	 *
	 * @param supplier
	 * 	future supplier
	 *
	 * @return
	 * 	CompletableFuture returned by supplier or cancelled future if there was something running.
	 *
	 * @param <R>
	 *      return type
	 * @param <X>
	 *      exception thrown by supplier
	 *
	 * @throws X
	 * 	if supplier threw an exception
	 */
	public <R, X extends Exception> CompletionStage<R> composeStage(ThrowingCallable<CompletionStage<R>, X> supplier) throws X
	{
		if (RUNNING_UPDATER.compareAndSet(this, 0, 1)) {
			try {
				CompletionStage<R> future = supplier.call();
				future.whenComplete((v, ex) -> RUNNING_UPDATER.set(this, 0));
				return future;
			}
			catch (Throwable ex) {
				RUNNING_UPDATER.set(this, 0);
				throw ex;
			}
		}
		else {
			CompletableFuture<R> future = new CompletableFuture<>();
			future.cancel(true);
			return future;
		}
	}
}
