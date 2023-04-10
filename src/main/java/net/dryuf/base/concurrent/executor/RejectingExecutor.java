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


import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;


/**
 * {@link CloseableExecutor} which rejects everything.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class RejectingExecutor implements CloseableExecutor
{
	private static RejectingExecutor instance = new RejectingExecutor();

	/**
	 * Gets instance of {@link DirectExecutor}.
	 *
	 * @return
	 * 	single instane of {@link DirectExecutor}
	 */
	public static RejectingExecutor	getInstance()
	{
		return instance;
	}

	private RejectingExecutor()
	{
	}

	@Override
	public void			execute(Runnable runnable)
	{
		throw new RejectedExecutionException(getClass()+" rejects everything.");
	}

	@Override
	public <T> CompletableFuture<T> submit(Callable<T> callable)
	{
		throw new RejectedExecutionException(getClass()+" rejects everything.");
	}

	@Override
	public void close()
	{
	}
}
