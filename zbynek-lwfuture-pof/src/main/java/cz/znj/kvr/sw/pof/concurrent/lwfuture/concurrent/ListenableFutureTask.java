package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;


public class ListenableFutureTask<V> extends AbstractFuture<V> implements RunnableFuture<V>
{
	public                          ListenableFutureTask(final Runnable runnable, final V result)
	{
		this(new Callable<V>() {
			@Override
			public V call() throws Exception {
				return result;
			}
		});
	}

	public                          ListenableFutureTask(final Callable<V> callable)
	{
		super(false);
		this.callable = callable;
	}

	protected void                  interruptTask()
	{
		
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

	private Thread                  myThread;

	private final Callable<V>       callable;
}
