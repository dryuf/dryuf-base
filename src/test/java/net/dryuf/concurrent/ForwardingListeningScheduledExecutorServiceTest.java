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
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link ForwardingListeningScheduledExecutorService ForwardingListeningScheduledExecutorService} class.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class ForwardingListeningScheduledExecutorServiceTest
{
	@Test(timeOut = 1000L)
	public void                     testExecute() throws InterruptedException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			synchronized (result) {
				executor.execute(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (result) {
							result.incrementAndGet();
							result.notifyAll();
						}
					}
				});
				result.wait();
			}
			AssertJUnit.assertEquals(1, result.get());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 1000L)
	public void                     testScheduleRunnableNone() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableFuture<Object> future;
			synchronized (result) {
				future = (ListenableFuture<Object>)executor.schedule(new Runnable() {
					@Override
					public void run() {
						synchronized (result) {
							result.incrementAndGet();
							result.notifyAll();
						}
					}
				}, 1L, TimeUnit.MILLISECONDS);
				result.wait();
			}
			future.addListener(listener);
			future.get();
			AssertJUnit.assertEquals(1, result.get());
			AssertJUnit.assertNull(listener.waitValue());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@Test(timeOut = 1000L)
	public void                     testScheduleRunnableResult() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Integer> listener = new TestListener<Integer>();
			ListenableFuture<Integer> future;
			synchronized (result) {
				future = executor.schedule(new Runnable() {
					@Override
					public void run() {
						synchronized (result) {
							result.incrementAndGet();
							result.notifyAll();
						}
					}
				}, 1, 1L, TimeUnit.MILLISECONDS);
				result.wait();
			}
			future.addListener(listener);
			AssertJUnit.assertEquals(1, (int) future.get());
			AssertJUnit.assertEquals(1, result.get());
			AssertJUnit.assertEquals(1, listener.waitValue());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@Test(timeOut = 1000L)
	public void                     testScheduleCallable() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Integer> listener = new TestListener<Integer>();
			ListenableFuture<Integer> future;
			synchronized (result) {
				future = executor.schedule(new Callable<Integer>() {
					@Override
					public Integer call() {
						synchronized (result) {
							result.incrementAndGet();
							result.notifyAll();
						}
						return 1;
					}
				}, 1L, TimeUnit.MILLISECONDS);
				result.wait();
			}
			future.addListener(listener);
			AssertJUnit.assertEquals(1, (int)future.get());
			AssertJUnit.assertEquals(1, result.get());
			AssertJUnit.assertEquals(1, listener.waitValue());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = CancellationException.class, timeOut = 1000L)
	public void                     testScheduleAtFixedRate() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			final Semaphore sem = new Semaphore(1);
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleAtFixedRate(new Runnable()
				{
					@Override
					public void run()
					{
						sem.acquireUninterruptibly();
						synchronized (result) {
							result.incrementAndGet();
							result.notify();
						}
					}
				}, 1L, 1L, TimeUnit.MILLISECONDS);
				AssertJUnit.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				AssertJUnit.assertEquals(1, result.get());
				sem.release();
				result.wait();
				AssertJUnit.assertEquals(2, result.get());
				sem.release();
				future.setDelayedCancel();
				future.cancel(true);
			}
			future.addListener(listener);
			try {
				future.get();
				AssertJUnit.fail("Expected CancellationException");
			}
			finally {
				AssertJUnit.assertTrue(listener.waitValue() instanceof CancellationException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ExecutionException.class, timeOut = 1000000L)
	public void                     testScheduleAtFixedRateExcepted() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			final Semaphore sem = new Semaphore(1);
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						sem.acquireUninterruptibly();
						synchronized (result) {
							result.notify();
							if (result.incrementAndGet() == 2)
								throw new TestingRuntimeException();
						}
					}
				}, 1L, 1L, TimeUnit.MILLISECONDS);
				ListenableScheduledFuture<Object> longTermFuture = (ListenableScheduledFuture<Object>)executor.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						throw new TestingRuntimeException();
					}
				}, 10000L, 10000L, TimeUnit.MILLISECONDS);
				future.compareTo(longTermFuture); // dont test result as it is implementation specific
				AssertJUnit.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				AssertJUnit.assertEquals(1, result.get());
				sem.release();
				result.wait();
				AssertJUnit.assertEquals(2, result.get());
				sem.release();
				AssertJUnit.assertFalse(longTermFuture.isDone());
			}
			future.addListener(listener);
			try {
				future.get();
				AssertJUnit.fail("Expected ExecutionException");
			}
			finally {
				AssertJUnit.assertTrue(listener.waitValue() instanceof TestingRuntimeException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = CancellationException.class, timeOut = 1000L)
	public void                     testScheduleWithFixedDelay() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			final Semaphore sem = new Semaphore(1);
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleWithFixedDelay(new Runnable()
				{
					@Override
					public void run()
					{
						sem.acquireUninterruptibly();
						synchronized (result) {
							result.incrementAndGet();
							result.notify();
						}
					}
				}, 0L, 1L, TimeUnit.MILLISECONDS);
				AssertJUnit.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				AssertJUnit.assertEquals(1, result.get());
				sem.release();
				result.wait();
				AssertJUnit.assertEquals(2, result.get());
				sem.release();
				future.setDelayedCancel();
				future.cancel(true);
			}
			future.addListener(listener);
			try {
				future.get();
				AssertJUnit.fail("Expected CancellationException");
			}
			finally {
				AssertJUnit.assertTrue(listener.waitValue() instanceof CancellationException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ExecutionException.class, timeOut = 1000L)
	public void                     testScheduleWithFixedDelayExcepted() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			final Semaphore sem = new Semaphore(1);
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleWithFixedDelay(new Runnable()
				{
					@Override
					public void run()
					{
						sem.acquireUninterruptibly();
						synchronized (result) {
							result.notify();
							if (result.incrementAndGet() == 2)
								throw new TestingRuntimeException();
						}
					}
				}, 0L, 1L, TimeUnit.MILLISECONDS);
				AssertJUnit.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				AssertJUnit.assertEquals(1, result.get());
				sem.release();
				result.wait();
				AssertJUnit.assertEquals(2, result.get());
				sem.release();
			}
			future.addListener(listener);
			try {
				future.get();
				AssertJUnit.fail("Expected ExecutionException");
			}
			finally {
				AssertJUnit.assertTrue(listener.waitValue() instanceof TestingRuntimeException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 1000L)
	public void                     testShutdownCancelling() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			ListenableScheduledFuture<Object> f0;
			ListenableScheduledFuture<Object> f1;
			TestListener<Object> t0 = new TestListener<Object>();
			TestListener<Object> t1 = new TestListener<Object>();
			synchronized (result) {
				f0 = executor.schedule(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						synchronized (result) {
							result.incrementAndGet();
							result.notify();
						}
						Thread.sleep(1000000);
						return null;
					}
				}, 0L, TimeUnit.MILLISECONDS);
				f1 = (ListenableScheduledFuture<Object>) executor.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						synchronized (result) {
							result.incrementAndGet();
							result.notify();
						}
						try {
							Thread.sleep(1000000);
						}
						catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}, 0L, 1000000L, TimeUnit.MILLISECONDS);
				f0.addListener(t0);
				f1.addListener(t1);
				result.wait();
				if (result.get() < 2)
					result.wait();
			}
			executor.shutdownCancelling();
			try {
				f0.get();
				AssertJUnit.fail("Expected ExecutionException");
			}
			catch (CancellationException ex) {
				AssertJUnit.assertTrue(t0.waitValue() instanceof CancellationException);
			}
			try {
				f1.get();
				AssertJUnit.fail("Expected ExecutionException");
			}
			catch (CancellationException ex) {
				AssertJUnit.assertTrue(t1.waitValue() instanceof CancellationException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	private static ListeningScheduledExecutorService getExecutor()
	{
		return ListeningExecutors.listeningDecorator(Executors.newScheduledThreadPool(2));
	}
}
