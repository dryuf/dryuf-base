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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Tests for {@link ListeningExecutors} class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class ListeningExecutorsTest
{
	@Test
	public void			testListeningDecoratorBasic()
	{
		ListeningExecutorService executor = ListeningExecutors.listeningDecorator(Executors.newCachedThreadPool());
		try {
			//Assert.assertTrue(executor instanceof ListeningExecutorService);
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void			testListeningDecoratorBasicToScheduled()
	{
		ListeningExecutorService executor = ListeningExecutors.listeningDecorator((ExecutorService)Executors.newScheduledThreadPool(1));
		try {
			Assert.assertTrue(executor instanceof ListeningScheduledExecutorService);
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void			testListeningDecoratorScheduled()
	{
		ListeningScheduledExecutorService executor = ListeningExecutors.listeningDecorator(Executors.newScheduledThreadPool(1));
		try {
			//Assert.assertTrue(executor instanceof ListeningScheduledExecutorService);
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void			testDirectExecutor()
	{
		Assert.assertNotNull(ListeningExecutors.directExecutor());
	}


	@Test
	public void			testRejectingExecutor()
	{
		Assert.assertNotNull(ListeningExecutors.rejectingExecutor());
	}
}
