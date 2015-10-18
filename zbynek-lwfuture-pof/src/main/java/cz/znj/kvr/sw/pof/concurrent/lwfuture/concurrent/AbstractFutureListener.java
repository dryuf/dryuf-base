package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;


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
