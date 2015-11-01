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

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFuture;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableScheduledFuture;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListeningExecutors;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListeningScheduledExecutorService;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.OneShotScheduledFutureTask;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.RepeatingScheduledFutureTask;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ForwardingListeningScheduledExecutorService ForwardingListeningScheduledExecutorService} class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class ForwardingListeningScheduledExecutorServiceTest
{
	@Test(timeout = 1000L)
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
			Assert.assertEquals(1, result.get());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(timeout = 1000L)
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
			Assert.assertEquals(1, result.get());
			Assert.assertNull(listener.waitValue());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@Test(timeout = 1000L)
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
			Assert.assertEquals(1, (int)future.get());
			Assert.assertEquals(1, result.get());
			Assert.assertEquals(1, listener.waitValue());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@Test(timeout = 1000L)
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
			Assert.assertEquals(1, (int)future.get());
			Assert.assertEquals(1, result.get());
			Assert.assertEquals(1, listener.waitValue());
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = CancellationException.class, timeout = 1000L)
	public void                     testScheduleAtFixedRate() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleAtFixedRate(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (result) {
							result.incrementAndGet();
							result.notify();
						}
					}
				}, 1L, 1L, TimeUnit.MILLISECONDS);
				Assert.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				Assert.assertEquals(1, result.get());
				result.wait();
				Assert.assertEquals(2, result.get());
				future.setDelayedCancel();
				future.cancel(true);
			}
			future.addListener(listener);
			try {
				future.get();
				Assert.fail("Expected CancellationException");
			}
			finally {
				Assert.assertTrue(listener.waitValue() instanceof CancellationException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ExecutionException.class, timeout = 1000000L)
	public void                     testScheduleAtFixedRateExcepted() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
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
				Assert.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				Assert.assertEquals(1, result.get());
				result.wait();
				Assert.assertEquals(2, result.get());
				Assert.assertFalse(longTermFuture.isDone());
			}
			future.addListener(listener);
			try {
				future.get();
				Assert.fail("Expected ExecutionException");
			}
			finally {
				Assert.assertTrue(listener.waitValue() instanceof TestingRuntimeException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = CancellationException.class, timeout = 1000L)
	public void                     testScheduleWithFixedDelay() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleWithFixedDelay(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (result) {
							result.incrementAndGet();
							result.notify();
						}
					}
				}, 0L, 1L, TimeUnit.MILLISECONDS);
				Assert.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				Assert.assertEquals(1, result.get());
				result.wait();
				Assert.assertEquals(2, result.get());
				future.setDelayedCancel();
				future.cancel(true);
			}
			future.addListener(listener);
			try {
				future.get();
				Assert.fail("Expected CancellationException");
			}
			finally {
				Assert.assertTrue(listener.waitValue() instanceof CancellationException);
			}
		}
		finally {
			executor.shutdownCancelling();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expected = ExecutionException.class, timeout = 1000L)
	public void                     testScheduleWithFixedDelayExcepted() throws InterruptedException, ExecutionException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		try {
			final AtomicInteger result = new AtomicInteger();
			TestListener<Object> listener = new TestListener<Object>();
			ListenableScheduledFuture<Object> future;
			synchronized (result) {
				future = (ListenableScheduledFuture<Object>) executor.scheduleWithFixedDelay(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (result) {
							result.notify();
							if (result.incrementAndGet() == 2)
								throw new TestingRuntimeException();
						}
					}
				}, 0L, 1L, TimeUnit.MILLISECONDS);
				Assert.assertTrue(future.getDelay(TimeUnit.MILLISECONDS) <= 1);
				result.wait();
				Assert.assertEquals(1, result.get());
				result.wait();
				Assert.assertEquals(2, result.get());
			}
			future.addListener(listener);
			try {
				future.get();
				Assert.fail("Expected ExecutionException");
			}
			finally {
				Assert.assertTrue(listener.waitValue() instanceof TestingRuntimeException);
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
