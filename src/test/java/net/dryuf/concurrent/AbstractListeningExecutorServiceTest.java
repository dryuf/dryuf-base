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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Tests for {@link AbstractListeningExecutorService AbstractListeningExecutorService} class.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
 */
public class AbstractListeningExecutorServiceTest
{
	@Test(timeOut = 1000L)
	public void			testSubmitCallable() throws ExecutionException, InterruptedException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			TestListener<Integer> test = new TestListener<Integer>();
			AssertJUnit.assertEquals(1, (int) executor.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return 1;
				}
			}).addListener(test).get());
			AssertJUnit.assertEquals(1, (int)(Integer)test.waitValue());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test(timeOut = 1000L)
	public void			testSubmitRunnableValue() throws ExecutionException, InterruptedException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			TestListener<Integer> test = new TestListener<Integer>();
			AssertJUnit.assertEquals(1, (int) executor.submit(new Runnable() {
				@Override
				public void run() {
				}
			}, 1).addListener(test).get());
			AssertJUnit.assertEquals(1, (int)(Integer)test.waitValue());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 1000L)
	public void			testSubmitRunnableWithout() throws ExecutionException, InterruptedException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			TestListener<Object> test = new TestListener<Object>();
			AssertJUnit.assertNull(((ListenableFuture<Object>)executor.submit(new Runnable() {
				@Override
				public void run() {
				}
			})).addListener(test).get());
			AssertJUnit.assertNull(test.waitValue());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test(timeOut = 1000L)
	public void			testInvokeAll() throws InterruptedException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			@SuppressWarnings("unchecked")
			List<Future<Integer>> futures = executor.invokeAll(Arrays.asList(
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 0;
						}
					},
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 1;
						}
					}
			));
			AssertJUnit.assertTrue(futures.get(0) instanceof ListenableFuture);
			AssertJUnit.assertTrue(futures.get(1) instanceof ListenableFuture);
			AssertJUnit.assertEquals(0, (int)futures.get(0).get());
			AssertJUnit.assertEquals(1, (int)futures.get(1).get());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test(timeOut = 1000L)
	public void			testInvokeAllTimed() throws InterruptedException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			@SuppressWarnings("unchecked")
			List<Future<Integer>> futures = executor.invokeAll(Arrays.asList(
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 0;
						}
					},
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 1;
						}
					}
			), 1000, TimeUnit.MILLISECONDS);
			AssertJUnit.assertTrue(futures.get(0) instanceof ListenableFuture);
			AssertJUnit.assertTrue(futures.get(1) instanceof ListenableFuture);
			AssertJUnit.assertEquals(0, (int)futures.get(0).get());
			AssertJUnit.assertEquals(1, (int)futures.get(1).get());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test(timeOut = 1000L)
	public void			testInvokeAllTimeout() throws InterruptedException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			@SuppressWarnings("unchecked")
			List<Future<Integer>> futures = executor.invokeAll(Arrays.asList(
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							Thread.sleep(10000);
							return 0;
						}
					},
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 1;
						}
					}
			), 10, TimeUnit.MILLISECONDS);
			AssertJUnit.assertTrue(futures.get(0) instanceof ListenableFuture);
			AssertJUnit.assertTrue(futures.get(1) instanceof ListenableFuture);
			AssertJUnit.assertTrue(futures.get(0).isCancelled());
			AssertJUnit.assertEquals(1, (int)futures.get(1).get());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 1000L)
	public void			testInvokeAny() throws InterruptedException, TimeoutException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			AssertJUnit.assertEquals(0, (int)executor.invokeAny(Arrays.asList(
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							Thread.sleep(10000);
							return 0;
						}
					},
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 0;
						}
					}
			)));
		}
		finally {
			executor.shutdown();
			executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
		}
	}

	@SuppressWarnings("unchecked")
	@Test(timeOut = 1000L)
	public void			testInvokeAnyTimed() throws InterruptedException, TimeoutException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			AssertJUnit.assertEquals(0, (int)executor.invokeAny(Arrays.asList(
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							Thread.sleep(10000);
							return 0;
						}
					},
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							return 0;
						}
					}
			), 2000, TimeUnit.MILLISECONDS));
		}
		finally {
			executor.shutdown();
			executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = TimeoutException.class, timeOut = 1000L)
	public void			testInvokeAnyTimeout() throws InterruptedException, TimeoutException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			AssertJUnit.assertEquals(0, (int)executor.invokeAny(Arrays.asList(
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							Thread.sleep(10000);
							return 0;
						}
					},
					new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							Thread.sleep(10000);
							return 0;
						}
					}
			), 1, TimeUnit.MILLISECONDS));
		}
		finally {
			executor.shutdown();
			executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
		}
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ExecutionException.class, timeOut = 1000L)
	public void			testInvokeAnyExcepted() throws InterruptedException, TimeoutException, ExecutionException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			executor.invokeAny(Arrays.asList(
					new Callable<Integer>()
					{
						@Override
						public Integer call() throws Exception
						{
							throw new TestingRuntimeException();
						}
					},
					new Callable<Integer>()
					{
						@Override
						public Integer call() throws Exception
						{
							throw new TestingRuntimeException();
						}
					}
			));
			AssertJUnit.fail("Unreachable, should throw TestingRuntimeException");
		}
		finally {
			executor.shutdown();
			executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
		}
	}

	@Test(expectedExceptions = CancellationException.class, timeOut = 1000L)
	public void			testShutdownCancelling() throws ExecutionException, InterruptedException
	{
		ListeningExecutorService executor = getExecutor();
		try {
			int i = 0;
			ListenableFuture<Void> future = executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Thread.sleep(10000);
					return null;
				}
			});
			executor.shutdownCancelling();
			try {
				future.get();
				AssertJUnit.fail("get() should have thrown an CancellationException");
			}
			catch (ExecutionException ex) {
				AssertJUnit.fail("get() should have thrown an CancellationException");
			}
		}
		finally {
			executor.shutdown();
			executor.awaitTermination(10000L, TimeUnit.MILLISECONDS);
		}
	}

//	<T> T invokeAny(Collection<? extends Callable<T>> var1) throws InterruptedException, ExecutionException;
//
//	<T> T invokeAny(Collection<? extends Callable<T>> var1, long var2, TimeUnit var4) throws InterruptedException, ExecutionException, TimeoutException;

	private static ListeningExecutorService getExecutor()
	{
		return ListeningExecutors.listeningDecorator(Executors.newCachedThreadPool());
	}
}
