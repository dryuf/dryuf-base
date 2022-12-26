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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class AbstractFutureUncancellableTest
{
	@Test(timeOut = 1000)
	public void                     testUncancellable() throws  InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
				try {
					queue.add(0);
					Thread.sleep(1);
					queue.add(1);
					Thread.sleep(1);
					queue.add(2);
				}
				catch (InterruptedException e) {
					queue.add(-1);
				}
			}
		}, null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				try {
					queue.put(3);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		future.setUncancellable();
		executor.execute(future);
		AssertJUnit.assertEquals(0, (int) queue.take());
		AssertJUnit.assertFalse(future.cancel(true));
		AssertJUnit.assertEquals(1, (int)queue.take());
		AssertJUnit.assertEquals(2, (int) queue.take());
		AssertJUnit.assertEquals(3, (int)queue.take());
		AssertJUnit.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
	}

	@Test(timeOut = 1000)
	public void                     testUncancellableFinished() throws  InterruptedException
	{
		Executor executor = ListeningExecutors.directExecutor();
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
					queue.add(0);
			}
		}, null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				queue.add(1);
			}
		});
		executor.execute(future);
		future.setUncancellable();
		AssertJUnit.assertEquals(0, (int) queue.take());
		AssertJUnit.assertFalse(future.cancel(true));
		AssertJUnit.assertEquals(1, (int)queue.take());
		AssertJUnit.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
	}

	@Test(expectedExceptions = IllegalStateException.class, timeOut = 1000)
	public void                     testUncancellableCancelled() throws  InterruptedException
	{
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
			}
		}, null);
		AssertJUnit.assertTrue(future.cancel(true));
		future.setUncancellable();
	}
}
