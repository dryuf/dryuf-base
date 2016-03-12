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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


/**
 * Tests for utility methods in Futures class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr000@gmail.com http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class FuturesTest
{
	@Test
	public void                     testAllOfIterable()
	{
		SettableFuture<?> f0 = new SettableFuture<Void>();
		SettableFuture<?> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(Arrays.<ListenableFuture<?>>asList(f0, f1));
		AssertJUnit.assertFalse(all.isDone());
		f0.set(null);
		AssertJUnit.assertFalse(all.isDone());
		f1.set(null);
		AssertJUnit.assertTrue(all.isDone());
	}

	@Test
	public void                     testAllOfArray()
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(f0, f1);
		AssertJUnit.assertFalse(all.isDone());
		f0.set(null);
		AssertJUnit.assertFalse(all.isDone());
		f1.set(null);
		AssertJUnit.assertTrue(all.isDone());
	}

	@Test
	public void                     testAllOfFailed()
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(f0, f1);
		AssertJUnit.assertFalse(all.isDone());
		f0.setException(new TestingRuntimeException());
		AssertJUnit.assertTrue(all.isDone());
	}


	@Test
	public void                     testAllOfCancelled()
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		ListenableFuture<Void> all = Futures.allOf(f0, f1);
		AssertJUnit.assertFalse(all.isDone());
		f0.cancel(true);
		AssertJUnit.assertTrue(all.isDone());
	}

	@Test
	public void                     testAnyOfIterable()
	{
		SettableFuture<Integer> f0 = new SettableFuture<Integer>();
		SettableFuture<Integer> f1 = new SettableFuture<Integer>();
		@SuppressWarnings("unchecked")
		ListenableFuture<Integer> any = Futures.anyOf(Arrays.<ListenableFuture<Integer>>asList(f0, f1));
		AssertJUnit.assertFalse(any.isDone());
		f0.set(1);
		AssertJUnit.assertTrue(any.isDone());
		f1.set(null);
		AssertJUnit.assertTrue(f1.isCancelled());
	}

	@Test
	public void                     testAnyOfArray()
	{
		SettableFuture<Integer> f0 = new SettableFuture<Integer>();
		SettableFuture<Integer> f1 = new SettableFuture<Integer>();
		@SuppressWarnings("unchecked")
		ListenableFuture<Integer> any = Futures.anyOf(f0, f1);
		AssertJUnit.assertFalse(any.isDone());
		f1.set(1);
		AssertJUnit.assertTrue(any.isDone());
		f0.set(null);
		AssertJUnit.assertTrue(f0.isCancelled());
	}

	@Test(expectedExceptions = ExecutionException.class, timeOut = 1000L)
	public void                     testAnyOfFailure() throws ExecutionException, InterruptedException
	{
		SettableFuture<Integer> f0 = new SettableFuture<Integer>();
		SettableFuture<Integer> f1 = new SettableFuture<Integer>();
		@SuppressWarnings("unchecked")
		ListenableFuture<Integer> any = Futures.anyOf(f0, f1);
		AssertJUnit.assertFalse(any.isDone());
		f1.setException(new TestingRuntimeException());
		AssertJUnit.assertTrue(any.isDone());
		f0.set(null);
		AssertJUnit.assertTrue(f0.isCancelled());
		any.get();
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 1000L)
	public void			testCancelAll() throws ExecutionException, InterruptedException
	{
		SettableFuture<Void> f0 = new SettableFuture<Void>();
		SettableFuture<Void> f1 = new SettableFuture<Void>();
		Futures.cancelAll(Arrays.asList(f0, f1));
		try {
			f0.get();
			AssertJUnit.fail("f0.get() did not throw CancellationException");
		}
		catch (CancellationException ex) {
		}
		try {
			f1.get();
			AssertJUnit.fail("f1.get() did not throw CancellationException");
		}
		catch (CancellationException ex) {
		}
	}

	@Test(timeOut = 1000L)
	public void			testSuccessFuture() throws ExecutionException, InterruptedException
	{
		AssertJUnit.assertEquals(1, (int) Futures.successFuture(1).get());
	}

	@Test(expectedExceptions = ExecutionException.class, timeOut = 1000L)
	public void			testFailedFuture() throws ExecutionException, InterruptedException
	{
		Futures.failedFuture(new TestingRuntimeException()).get();
	}

	@Test(expectedExceptions = CancellationException.class, timeOut = 1000L)
	public void			testCancelledFuture() throws ExecutionException, InterruptedException
	{
		Futures.cancelledFuture().get();
	}
}
