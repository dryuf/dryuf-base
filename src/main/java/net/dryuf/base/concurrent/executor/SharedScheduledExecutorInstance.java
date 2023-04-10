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

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Instance of {@link ScheduledExecutorService}, common pool to be shared by runtime components requiring
 * scheduling.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class SharedScheduledExecutorInstance
{
	/** Time (in ns) to safely propagate memory to caches without using explicit memory barrier in user code
	 * (they're used either by JVM during stop-the-world or by operating system when switching context. */
	public static final long	MEMORY_PROPAGATION_DELAY_NS =
			Optional.ofNullable(System.getProperty("net.dryuf.concurrent.memoryPropagationDelay"))
			.map(Long::valueOf)
			.orElse(1_200_000_000L);

	/**
	 * Gets the instance of shared ListeningScheduledExecutor.
	 *
	 * @return
	 * 	instance of shared ListeningScheduledExecutor.
	 */
	public static ScheduledExecutorService getScheduledExecutorService()
	{
		return scheduledExecutorService;
	}

	private static final ScheduledExecutorService scheduledExecutorService =
		Executors.newScheduledThreadPool(
			Runtime.getRuntime().availableProcessors(),
			new ThreadFactory() {
				AtomicInteger counter = new AtomicInteger();

				@Override
				public Thread newThread(Runnable r) {
					Thread t = Executors.defaultThreadFactory().newThread(r);
					t.setName(SharedScheduledExecutorInstance.class.getName() + -counter.incrementAndGet());
					t.setDaemon(true);
					return t;
				}
			}
		);
}
