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


import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Executor} executing {@link Runnable} directly in this thread.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class DirectExecutor implements Executor
{
	public void			execute(Runnable runnable)
	{
		try {
			runnable.run();
		}
		catch (RuntimeException ex) {
			logger.log(Level.SEVERE, "DirectExecutor: Runnable raised RuntimeException while executing Runnable "+runnable, ex);
		}
	}

	/**
	 * Gets instance of {@link DirectExecutor}.
	 *
	 * @return
	 * 	single instance of {@link DirectExecutor}
	 */
	public static DirectExecutor	getInstance()
	{
		return instance;
	}

	private static DirectExecutor	instance = new DirectExecutor();

	private static Logger		logger = Logger.getLogger(DirectExecutor.class.getName());
}
