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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;


import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Interface extending {@link ScheduledExecutorService} by return {@link ListenableFuture} instead of {@link Future}.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public interface ListeningScheduledExecutorService extends ListeningExecutorService, ScheduledExecutorService
{
	<V> ListenableScheduledFuture<V> schedule(Runnable command, V result, long delay, TimeUnit unit);

	ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

	<V> ListenableScheduledFuture<V> schedule(Callable<V> var1, long delay, TimeUnit unit);

	ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long delay, long var4, TimeUnit unit);

	ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long delay, long var4, TimeUnit unit);
}
