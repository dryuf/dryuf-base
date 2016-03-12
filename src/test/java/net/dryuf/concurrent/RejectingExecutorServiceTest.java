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

import net.dryuf.concurrent.ListeningExecutorService;
import net.dryuf.concurrent.ListeningExecutors;
import net.dryuf.concurrent.RejectingExecutorService;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Tests for {@link net.dryuf.concurrent.RejectingExecutorService RejectingExecutorService} class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr000@gmail.com http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class RejectingExecutorServiceTest
{
	@Test(expectedExceptions = RejectedExecutionException.class)
	public void                     testFailure()
	{
		final AtomicInteger result = new AtomicInteger();
		try {
			RejectingExecutorService.getInstance().execute(new Runnable() {
				@Override
				public void run() {
					result.incrementAndGet();
				}
			});
		}
		finally {
			AssertJUnit.assertEquals(0, result.get());
		}
	}

	@Test
	public void			testLifecycle1() throws InterruptedException
	{
		ListeningExecutorService executor = getExecutor();
		AssertJUnit.assertTrue(executor.isTerminated());
		AssertJUnit.assertTrue(executor.isShutdown());
		executor.shutdown();
		executor.shutdownNow();
		AssertJUnit.assertTrue(executor.isTerminated());
		AssertJUnit.assertTrue(executor.isShutdown());
		AssertJUnit.assertTrue(executor.awaitTermination(0, TimeUnit.MILLISECONDS));
	}

	private static ListeningExecutorService getExecutor()
	{
		return ListeningExecutors.rejectingExecutorService();
	}
}
