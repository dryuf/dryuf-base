package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

/**
 * ListenableFuture implementation which can be used as handling wrapper for actually running the task.
 * @param <V>
 *         future return type
 */
public class ListenableFutureTask<V> extends AbstractFuture<V> implements RunnableFuture<V>
{
	/**
	 * Constructs new instance with Runnable reference and provided result.
	 *
	 * @param runnable
	 *      function to run
	 * @param result
	 *      provided result
	 */
	public                          ListenableFutureTask(final Runnable runnable, final V result)
	{
		this(new Callable<V>() {
			@Override
			public V call() throws Exception {
				return result;
			}
		});
	}

	/**
	 * Constructs new instance with Callback reference.
	 *
	 * @param callable
	 *      function to compute the result
	 */
	public                          ListenableFutureTask(final Callable<V> callable)
	{
		super(false);
		this.callable = callable;
	}

	protected void                  interruptTask()
	{
		myThread.interrupt();
	}

	@Override
	public void                     run()
	{
		try {
			myThread = Thread.currentThread();
			if (setRunning());
				set(callable.call());
		}
		catch (Exception ex) {
			setException(ex);
		}
	}

	/**
	 * The thread that executes the task.
	 *
	 * Volatile is not needed as this is surrounded with other memory barrier reads/writes.
	 */
	private Thread                  myThread;

	/**
	 * Callable performing the task.
	 */
	private final Callable<V>       callable;
}
