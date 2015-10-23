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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.test;

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * In these tests we want to raise probability of race condition as high as possible.
 *
 * We start the threads at the beginning than they meet on CyclicBarrier and after 2 milliseconds of busy loops we finally give a green to them.
 *
 * Each of the tests is repeated 100 times.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class AbstractFutureAsyncTest
{
	protected static class ThreadGroup
	{
		public                          ThreadGroup(int threadCount, int actionCount)
		{
			result = new long[threadCount][actionCount];
		}

		public void                     assertResult()
		{
			for (int i = 0; i < result.length; ++i) {
				for (int j = 0; j < result[i].length; ++j) {
					Assert.assertEquals(1, result[i][j]);
				}
			}
		}

		public void                     init(int count)
		{
			fineBarrier = new AtomicBoolean(false);
			threadBarrier = new CyclicBarrier(count, new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2);
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					fineBarrier.set(true);
				}
			});
			threads = new LinkedList<Thread>();
		}

		public void                     startGroupThreads(final CallProc<Integer> runner)
		{
			for (int i = 0; i < result.length; ++i) {
				final int id = i;
				startThread(new Runnable() {
					@Override
					public void run() {
						runner.run(id);
					}
				});
			}
		}

		public Thread                   startThread(final Runnable runner)
		{
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						threadBarrier.await();
					}
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					catch (BrokenBarrierException e) {
						throw new RuntimeException(e);
					}
					while (!fineBarrier.get()) {
					}
					runner.run();
				}
			});
			thread.start();
			threads.add(thread);
			return thread;
		}

		public void                     waitThreads()
		{
			for (Thread thread: threads) {
				try {
					thread.join();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			threads = null;
		}

		public void                     close()
		{
			if (threads != null) {
				for (Thread thread : threads)
					thread.interrupt();
				waitThreads();
			}
		}

		public long[][]                 result;

		protected List<Thread>          threads;

		private AtomicBoolean           fineBarrier;

		private CyclicBarrier           threadBarrier;
	}

	@Test
	public void                     testSuccessMatchProcessors() throws ExecutionException, InterruptedException
	{
		for (int tries = 0; tries < 100; ++tries) {
			final SettableFuture<Void> future = new SettableFuture<Void>();
			final long hit[] = new long[2];
			final ThreadGroup group = new ThreadGroup(Math.max(1, Runtime.getRuntime().availableProcessors()-1), 20);
			try {
				group.init(group.result.length+1);
				group.startGroupThreads(new CallProc<Integer>() {
					@Override
					public void run(Integer id) {
						final long[] r = group.result[id];
						for (int i = 0; i < r.length; ++i) {
							final int aid = i;
							future.addListener(new Runnable()
							{
								@Override
								public void run()
								{
									r[aid] = 1;
								}
							});
						}
						;
					}
				});
				group.startThread(new Runnable() {
					@Override
					public void run() {
						future.addListener(new Runnable() {
							@Override
							public void run()
							{
								hit[0] = 1;
							}
						});
						future.set(null);
						future.addListener(new Runnable() {
							@Override
							public void run()
							{
								hit[1] = 1;
							}
						});
					}
				});
				group.waitThreads();
				group.assertResult();
				Assert.assertArrayEquals(new long[]{1, 1}, hit);
			}
			finally {
				group.close();
			}
		}
	}

	@Test
	public void                     testSuccessOverloadedProcessors() throws ExecutionException, InterruptedException
	{
		for (int tries = 0; tries < 100; ++tries) {
			final SettableFuture<Void> future = new SettableFuture<Void>();
			final long hit[] = new long[2];
			final ThreadGroup group = new ThreadGroup(Math.max(1, Runtime.getRuntime().availableProcessors()*4), 20);
			try {
				group.init(group.result.length+1);
				group.startGroupThreads(new CallProc<Integer>() {
					@Override
					public void run(Integer id) {
						final long[] r = group.result[id];
						for (int i = 0; i < r.length; ++i) {
							final int aid = i;
							future.addListener(new Runnable()
							{
								@Override
								public void run()
								{
									r[aid] = 1;
								}
							});
						}
						;
					}
				});
				group.startThread(new Runnable() {
					@Override
					public void run() {
						future.addListener(new Runnable() {
							@Override
							public void run() {
								hit[0] = 1;
							}
						});
						future.set(null);
						future.addListener(new Runnable() {
							@Override
							public void run() {
								hit[1] = 1;
							}
						});
					}
				});
				group.waitThreads();
				group.assertResult();
				Assert.assertArrayEquals(new long[]{1, 1}, hit);
			}
			finally {
				group.close();
			}
		}
	}
}
