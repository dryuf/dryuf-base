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

import net.dryuf.concurrent.ListenableFuture;
import net.dryuf.concurrent.ListeningExecutorService;
import net.dryuf.concurrent.ListeningExecutors;
import net.dryuf.concurrent.ListeningScheduledExecutorService;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link net.dryuf.concurrent.ForwardingListeningExecutorService ForwardingListeningExecutorService} class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr000@gmail.com http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class ForwardingListeningExecutorServiceTest
{
	@Test
	public void                     testExecute() throws InterruptedException
	{
		final AtomicInteger result = new AtomicInteger();
		synchronized (result) {
			getExecutor().execute(new Runnable()
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

	@Test
	public void                     testShutdownCancelling() throws ExecutionException, InterruptedException
	{
		ListeningExecutorService executor = ListeningExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
		try {
			ListenableFuture<Object> f0 = executor.submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Thread.sleep(1000000L);
					return null;
				}
			});
			ListenableFuture<Object> f1 = executor.submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Thread.sleep(1000000L);
					return null;
				}
			});
			executor.shutdownCancelling();
			AssertJUnit.assertTrue(f1.isCancelled());
			try {
				f0.get();
				AssertJUnit.fail("Previous should have thrown CancellationException");
			}
			catch (CancellationException ex) {
			}
			AssertJUnit.assertTrue(f0.isCancelled());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void			testLifecycle1() throws InterruptedException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		AssertJUnit.assertFalse(executor.isTerminated());
		AssertJUnit.assertFalse(executor.isShutdown());
		executor.shutdown();
		AssertJUnit.assertTrue(executor.isTerminated());
		AssertJUnit.assertTrue(executor.isShutdown());
		AssertJUnit.assertTrue(executor.awaitTermination(0, TimeUnit.MILLISECONDS));
	}

	@Test
	public void			testLifecycle2() throws InterruptedException
	{
		ListeningScheduledExecutorService executor = getExecutor();
		AssertJUnit.assertFalse(executor.isTerminated());
		AssertJUnit.assertFalse(executor.isShutdown());
		executor.shutdownNow();
		AssertJUnit.assertTrue(executor.isTerminated());
		AssertJUnit.assertTrue(executor.isShutdown());
		AssertJUnit.assertTrue(executor.awaitTermination(0, TimeUnit.MILLISECONDS));
	}

	private static ListeningScheduledExecutorService getExecutor()
	{
		return ListeningExecutors.listeningDecorator(Executors.newScheduledThreadPool(2));
	}
}
