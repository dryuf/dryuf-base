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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Tests for {@link ListeningExecutors} class.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class ListeningExecutorsTest
{
	@Test
	public void			testListeningDecoratorBasic()
	{
		ListeningExecutorService executor = ListeningExecutors.listeningDecorator(Executors.newCachedThreadPool());
		try {
			//AssertJUnit.assertTrue(executor instanceof ListeningExecutorService);
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
			AssertJUnit.assertTrue(executor instanceof ListeningScheduledExecutorService);
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
			//AssertJUnit.assertTrue(executor instanceof ListeningScheduledExecutorService);
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void			testDirectExecutor()
	{
		AssertJUnit.assertNotNull(ListeningExecutors.directExecutor());
	}


	@Test
	public void			testRejectingExecutor()
	{
		AssertJUnit.assertNotNull(ListeningExecutors.rejectingExecutor());
	}
}
