/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
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

package net.dryuf.concurrent;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class AbstractFutureAsyncTest
{
	@Test(timeOut = 1000L)
	public void                     testWaitSuccess() throws ExecutionException, InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		TestListener<Integer> listener = new TestListener<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(1);
				return 1;
			}
		});
		future.addListener(listener);
		executor.execute(future);
		AssertJUnit.assertEquals(1, (int)future.get());
		AssertJUnit.assertEquals(1, (int)(Integer)listener.waitValue());
	}

	@Test(expectedExceptions = ExecutionException.class, timeOut = 1000L)
	public void                     testWaitFailure() throws ExecutionException, InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		TestListener<Integer> listener = new TestListener<Integer>();
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
			AssertJUnit.fail("get() succeeded");
		}
		finally {
			AssertJUnit.assertTrue(listener.waitValue() instanceof TestingRuntimeException);
		}
	}

	@Test(expectedExceptions = CancellationException.class, timeOut = 1000L)
	public void                     testWaitCancel() throws ExecutionException, InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		TestListener<Integer> listener = new TestListener<Integer>();
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
			AssertJUnit.fail("get() succeeded");
		}
		finally {
			AssertJUnit.assertTrue(listener.waitValue() instanceof CancellationException);
		}
	}
}
