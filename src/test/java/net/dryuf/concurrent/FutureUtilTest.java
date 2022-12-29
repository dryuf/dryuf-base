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

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * {@link FutureUtil} tests.
 */
public class FutureUtilTest
{
	@Test
	public void toCompletable_success_success() throws ExecutionException, InterruptedException
	{
		ListenableFuture<Integer> listenable = Futures.successFuture(0);
		CompletableFuture<Integer> future = FutureUtil.toCompletable(listenable);
		Assert.assertEquals(future.get(), (Integer) 0);
	}

	@Test
	public void toCompletable_exception_exception() throws ExecutionException, InterruptedException
	{
		ListenableFuture<Integer> listenable = Futures.failedFuture(new NumberFormatException());
		CompletableFuture<Integer> future = FutureUtil.toCompletable(listenable);
		ExecutionException ex = Assert.expectThrows(ExecutionException.class, future::get);
		MatcherAssert.assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void toCompletable_cancel_cancel() throws ExecutionException, InterruptedException
	{
		ListenableFuture<Integer> listenable = Futures.cancelledFuture();
		CompletableFuture<Integer> future = FutureUtil.toCompletable(listenable);
		CancellationException ex = Assert.expectThrows(CancellationException.class, future::get);
	}

	@Test
	public void exception_exception_exception()
	{
		CompletableFuture<Integer> future = FutureUtil.exception(new NumberFormatException());
		ExecutionException ex = Assert.expectThrows(ExecutionException.class, future::get);
		MatcherAssert.assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test(expectedExceptions = NumberFormatException.class)
	public void anyAndCancel_oneFailed_fails() throws Throwable
	{
		CompletableFuture<Void> one = new CompletableFuture<>();
		try {
			FutureUtil.anyAndCancel(Arrays.asList(
				one,
				FutureUtil.exception(new NumberFormatException())
			)).get();
		}
		catch (ExecutionException e) {
			assertTrue(one.isCancelled());
			throw e.getCause();
		}
	}

	@Test(expectedExceptions = CancellationException.class)
	public void anyAndCancel_cancel_allCancelled() throws Throwable
	{
		CompletableFuture<Void> one = new CompletableFuture<>();
		CompletableFuture<Void> two = new CompletableFuture<>();
		try {
			CompletableFuture<Void> future = FutureUtil.anyAndCancel(Arrays.asList(
				one,
				two
			));
			future.cancel(true);
			future.get();
		}
		catch (ExecutionException e) {
			assertTrue(one.isCancelled());
			assertTrue(two.isCancelled());
			throw e.getCause();
		}
	}

	@Test
	public void submitDirect_success_success() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitDirect(() -> 1);
		Assert.assertEquals(future.get(), (Integer) 1);
	}

	@Test
	public void submitDirect_exception_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitDirect(() -> { throw new NumberFormatException(); });
		ExecutionException ex = Assert.expectThrows(ExecutionException.class, future::get);
		MatcherAssert.assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void submitAsync_success_success() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> 1);
		Assert.assertEquals(future.get(), (Integer) 1);
	}

	@Test
	public void submitAsync_exception_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> { throw new NumberFormatException(); });
		ExecutionException ex = Assert.expectThrows(ExecutionException.class, future::get);
		MatcherAssert.assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void submitAsyncExecutor_success_success() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> 1, DirectExecutor.getInstance());
		Assert.assertEquals(future.get(), (Integer) 1);
	}

	@Test
	public void submitAsyncExecutor_exception_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> { throw new NumberFormatException(); }, DirectExecutor.getInstance());
		ExecutionException ex = Assert.expectThrows(ExecutionException.class, future::get);
		MatcherAssert.assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void submitAsyncExecutor_closed_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> { throw new NumberFormatException(); }, RejectingExecutor.getInstance());
		ExecutionException ex = Assert.expectThrows(ExecutionException.class, future::get);
		MatcherAssert.assertThat(ex.getCause(), IsInstanceOf.instanceOf(RejectedExecutionException.class));
	}

	@Test
	public void completeOrFail_success_success() throws ExecutionException, InterruptedException
	{
		@SuppressWarnings("unchecked")
		CompletableFuture<Integer> result = Mockito.mock(CompletableFuture.class);
		FutureUtil.completeOrFail(result, 0, null);
		Mockito.verify(result, times(1))
			.complete(0);
	}

	@Test
	public void completeOrFail_exception_excepted() throws ExecutionException, InterruptedException
	{
		@SuppressWarnings("unchecked")
		CompletableFuture<Integer> result = Mockito.mock(CompletableFuture.class);
		FutureUtil.completeOrFail(result, null, new NumberFormatException());
		Mockito.verify(result, times(1))
			.completeExceptionally(any(NumberFormatException.class));
	}

	@Test
	public void whenException_exception_call()
	{
		@SuppressWarnings("unchecked")
		Consumer<Throwable> consumer = mock(Consumer.class);
		FutureUtil.whenException(consumer)
			.accept(null, new NumberFormatException());
		verify(consumer, times(1))
			.accept(any(NumberFormatException.class));
	}

	@Test
	public void whenException_success_none()
	{
		@SuppressWarnings("unchecked")
		Consumer<Throwable> consumer = mock(Consumer.class);
		FutureUtil.whenException(consumer)
			.accept(0, null);
		verify(consumer, times(0))
			.accept(any(NumberFormatException.class));
	}

	@Test(timeOut = 10000L)
	public void waitUninterruptibly_uninterrupted_false()
	{
		Object lock = new Object();
		synchronized (lock) {
			CompletableFuture.runAsync(() -> { synchronized (lock) { lock.notify(); } });
			boolean interrupted = FutureUtil.waitUninterruptibly(lock);
			assertFalse(interrupted);
		}
	}

	@Test(timeOut = 10000L)
	public void waitUninterruptibly_interrupted_true()
	{
		Object lock = new Object();
		Thread.currentThread().interrupt();
		synchronized (lock) {
			CompletableFuture.runAsync(() -> { synchronized (lock) { lock.notify(); } });
			boolean interrupted = FutureUtil.waitUninterruptibly(lock);
			assertTrue(interrupted);
		}
	}

	@Test(timeOut = 10000L)
	public void waitUninterruptiblyKeepInterrupt_uninterrupted_false()
	{
		Object lock = new Object();
		synchronized (lock) {
			CompletableFuture.runAsync(() -> { synchronized (lock) { lock.notify(); } });
			FutureUtil.waitUninterruptiblyKeepInterrupt(lock);
			boolean interrupted = Thread.interrupted();
			assertFalse(interrupted);
		}
	}

	@Test(timeOut = 10000L)
	public void waitUninterruptiblyKeepInterrupt_interrupted_true()
	{
		Object lock = new Object();
		Thread.currentThread().interrupt();
		synchronized (lock) {
			CompletableFuture.runAsync(() -> { synchronized (lock) { lock.notify(); } });
			FutureUtil.waitUninterruptiblyKeepInterrupt(lock);
			boolean interrupted = Thread.interrupted();
			assertTrue(interrupted);
		}
	}
}
