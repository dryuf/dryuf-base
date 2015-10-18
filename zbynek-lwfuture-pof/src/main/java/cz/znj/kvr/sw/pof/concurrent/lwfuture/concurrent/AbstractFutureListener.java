package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;


/**
 * Default FutureListener implementation taking no action by default
 *
 * @param <V>
 *      future result type
 */
public class AbstractFutureListener<V> implements FutureListener<V>
{
	public void			onSuccess(V result)
	{
	}

	public void			onFailure(Throwable ex)
	{
	}

	public void                     onCancelled()
	{
	}
}
