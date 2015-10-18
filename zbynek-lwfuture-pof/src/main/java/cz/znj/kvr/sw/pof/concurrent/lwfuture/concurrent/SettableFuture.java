package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;


/**
 * Asynchronous Future that can be finished externally.
 *
 * @param <V>
 *      future result type
 */
public class SettableFuture<V> extends AbstractFuture<V>
{
	public boolean                  set(V result)
	{
		return super.set(result);
	}

	public boolean                  setException(Throwable ex)
	{
		return super.setException(ex);
	}
}
