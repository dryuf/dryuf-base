/*
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/
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

package net.dryuf.concurrent.executor;

import java.util.concurrent.CompletableFuture;


/**
 * {@link CloseableExecutor} executing items in common pool.  The implementation waits for current executions upon
 * {@link #close()} .
 */
public class CommonPoolExecutor extends AbstractCloseableExecutor
{
	/**
	 * Gets new instance of common pool executor, not closing the common pool but still waiting for futures.
	 *
	 * @return
	 * 	new {@link CommonPoolExecutor}
	 */
	public static CommonPoolExecutor getInstance()
	{
		return new CommonPoolExecutor();
	}

	@Override
	protected void execute0(Runnable runnable)
	{
		CompletableFuture.runAsync(runnable);
	}
}
