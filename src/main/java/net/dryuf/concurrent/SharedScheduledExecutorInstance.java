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

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Instance of {@link ListeningScheduledExecutorService}, common pool to be shared by runtime components requiring
 * scheduling.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr000@gmail.com http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
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
	public static ListeningScheduledExecutorService getScheduledExecutorService()
	{
		return scheduledExecutorService;
	}

	private static final ListeningScheduledExecutorService scheduledExecutorService =
			ListeningExecutors.listeningDecorator(Executors.newScheduledThreadPool(
				Runtime.getRuntime().availableProcessors(),
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = Executors.defaultThreadFactory().newThread(r);
						t.setDaemon(true);
						return t;
					}
				}
			));
}
