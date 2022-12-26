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


import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Interface extending {@link ScheduledExecutorService} by return {@link ListenableFuture} instead of {@link Future}.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public interface ListeningScheduledExecutorService extends ListeningExecutorService, ScheduledExecutorService
{
	<V> ListenableScheduledFuture<V> schedule(Runnable command, V result, long delay, TimeUnit unit);

	@Override
	ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

	@Override
	<V> ListenableScheduledFuture<V> schedule(Callable<V> var1, long delay, TimeUnit unit);

	@Override
	ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long delay, long period, TimeUnit unit);

	@Override
	ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long delay, long period, TimeUnit unit);
}
