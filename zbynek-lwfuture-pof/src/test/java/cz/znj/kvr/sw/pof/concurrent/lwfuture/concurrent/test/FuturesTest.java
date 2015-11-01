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

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.Futures;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFuture;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Tests for utility methods in Futures class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class FuturesTest
{
	@Test
	public void                     testAllOfIterable()
	{
		SettableFuture<?> f0 = new SettableFuture<Void>();
		SettableFuture<?> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(Arrays.<ListenableFuture<?>>asList(f0, f1));
		Assert.assertFalse(all.isDone());
		f0.set(null);
		Assert.assertFalse(all.isDone());
		f1.set(null);
		Assert.assertTrue(all.isDone());
	}

	@Test
	public void                     testAllOfArray()
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(f0, f1);
		Assert.assertFalse(all.isDone());
		f0.set(null);
		Assert.assertFalse(all.isDone());
		f1.set(null);
		Assert.assertTrue(all.isDone());
	}

	@Test
	public void                     testAllOfFailed()
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(f0, f1);
		Assert.assertFalse(all.isDone());
		f0.setException(new TestingRuntimeException());
		Assert.assertTrue(all.isDone());
	}


	@Test
	public void                     testAllOfCancelled()
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(f0, f1);
		Assert.assertFalse(all.isDone());
		f0.cancel(true);
		Assert.assertTrue(all.isDone());
	}

	@Test
	public void                     testAnyOfIterable()
	{
		SettableFuture<Integer> f0 = new SettableFuture<Integer>();
		SettableFuture<Integer> f1 = new SettableFuture<Integer>();
		ListenableFuture<Integer> any = Futures.anyOf(Arrays.<ListenableFuture<Integer>>asList(f0, f1));
		Assert.assertFalse(any.isDone());
		f0.set(1);
		Assert.assertTrue(any.isDone());
		f1.set(null);
		Assert.assertTrue(f1.isCancelled());
	}

	@Test
	public void                     testAnyOfArray()
	{
		SettableFuture<Integer> f0 = new SettableFuture<Integer>();
		SettableFuture<Integer> f1 = new SettableFuture<Integer>();
		ListenableFuture<Integer> any = Futures.anyOf(f0, f1);
		Assert.assertFalse(any.isDone());
		f1.set(1);
		Assert.assertTrue(any.isDone());
		f0.set(null);
		Assert.assertTrue(f0.isCancelled());
	}

	@Test(expected = ExecutionException.class, timeout = 1000L)
	public void                     testAnyOfFailure() throws ExecutionException, InterruptedException
	{
		SettableFuture<Integer> f0 = new SettableFuture<Integer>();
		SettableFuture<Integer> f1 = new SettableFuture<Integer>();
		ListenableFuture<Integer> any = Futures.anyOf(f0, f1);
		Assert.assertFalse(any.isDone());
		f1.setException(new TestingRuntimeException());
		Assert.assertTrue(any.isDone());
		f0.set(null);
		Assert.assertTrue(f0.isCancelled());
		any.get();
	}

	@SuppressWarnings("unchecked")
	@Test(timeout = 1000L)
	public void			testCancelAll() throws ExecutionException, InterruptedException
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		Futures.cancelAll(Arrays.asList(f0, f1));
		try {
			f0.get();
			Assert.fail("f0.get() did not throw CancellationException");
		}
		catch (CancellationException ex) {
		}
		try {
			f1.get();
			Assert.fail("f1.get() did not throw CancellationException");
		}
		catch (CancellationException ex) {
		}
	}

	@Test(timeout = 1000L)
	public void			testSuccessFuture() throws ExecutionException, InterruptedException
	{
		Assert.assertEquals(1, (int) Futures.successFuture(1).get());
	}

	@Test(expected = ExecutionException.class, timeout = 1000L)
	public void			testFailedFuture() throws ExecutionException, InterruptedException
	{
		Futures.failedFuture(new TestingRuntimeException()).get();
	}

	@Test(expected = CancellationException.class, timeout = 1000L)
	public void			testCancelledFuture() throws ExecutionException, InterruptedException
	{
		Futures.cancelledFuture().get();
	}
}
