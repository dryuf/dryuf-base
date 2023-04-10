package net.dryuf.base.concurrent.sync;

import net.dryuf.base.function.ThrowingRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * Synchronization object similar to CountDownLatch but automatically executing an action when reaching target.
 */
public class CountDownRunner
{
	private static final AtomicIntegerFieldUpdater<CountDownRunner> REMAINING_UPDATER =
			AtomicIntegerFieldUpdater.newUpdater(CountDownRunner.class, "remaining");

	private volatile int remaining;

	private final CompletableFuture<Void> future = new CompletableFuture<>();

	/**
	 * Constructs new CountDownRunner.
	 *
	 * @param counter
	 * 	capacity count
	 */
	public CountDownRunner(int counter)
	{
		this.remaining = counter;
	}

	/**
	 * Constructs new CountDownRunner.
	 *
	 * @param counter
	 * 	capacity count
	 * @param handler
	 * 	action to be executed when reaching target
	 * @param <X>
	 *      type of exception thrown by handler
	 */
	public <X extends Exception> CountDownRunner(int counter, ThrowingRunnable<X> handler)
	{
		this(counter);
		addRunner(handler);
	}

	/**
	 * Decrements count of latch.
	 */
	public void countDown()
	{
		if (REMAINING_UPDATER.decrementAndGet(this) == 0) {
			future.complete(null);
		}
	}

	/**
	 * Adds new target handler to be executed once this object count down is completed.
	 *
	 * @param handler
	 * 	handler to be executed
	 * @param <X>
	 *      type of exception thrown by handler
	 */
	public <X extends Exception> void addRunner(ThrowingRunnable<X> handler)
	{
		future.thenRun(ThrowingRunnable.sneaky(handler));
	}
}
