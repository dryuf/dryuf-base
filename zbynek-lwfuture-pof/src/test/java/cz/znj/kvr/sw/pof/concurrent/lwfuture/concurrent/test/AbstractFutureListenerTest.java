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
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.CancelListener;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.FailureListener;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.FutureNotifier;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SettableFuture;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SuccessListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;


public class AbstractFutureListenerTest
{
	@Test(timeout = 1000L)
	public void                     testListenersSet()
	{
		final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(null, null, null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				queue.add(1);
			}
		});
		future.addListener(new FutureNotifier<Future<Void>>() {
			@Override
			public void notify(Future<Void> future) {
				queue.add(2);
			}
		});
		future.addListener(new DefaultFutureListener<Void>() {
			@Override
			public void onSuccess(Void value) {
				queue.add(3);
			}
		});
		future.addListener(new SuccessListener<Void>() {
			@Override
			public void onSuccess(Void value) {
				queue.add(4);
			}
		}, null, null);
		queue.add(0);
		future.set(null);
		Assert.assertEquals(0, (int)queue.remove());
		Assert.assertEquals(1, (int)queue.remove());
		Assert.assertEquals(2, (int)queue.remove());
		Assert.assertEquals(3, (int)queue.remove());
		Assert.assertEquals(4, (int)queue.remove());
	}

	@Test(timeout = 1000L)
	public void                     testListenersExcepted()
	{
		final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(null, null, null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				queue.add(1);
			}
		});
		future.addListener(new FutureNotifier<Future<Void>>() {
			@Override
			public void notify(Future<Void> future) {
				queue.add(2);
			}
		});
		future.addListener(new DefaultFutureListener<Void>() {
			@Override
			public void onFailure(Throwable ex) {
				queue.add(3);
			}
		});
		future.addListener(null, new FailureListener() {
			@Override
			public void onFailure(Throwable ex) {
				queue.add(4);
			}
		}, null);
		queue.add(0);
		future.setException(new NumberFormatException());
		Assert.assertEquals(0, (int) queue.remove());
		Assert.assertEquals(1, (int) queue.remove());
		Assert.assertEquals(2, (int) queue.remove());
		Assert.assertEquals(3, (int) queue.remove());
		Assert.assertEquals(4, (int)queue.remove());
	}

	@Test(timeout = 1000L)
	public void                     testListenersCancelled()
	{
		final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(null, null, null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				queue.add(1);
			}
		});
		future.addListener(new FutureNotifier<Future<Void>>() {
			@Override
			public void notify(Future<Void> future) {
				queue.add(2);
			}
		});
		future.addListener(new DefaultFutureListener<Void>() {
			@Override
			public void onCancelled() {
				queue.add(3);
			}
		});
		future.addListener(null, null, new CancelListener() {
			@Override
			public void onCancelled() {
				queue.add(4);
			}
		});
		queue.add(0);
		future.cancel(true);
		Assert.assertEquals(0, (int) queue.remove());
		Assert.assertEquals(1, (int) queue.remove());
		Assert.assertEquals(2, (int) queue.remove());
		Assert.assertEquals(3, (int) queue.remove());
		Assert.assertEquals(4, (int)queue.remove());
	}

	@Test
	public void                     testExceptingListenerSet()
	{
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new TestingRuntimeException();
			}
		});
		future.set(null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new TestingRuntimeException();
			}
		});
	}

	@Test
	public void                     testExceptingListenerExcepted()
	{
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new TestingRuntimeException();
			}
		});
		future.setException(new NumberFormatException());
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new TestingRuntimeException();
			}
		});
	}

	@Test
	public void                     testExceptingListenerCancelled()
	{
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new TestingRuntimeException();
			}
		});
		future.cancel(true);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new TestingRuntimeException();
			}
		});
	}
}
