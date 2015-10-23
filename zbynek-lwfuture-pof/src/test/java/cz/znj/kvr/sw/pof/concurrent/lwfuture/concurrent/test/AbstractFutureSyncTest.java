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

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


public class AbstractFutureSyncTest
{
	@Test
	public void                     testSuccess() throws ExecutionException, InterruptedException
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		future.set(0);
		Assert.assertEquals(0, listener.getValue());
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertEquals(0, second.getValue());
		Assert.assertEquals(0, future.get());
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testException()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		future.setException(new NumberFormatException());
		Assert.assertTrue(listener.getValue() instanceof NumberFormatException);
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertTrue(second.getValue() instanceof NumberFormatException);
		try {
			future.get();
			Assert.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			Assert.assertTrue(e.getCause() instanceof NumberFormatException);
		}
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testCancel()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		Assert.assertTrue(future.cancel(true));
		Assert.assertTrue(listener.getValue() instanceof CancellationException);
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertTrue(second.getValue() instanceof CancellationException);
		try {
			future.get();
			Assert.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (CancellationException ex) {
		}
		Assert.assertTrue(future.isDone());
		Assert.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testCancelAndSet()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		Assert.assertTrue(future.cancel(true));
		Assert.assertFalse(future.set(0));
		Assert.assertTrue(listener.getValue() instanceof CancellationException);
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertTrue(second.getValue() instanceof CancellationException);
		try {
			future.get();
			Assert.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (CancellationException ex) {
		}
		Assert.assertTrue(future.isDone());
		Assert.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testCancelAndSetException()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		Assert.assertTrue(future.cancel(true));
		Assert.assertFalse(future.setException(new NumberFormatException()));
		Assert.assertTrue(listener.getValue() instanceof CancellationException);
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertTrue(second.getValue() instanceof CancellationException);
		try {
			future.get();
			Assert.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (CancellationException ex) {
		}
		Assert.assertTrue(future.isDone());
		Assert.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testSetAndCancel() throws ExecutionException, InterruptedException
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		Assert.assertTrue(future.set(0));
		Assert.assertFalse(future.cancel(true));
		Assert.assertEquals(0, listener.getValue());
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertEquals(0, second.getValue());
		Assert.assertEquals(0, future.get());
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testExceptionAndCancel()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(future.isCancelled());
		Assert.assertTrue(future.setException(new NumberFormatException()));
		Assert.assertFalse(future.cancel(true));
		Assert.assertTrue(listener.getValue() instanceof NumberFormatException);
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertTrue(second.getValue() instanceof NumberFormatException);
		try {
			future.get();
			Assert.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			Assert.assertTrue(e.getCause() instanceof NumberFormatException);
		}
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future.isCancelled());
	}
}
