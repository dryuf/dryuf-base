/*
 * Copyright 2015 Zbynek Vyskovsky mailto:kvr000@gmail.com http://kvr.znj.cz/ http://github.com/kvr000/
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

package net.dryuf.concurrent.queue;

import net.dryuf.concurrent.DirectExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link SingleConsumerQueue}.
 */
public class SingleConsumerQueueTest
{
	@Test
	public void testAdd()
	{
		Runnable restarter = mock(Runnable.class);
		SingleConsumerQueue<Integer> subject = new SingleConsumerQueue<>(restarter, DirectExecutor.getInstance());
		verify(restarter, times(0)).run();
		subject.add(0);
		verify(restarter, times(1)).run();
		subject.add(1);
		verify(restarter, times(1)).run();
		subject.add(1);
		verify(restarter, times(1)).run();
	}

	@Test
	public void testOrder()
	{
		Runnable restarter = mock(Runnable.class);
		SingleConsumerQueue<Integer> subject = new SingleConsumerQueue<>(restarter, DirectExecutor.getInstance());
		subject.add(0);
		subject.add(1);
		subject.add(2);
		verify(restarter, times(1)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.next(), (Integer) 0);
			Assert.assertEquals(consumer.next(), (Integer) 1);
			Assert.assertEquals(consumer.next(), (Integer) 2);
			Assert.assertEquals(consumer.next(), null);
		}
	}

	@Test
	public void testParallelOrder()
	{
		Runnable consumerCallback = mock(Runnable.class);
		SingleConsumerQueue<Integer> subject = new SingleConsumerQueue<>(consumerCallback, DirectExecutor.getInstance());
		subject.add(0);
		subject.add(1);
		subject.add(2);
		verify(consumerCallback, times(1)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.next(), (Integer) 0);
			subject.add(3);
			subject.add(4);
			verify(consumerCallback, times(1)).run();
			Assert.assertEquals(consumer.next(), (Integer) 1);
			Assert.assertEquals(consumer.next(), (Integer) 2);
			Assert.assertEquals(consumer.next(), (Integer) 3);
			Assert.assertEquals(consumer.next(), (Integer) 4);
			Assert.assertEquals(consumer.next(), null);
		}
	}

	@Test
	public void testPending()
	{
		Runnable consumerCallback = mock(Runnable.class);
		SingleConsumerQueue<Integer> subject = new SingleConsumerQueue<>(consumerCallback, DirectExecutor.getInstance());
		subject.add(0);
		subject.add(1);
		subject.add(2);
		verify(consumerCallback, times(1)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.next(), (Integer) 0);
			subject.add(3);
			subject.add(4);
			verify(consumerCallback, times(1)).run();
		}
		verify(consumerCallback, times(2)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.next(), (Integer) 1);
			Assert.assertEquals(consumer.next(), (Integer) 2);
			Assert.assertEquals(consumer.next(), (Integer) 3);
			Assert.assertEquals(consumer.next(), (Integer) 4);
			Assert.assertEquals(consumer.next(), null);
		}
	}

	@Test
	public void testNewlyAdded()
	{
		Runnable consumerCallback = mock(Runnable.class);
		SingleConsumerQueue<Integer> subject = new SingleConsumerQueue<>(consumerCallback, DirectExecutor.getInstance());
		subject.add(0);
		subject.add(1);
		subject.add(2);
		verify(consumerCallback, times(1)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.next(), (Integer) 0);
			Assert.assertEquals(consumer.next(), (Integer) 1);
			Assert.assertEquals(consumer.next(), (Integer) 2);
		}
		subject.add(3);
		verify(consumerCallback, times(2)).run();
		subject.add(4);
		verify(consumerCallback, times(2)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.next(), (Integer) 3);
			Assert.assertEquals(consumer.next(), (Integer) 4);
			Assert.assertEquals(consumer.next(), null);
			verify(consumerCallback, times(2)).run();
		}
		verify(consumerCallback, times(2)).run();
	}

	@Test
	public void testNextOrClose()
	{
		Runnable consumerCallback = mock(Runnable.class);
		SingleConsumerQueue<Integer> subject = new SingleConsumerQueue<>(consumerCallback, DirectExecutor.getInstance());
		subject.add(0);
		subject.add(1);
		verify(consumerCallback, times(1)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.nextOrClose(), (Integer) 0);
			Assert.assertEquals(consumer.nextOrClose(), (Integer) 1);
			subject.add(2);
			Assert.assertEquals(consumer.nextOrClose(), (Integer) 2);
			Assert.assertEquals(consumer.nextOrClose(), null);
			subject.add(3);
			verify(consumerCallback, times(2)).run();
		}
		verify(consumerCallback, times(2)).run();
		subject.add(4);
		verify(consumerCallback, times(2)).run();
		try (SingleConsumerQueue<Integer>.Consumer consumer = subject.consume()) {
			Assert.assertEquals(consumer.nextOrClose(), (Integer) 3);
			Assert.assertEquals(consumer.nextOrClose(), (Integer) 4);
			Assert.assertEquals(consumer.nextOrClose(), null);
		}
	}
}
