package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class DistinguishFutureListener<V> implements FutureListener<V>
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

	@Override
	public void                     run(Future<V> future)
	{
		try {
			onSuccess(future.get());
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			onFailure(e.getCause());
		}
		catch (CancellationException e) {
			onCancelled();
		}
	}
}
