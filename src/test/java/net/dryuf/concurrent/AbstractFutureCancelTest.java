/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr000@gmail.com http://kvr.znj.cz/ http://github.com/kvr000/
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


import net.dryuf.concurrent.DefaultFutureListener;
import net.dryuf.concurrent.ListenableFutureTask;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class AbstractFutureCancelTest
{
	@Test(timeOut = 1000)
	public void                     testCancel() throws  InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
				try {
					queue.put(0);
					Thread.sleep(86400000);
				}
				catch (InterruptedException e) {
					try {
						queue.put(1);
						Thread.sleep(20);
						queue.put(2);
					}
					catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		}, null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				try {
					queue.put(1);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});
		executor.execute(future);
		AssertJUnit.assertEquals(0, (int) queue.take());
		future.cancel(true);
		AssertJUnit.assertEquals(1, (int)queue.take());
		AssertJUnit.assertEquals(1, (int) queue.take());
		AssertJUnit.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
		AssertJUnit.assertEquals(2, (int)queue.take());
		AssertJUnit.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
	}

	@Test(timeOut = 1000000L)
	public void                     testDelayedCancel() throws  InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
				try {
					queue.put(0);
					Thread.sleep(86400000);
				}
				catch (InterruptedException e) {
					try {
						queue.put(1);
						Thread.sleep(20);
						queue.put(2);
					}
					catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
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
		future.addListener(new DefaultFutureListener<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				throw new RuntimeException("onSuccess was called.");
			}

			@Override
			public void onFailure(Throwable ex) {
				throw new RuntimeException("onFailure was called.");
			}

			@Override
			public void onCancelled() {
				queue.add(4);
			}
		});
		future.setDelayedCancel();
		executor.execute(future);
		AssertJUnit.assertEquals(0, (int)queue.take());
		future.cancel(true);
		AssertJUnit.assertEquals(1, (int)queue.take());
		AssertJUnit.assertEquals(2, (int)queue.take());
		AssertJUnit.assertEquals(3, (int)queue.take());
		AssertJUnit.assertEquals(4, (int)queue.take());
		AssertJUnit.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
	}


	@Test(timeOut = 1000)
	public void                     testDelayedCancelAfter() throws  InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
				try {
					queue.put(0);
					Thread.sleep(86400000);
				}
				catch (InterruptedException e) {
					try {
						queue.put(1);
						Thread.sleep(20);
						queue.put(2);
					}
					catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
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
		future.setDelayedCancel();
		executor.execute(future);
		AssertJUnit.assertEquals(0, (int)queue.take());
		future.setDelayedCancel();
		future.cancel(true);
		AssertJUnit.assertEquals(1, (int)queue.take());
		AssertJUnit.assertEquals(2, (int)queue.take());
		AssertJUnit.assertEquals(3, (int) queue.take());
	}

	@Test(timeOut = 1000, expectedExceptions = IllegalStateException.class)
	public void                     testDelayedCancelLate() throws  InterruptedException
	{
		Executor executor = Executors.newSingleThreadExecutor();
		final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		ListenableFutureTask<Integer> future = new ListenableFutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
				try {
					queue.put(0);
					Thread.sleep(86400000);
				}
				catch (InterruptedException e) {
					try {
						queue.put(1);
						Thread.sleep(20);
						queue.put(2);
					}
					catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
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
		future.setDelayedCancel();
		executor.execute(future);
		AssertJUnit.assertEquals(0, (int)queue.take());
		future.cancel(true);
		future.setDelayedCancel();
	}
}
