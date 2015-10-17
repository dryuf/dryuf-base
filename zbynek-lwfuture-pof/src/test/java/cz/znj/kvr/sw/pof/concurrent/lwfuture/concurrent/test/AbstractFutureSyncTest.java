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
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		future.set(0);
		Assert.assertEquals(0, listener.getValue());
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertEquals(0, second.getValue());
		Assert.assertEquals(0, future.get());
	}

	@Test
	public void                     testException()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
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
	}

	@Test
	public void                     testCancel()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		future.cancel(true);
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
	}

	@Test
	public void                     testCancelAndSet()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		future.cancel(true);
		future.set(0);
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
	}

	@Test
	public void                     testCancelAndSetException()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		future.cancel(true);
		future.setException(new NumberFormatException());
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
	}

	@Test
	public void                     testSetAndCancel() throws ExecutionException, InterruptedException
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		future.set(0);
		future.cancel(true);
		Assert.assertEquals(0, listener.getValue());
		TestListener second = new TestListener();
		future.addListener(second);
		Assert.assertEquals(0, second.getValue());
		Assert.assertEquals(0, future.get());
	}

	@Test
	public void                     testExceptionAndCancel()
	{
		TestListener listener = new TestListener();
		SettableFuture<Object> future = new SettableFuture<>();
		future.addListener(listener);
		Assert.assertNull(listener.getValue());
		future.setException(new NumberFormatException());
		future.cancel(true);
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
	}

}
