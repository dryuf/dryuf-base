/*
 * Copyright 2015 Zbynek Vyskovsky http://kvr.znj.cz/ http://github.com/kvr000/
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
import org.junit.Test;

public class AbstractFutureListenerTest
{
	@Test
	public void                     testListenerSetException()
	{
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new NullPointerException();
			}
		});
		future.set(null);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new NullPointerException();
			}
		});
	}

	@Test
	public void                     testListenerExceptedException()
	{
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new NullPointerException();
			}
		});
		future.setException(new UnsupportedOperationException());
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new NullPointerException();
			}
		});
	}

	@Test
	public void                     testListenerCancelledException()
	{
		SettableFuture<Void> future = new SettableFuture<Void>();
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new NullPointerException();
			}
		});
		future.cancel(true);
		future.addListener(new Runnable() {
			@Override
			public void run() {
				throw new NullPointerException();
			}
		});
	}
}
