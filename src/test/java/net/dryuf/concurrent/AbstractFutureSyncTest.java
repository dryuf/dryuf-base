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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


public class AbstractFutureSyncTest
{
	@Test
	public void                     testSuccess() throws ExecutionException, InterruptedException
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		future.set(0);
		AssertJUnit.assertEquals(0, listener.getValue());
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertEquals(0, second.getValue());
		AssertJUnit.assertEquals(0, future.get());
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testException()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		future.setException(new NumberFormatException());
		AssertJUnit.assertTrue(listener.getValue() instanceof NumberFormatException);
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertTrue(second.getValue() instanceof NumberFormatException);
		try {
			future.get();
			AssertJUnit.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			AssertJUnit.assertTrue(e.getCause() instanceof NumberFormatException);
		}
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testCancel()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.cancel(true));
		AssertJUnit.assertTrue(listener.getValue() instanceof CancellationException);
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertTrue(second.getValue() instanceof CancellationException);
		try {
			future.get();
			AssertJUnit.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (CancellationException ex) {
		}
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testCancelAndSet()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.cancel(true));
		AssertJUnit.assertFalse(future.set(0));
		AssertJUnit.assertTrue(listener.getValue() instanceof CancellationException);
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertTrue(second.getValue() instanceof CancellationException);
		try {
			future.get();
			AssertJUnit.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (CancellationException ex) {
		}
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testCancelAndSetException()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.cancel(true));
		AssertJUnit.assertFalse(future.setException(new NumberFormatException()));
		AssertJUnit.assertTrue(listener.getValue() instanceof CancellationException);
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertTrue(second.getValue() instanceof CancellationException);
		try {
			future.get();
			AssertJUnit.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (CancellationException ex) {
		}
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testSetAndCancel() throws ExecutionException, InterruptedException
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.set(0));
		AssertJUnit.assertFalse(future.cancel(true));
		AssertJUnit.assertEquals(0, listener.getValue());
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertEquals(0, second.getValue());
		AssertJUnit.assertEquals(0, future.get());
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testExceptionAndCancel()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.setException(new NumberFormatException()));
		AssertJUnit.assertFalse(future.cancel(true));
		AssertJUnit.assertTrue(listener.getValue() instanceof NumberFormatException);
		TestListener<Object> second = new TestListener<Object>();
		future.addListener(second);
		AssertJUnit.assertTrue(second.getValue() instanceof NumberFormatException);
		try {
			future.get();
			AssertJUnit.fail("Previous statement should have thrown an exception.");
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			AssertJUnit.assertTrue(e.getCause() instanceof NumberFormatException);
		}
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
	}

	@Test
	public void                     testSetCancelled()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.setCancelled());
		AssertJUnit.assertTrue(listener.getValue() instanceof CancellationException);
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertTrue(future.isCancelled());
	}

	@Test
	public void                     testSetAndSetCancelled()
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.set(null));
		AssertJUnit.assertFalse(future.setCancelled());
		AssertJUnit.assertTrue(listener.getValue() == null);
	}

	@Test(expectedExceptions = CancellationException.class)
	public void                     testRestart() throws ExecutionException, InterruptedException
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.setRestart());
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertTrue(future.setRunning());
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertTrue(future.setRestart());
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertTrue(future.cancel(true));
		AssertJUnit.assertTrue(listener.waitValue() instanceof CancellationException);
		AssertJUnit.assertFalse(future.setRestart());
		future.get();
	}

	@Test(expectedExceptions = CancellationException.class)
	public void                     testRestartDelayedCancel() throws ExecutionException, InterruptedException
	{
		TestListener<Object> listener = new TestListener<Object>();
		SettableFuture<Object> future = new SettableFuture<Object>();
		future.addListener(listener);
		future.setDelayedCancel();
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.cancel(true));
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.setRestart());
		AssertJUnit.assertTrue(listener.waitValue() instanceof CancellationException);
		future.get();
	}

	@Test
	public void                     testDoubleSet() throws ExecutionException, InterruptedException
	{
		TestListener<Integer> listener = new TestListener<Integer>();
		SettableFuture<Integer> future = new SettableFuture<Integer>();
		future.addListener(listener);
		AssertJUnit.assertNull(listener.getValue());
		AssertJUnit.assertFalse(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
		AssertJUnit.assertTrue(future.set(0));
		AssertJUnit.assertFalse(future.set(1));
		AssertJUnit.assertEquals(0, (int)future.get());
		AssertJUnit.assertTrue(future.isDone());
		AssertJUnit.assertFalse(future.isCancelled());
	}
}
