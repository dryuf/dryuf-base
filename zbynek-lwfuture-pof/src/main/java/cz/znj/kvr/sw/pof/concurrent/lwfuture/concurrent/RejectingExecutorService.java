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


import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * {@link java.util.concurrent.ExecutorService ExecutorService} executing {@link Runnable} directly in this thread.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class RejectingExecutorService extends AbstractListeningExecutorService
{
	public void			execute(Runnable runnable)
	{
		throw new RejectedExecutionException(getClass()+" rejects everything.");
	}

	@Override
	public void                     shutdown()
	{
	}

	@Override
	public List<Runnable>           shutdownNow()
	{
		return Collections.<Runnable>emptyList();
	}

	@Override
	public boolean                  isShutdown()
	{
		return true;
	}

	@Override
	public boolean                  isTerminated()
	{
		return true;
	}

	@Override
	public boolean                  awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return true;
	}

	/**
	 * Gets instance of {@link RejectingExecutorService}.
	 *
	 * @return
	 * 	single instance of {@link RejectingExecutorService}
	 */
	public static RejectingExecutorService getInstance()
	{
		return instance;
	}

	private static RejectingExecutorService instance = new RejectingExecutorService();

	private static Logger		logger = Logger.getLogger(RejectingExecutorService.class.getName());
}
