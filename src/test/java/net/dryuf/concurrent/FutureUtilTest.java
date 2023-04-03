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

import com.google.common.collect.ImmutableList;
import net.dryuf.concurrent.function.ThrowingConsumer;
import net.dryuf.concurrent.function.ThrowingFunction;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


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
		assertEquals(future.get(), (Integer) 0);
	}

	@Test
	public void toCompletable_exception_exception() throws ExecutionException, InterruptedException
	{
		ListenableFuture<Integer> listenable = Futures.failedFuture(new NumberFormatException());
		CompletableFuture<Integer> future = FutureUtil.toCompletable(listenable);
		ExecutionException ex = expectThrows(ExecutionException.class, future::get);
		assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void toCompletable_cancel_cancel() throws ExecutionException, InterruptedException
	{
		ListenableFuture<Integer> listenable = Futures.cancelledFuture();
		CompletableFuture<Integer> future = FutureUtil.toCompletable(listenable);
		CancellationException ex = expectThrows(CancellationException.class, future::get);
	}

	@Test
	public void exception_exception_exception()
	{
		CompletableFuture<Integer> future = FutureUtil.exception(new NumberFormatException());
		ExecutionException ex = expectThrows(ExecutionException.class, future::get);
		assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
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
		assertEquals(future.get(), (Integer) 1);
	}

	@Test
	public void submitDirect_exception_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitDirect(() -> { throw new NumberFormatException(); });
		ExecutionException ex = expectThrows(ExecutionException.class, future::get);
		assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void submitAsync_success_success() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> 1);
		assertEquals(future.get(), (Integer) 1);
	}

	@Test
	public void submitAsync_exception_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> { throw new NumberFormatException(); });
		ExecutionException ex = expectThrows(ExecutionException.class, future::get);
		assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void submitAsyncExecutor_success_success() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> 1, DirectExecutor.getInstance());
		assertEquals(future.get(), (Integer) 1);
	}

	@Test
	public void submitAsyncExecutor_exception_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> { throw new NumberFormatException(); }, DirectExecutor.getInstance());
		ExecutionException ex = expectThrows(ExecutionException.class, future::get);
		assertThat(ex.getCause(), IsInstanceOf.instanceOf(NumberFormatException.class));
	}

	@Test
	public void submitAsyncExecutor_closed_excepted() throws ExecutionException, InterruptedException
	{
		CompletableFuture<Integer> future = FutureUtil.submitAsync(() -> { throw new NumberFormatException(); }, RejectingExecutor.getInstance());
		ExecutionException ex = expectThrows(ExecutionException.class, future::get);
		assertThat(ex.getCause(), IsInstanceOf.instanceOf(RejectedExecutionException.class));
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
	public void whenException_exception_call() throws Exception
	{
		@SuppressWarnings("unchecked")
		ThrowingConsumer<Exception, Exception> consumer = mock(ThrowingConsumer.class);
		FutureUtil.whenException(consumer)
			.accept(null, new NumberFormatException());
		verify(consumer, times(1))
			.accept(any(NumberFormatException.class));
	}

	@Test
	public void whenException_success_none() throws Exception
	{
		@SuppressWarnings("unchecked")
		ThrowingConsumer<Exception, Exception> consumer = mock(ThrowingConsumer.class);
		FutureUtil.whenException(consumer)
			.accept(0, null);
		verify(consumer, times(0))
			.accept(any(NumberFormatException.class));
	}

	@Test
	public void sneakyGet_success_return() throws Exception
	{
		CompletableFuture<Integer> source = CompletableFuture.completedFuture(5);
		Integer result = FutureUtil.sneakyGet(source);

		assertEquals(result, (Integer) 5);
	}

	@Test
	public void sneakyGet_exception_throw() throws Exception
	{
		CompletableFuture<Integer> source = FutureUtil.exception(new NumberFormatException());
		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(source));
	}

	@Test
	public void sneakyGet_interrupted_throw() throws Exception
	{
		CompletableFuture<Integer> source = new CompletableFuture<>();
		expectThrows(InterruptedException.class, () -> {
			Thread.currentThread().interrupt();
			FutureUtil.sneakyGet(source);
		});
	}

	@Test
	public void sneakyGetNow_success_return() throws Exception
	{
		CompletableFuture<Integer> source = CompletableFuture.completedFuture(5);
		Integer result = FutureUtil.sneakyGetNow(source, null);

		assertEquals(result, (Integer) 5);
	}

	@Test
	public void sneakyGetNow_exception_throw() throws Exception
	{
		CompletableFuture<Integer> source = FutureUtil.exception(new NumberFormatException());
		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGetNow(source, null));
	}

	@Test
	public void copy_success_propagate() throws Exception
	{
		CompletableFuture<Integer> source = CompletableFuture.completedFuture(5);
		CompletableFuture<Integer> target = new CompletableFuture<>();
		FutureUtil.copy(source, target);

		assertEquals(target.get(), (Integer) 5);
	}

	@Test
	public void copy_exception_propagate() throws Exception
	{
		CompletableFuture<Integer> source = FutureUtil.exception(new NumberFormatException());
		CompletableFuture<Integer> target = new CompletableFuture<>();
		FutureUtil.copy(source, target);

		ExecutionException ex = expectThrows(ExecutionException.class, () -> target.get());
		assertThat(ex.getCause(), instanceOf(NumberFormatException.class));
	}

	@Test
	public void join_success_resultLast() throws Exception
	{
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(5);
		CompletableFuture<Integer> two = CompletableFuture.completedFuture(6);
		CompletableFuture<Integer> out = FutureUtil.join(one, two, true);

		assertEquals(out.get(), (Integer) 6);
	}

	@Test
	public void join_failedFirst_immediately() throws Exception
	{
		CompletableFuture<Integer> one = FutureUtil.exception(new NumberFormatException());
		CompletableFuture<Integer> two = new CompletableFuture<>();
		CompletableFuture<Integer> out = FutureUtil.join(one, two, true);

		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(out));
		assertEquals(two.isCancelled(), true);
	}

	@Test
	public void join_failedSecond_immediately() throws Exception
	{
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(5);
		CompletableFuture<Integer> two = FutureUtil.exception(new NumberFormatException());
		CompletableFuture<Integer> out = FutureUtil.join(one, two, true);

		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(out));
	}

	@Test
	public void composeAlways_success_propagate() throws Exception
	{
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(5);
		Callable<CompletableFuture<Integer>> handler = mock(Callable.class);
		when(handler.call())
			.thenReturn(CompletableFuture.completedFuture(6));

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		assertEquals(out.get(), (Integer) 6);
		verify(handler, times(1))
			.call();
	}

	@Test
	public void composeAlways_exception_propagate() throws Exception
	{
		CompletableFuture<Integer> one = FutureUtil.exception(new NumberFormatException());
		Callable<CompletableFuture<Integer>> handler = mock(Callable.class);
		when(handler.call())
			.thenReturn(CompletableFuture.completedFuture(6));

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(out));
		verify(handler, times(1))
			.call();
	}

	@Test
	public void composeAlways_handlerException_propagate() throws Exception
	{
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(5);
		Callable<CompletableFuture<Integer>> handler = mock(Callable.class);
		when(handler.call())
			.thenThrow(new NumberFormatException());

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(out));
		verify(handler, times(1))
			.call();
	}

	@Test
	public void composeAlways_bothException_propagateFirst() throws Exception
	{
		CompletableFuture<Integer> one = FutureUtil.exception(new IOException());
		Callable<CompletableFuture<Integer>> handler = mock(Callable.class);
		when(handler.call())
			.thenThrow(new NumberFormatException());

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		expectThrows(IOException.class, () -> FutureUtil.sneakyGet(out));
		verify(handler, times(1))
			.call();
	}

	@Test
	public void composeAlwaysProcess_success_propagate() throws Exception
	{
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(5);
		ThrowingFunction<Integer, CompletableFuture<Integer>, NumberFormatException> handler = mock(ThrowingFunction.class);
		when(handler.apply(5))
			.thenReturn(CompletableFuture.completedFuture(6));

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		assertEquals(out.get(), (Integer) 6);
		verify(handler, times(1))
			.apply(5);
	}

	@Test
	public void composeAlwaysProcess_exception_propagate() throws Exception
	{
		CompletableFuture<Integer> one = FutureUtil.exception(new NumberFormatException());
		ThrowingFunction<Integer, CompletableFuture<Integer>, NumberFormatException> handler = mock(ThrowingFunction.class);
		when(handler.apply(null))
			.thenReturn(CompletableFuture.completedFuture(6));

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(out));
		verify(handler, times(1))
			.apply(null);
	}

	@Test
	public void composeAlwaysProcess_handlerException_propagate() throws Exception
	{
		CompletableFuture<Integer> one = CompletableFuture.completedFuture(5);
		ThrowingFunction<Integer, CompletableFuture<Integer>, NumberFormatException> handler = mock(ThrowingFunction.class);
		when(handler.apply(5))
			.thenThrow(new NumberFormatException());

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		expectThrows(NumberFormatException.class, () -> FutureUtil.sneakyGet(out));
		verify(handler, times(1))
			.apply(5);
	}

	@Test
	public void composeAlwaysProcess_bothException_propagateFirst() throws Exception
	{
		CompletableFuture<Integer> one = FutureUtil.exception(new IOException());
		ThrowingFunction<Integer, CompletableFuture<Integer>, NumberFormatException> handler = mock(ThrowingFunction.class);
		when(handler.apply(null))
			.thenReturn(CompletableFuture.completedFuture(6));

		CompletableFuture<Integer> out = FutureUtil.composeAlways(one, handler);

		expectThrows(IOException.class, () -> FutureUtil.sneakyGet(out));
		verify(handler, times(1))
			.apply(null);
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

	@Test
	public void nestedAllOrCancel_empty_completed() throws ExecutionException, InterruptedException
	{
		CompletableFuture<List<AutoCloseable>> result = FutureUtil.nestedAllOrCancel(Collections.emptyList());

		assertEquals(result.get(), Collections.emptyList());
	}

	@Test
	public void nestedAllOrCancel_allCompleted_completed() throws Exception
	{
		AutoCloseable c0 = mock(AutoCloseable.class);
		AutoCloseable c1 = mock(AutoCloseable.class);
		CompletableFuture<List<AutoCloseable>> result = FutureUtil.nestedAllOrCancel(ImmutableList.of(
				CompletableFuture.completedFuture(c0),
				CompletableFuture.completedFuture(c1)
		));

		assertEquals(result.get(), ImmutableList.of(c0, c1));
		verify(c0, times(0))
			.close();
		verify(c1, times(0))
			.close();
	}

	@Test
	public void nestedAllOrCancel_exception_allClosed() throws Exception
	{
		AutoCloseable c0 = mock(AutoCloseable.class);
		AutoCloseable c1 = mock(AutoCloseable.class);
		CompletableFuture<List<AutoCloseable>> result = FutureUtil.nestedAllOrCancel(ImmutableList.of(
				CompletableFuture.completedFuture(c0),
				FutureUtil.exception(new NumberFormatException()),
				CompletableFuture.completedFuture(c1)
		));

		verify(c0, times(1))
			.close();
		verify(c1, times(1))
			.close();
	}

	@Test
	public void nestedAllOrCancel_exception_allCancelled() throws Exception
	{
		AutoCloseable c0 = mock(AutoCloseable.class);
		CompletableFuture<AutoCloseable> c1 = mock(CompletableFuture.class);
		CompletableFuture<List<AutoCloseable>> result = FutureUtil.nestedAllOrCancel(ImmutableList.of(
				CompletableFuture.completedFuture(c0),
				FutureUtil.exception(new NumberFormatException()),
				c1
		));

		verify(c0, times(1))
			.close();
		verify(c1, times(1))
			.cancel(true);
	}
}
