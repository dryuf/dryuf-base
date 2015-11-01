/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr@centrum.cz http://kvr.znj.cz/ http://github.com/kvr000/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.test;

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.DefaultFutureListener;

import java.util.concurrent.CancellationException;


/**
 * Testing listener storing the future result in instance variable.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class TestListener<V> extends DefaultFutureListener<V>
{
	public static final CancellationException CANCELLED = new CancellationException();

	@Override
	public synchronized void        onSuccess(V result)
	{
		this.value = result;
		this.done = true;
		this.notifyAll();
	}

	@Override
	public synchronized void        onFailure(Throwable ex)
	{
		this.value = ex;
		this.done = true;
		this.notifyAll();
	}

	@Override
	public synchronized void        onCancelled()
	{
		this.value = CANCELLED;
		this.done = true;
		this.notifyAll();
	}

	/**
	 * Gets the value of result, exception or cancellation.
	 *
	 * @return
	 *      future result, either result, exception or cancellation
	 */
	public Object                   getValue()
	{
		return value;
	}

	public synchronized  Object	waitValue()
	{
		if (!done) {
			try {
				wait();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return value;
	}

	protected boolean		done;

	/**
	 * Result value, exception or cancellation.
	 */
	protected Object                value;
}
