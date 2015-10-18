package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.Future;


public interface FutureListener<V>
{
	public void			onSuccess(V result);

	public void			onFailure(Throwable ex);

	public void                     onCancelled();
}
