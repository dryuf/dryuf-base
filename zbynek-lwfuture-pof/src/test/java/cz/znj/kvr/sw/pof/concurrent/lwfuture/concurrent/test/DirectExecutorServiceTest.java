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
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListeningExecutorService;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListeningExecutors;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListeningScheduledExecutorService;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.DirectExecutorService DirectExecutorService} class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class DirectExecutorServiceTest
{
	@Test
	public void                     testExecute() throws InterruptedException
	{
		final AtomicInteger result = new AtomicInteger();
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				result.incrementAndGet();
			}
		});
		Assert.assertEquals(1, result.get());
	}

	@Test
	public void                     testException() throws InterruptedException
	{
		final AtomicInteger result = new AtomicInteger();
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				result.incrementAndGet();
				throw new TestingRuntimeException();
			}
		});
		Assert.assertEquals(1, result.get());
	}

	@Test
	public void			testLifecycle1() throws InterruptedException
	{
		ListeningExecutorService executor = getExecutor();
		Assert.assertFalse(executor.isTerminated());
		Assert.assertFalse(executor.isShutdown());
		executor.shutdown();
		executor.shutdownNow();
		Assert.assertFalse(executor.isTerminated());
		Assert.assertFalse(executor.isShutdown());
		Assert.assertTrue(executor.awaitTermination(0, TimeUnit.MILLISECONDS));
	}

	private static ListeningExecutorService getExecutor()
	{
		return ListeningExecutors.directExecutorService();
	}
}
