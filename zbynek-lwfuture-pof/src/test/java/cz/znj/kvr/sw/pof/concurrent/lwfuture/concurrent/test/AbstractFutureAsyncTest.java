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

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class AbstractFutureAsyncTest
{
	@Test(timeout = 1000L)
	public void                     testWaitSuccess() throws ExecutionException, InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		TestListener listener = new TestListener();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(1);
				return 1;
			}
		});
		future.addListener(listener);
		executor.execute(future);
		Assert.assertEquals(1, (int)future.get());
		Assert.assertEquals(1, (int)(Integer)listener.waitValue());
	}

	@Test(expected = ExecutionException.class, timeout = 1000L)
	public void                     testWaitFailure() throws ExecutionException, InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		TestListener listener = new TestListener();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(1);
				throw new TestingRuntimeException();
			}
		});
		future.addListener(listener);
		executor.execute(future);
		try {
			future.get();
			Assert.fail("get() succeeded");
		}
		finally {
			Assert.assertTrue(listener.waitValue() instanceof TestingRuntimeException);
		}
	}

	@Test(expected = CancellationException.class, timeout = 1000L)
	public void                     testWaitCancel() throws ExecutionException, InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		TestListener listener = new TestListener();
		final ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(1000000);
				return 0;
			}
		});
		future.addListener(listener);
		executor.execute(future);
		new Thread(new Runnable() {
			@Override
			public void run() {
				future.cancel(true);
			}
		}).start();
		try {
			future.get();
			Assert.fail("get() succeeded");
		}
		finally {
			Assert.assertTrue(listener.waitValue() instanceof CancellationException);
		}
	}
}
