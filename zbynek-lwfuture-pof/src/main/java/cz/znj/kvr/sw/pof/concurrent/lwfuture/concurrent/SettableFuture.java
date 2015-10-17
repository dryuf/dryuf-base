package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


public class SettableFuture<V> extends AbstractFuture<V>
{
	public void                     set(V result)
	{
		super.set(result);
	}

	public void                     setException(Throwable ex)
	{
		super.setException(ex);
	}
}
