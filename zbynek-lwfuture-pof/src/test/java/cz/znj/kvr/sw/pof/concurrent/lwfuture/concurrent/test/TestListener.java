package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.test;

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.AbstractFutureListener;

import java.util.concurrent.CancellationException;

public class TestListener extends AbstractFutureListener<Object>
{
	public static final CancellationException CANCELLED = new CancellationException();

	public void                     onSuccess(Object result)
	{
		this.value = result;
	}

	public void                     onFailure(Throwable ex)
	{
		this.value = ex;
	}

	public void                     onCancelled()
	{
		this.value = CANCELLED;
	}

	public Object                   getValue()
	{
		return value;
	}

	protected Object                value;
}
