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

package net.dryuf.base.concurrent.executor;

import net.dryuf.base.concurrent.executor.SharedScheduledExecutorInstance;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 * Tests for {@link SharedScheduledExecutorInstance} .
 */
public class SharedScheduledExecutorInstanceTest
{
	@Test(timeOut = 2000L)
	public void schedule_sleep_wait() throws Exception
	{
		@SuppressWarnings("unchecked")
		Callable<Integer> callback = Mockito.mock(Callable.class);
		Future<Integer> future = SharedScheduledExecutorInstance.getScheduledExecutorService()
			.schedule(callback, 500, TimeUnit.MILLISECONDS);
		Mockito.verify(callback, Mockito.times(0))
			.call();
		future.get();
		Mockito.verify(callback, Mockito.times(1))
			.call();
	}
}
