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
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFutureTask;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class AbstractFutureCancelTest
{
	@Test(timeout = 1000)
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
		Assert.assertEquals(0, (int) queue.take());
		future.cancel(true);
		Assert.assertEquals(1, (int)queue.take());
		Assert.assertEquals(1, (int) queue.take());
		Assert.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
		Assert.assertEquals(2, (int)queue.take());
		Assert.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
	}

	@Test(timeout = 1000)
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
		Assert.assertEquals(0, (int)queue.take());
		future.cancel(true);
		Assert.assertEquals(1, (int)queue.take());
		Assert.assertEquals(2, (int)queue.take());
		Assert.assertEquals(3, (int)queue.take());
		Assert.assertEquals(4, (int)queue.take());
		Assert.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
	}


	@Test(timeout = 1000)
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
		Assert.assertEquals(0, (int)queue.take());
		future.setDelayedCancel();
		future.cancel(true);
		Assert.assertEquals(1, (int)queue.take());
		Assert.assertEquals(2, (int)queue.take());
		Assert.assertEquals(3, (int) queue.take());
	}

	@Test(timeout = 1000, expected = IllegalStateException.class)
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
		Assert.assertEquals(0, (int)queue.take());
		future.cancel(true);
		future.setDelayedCancel();
	}
}
