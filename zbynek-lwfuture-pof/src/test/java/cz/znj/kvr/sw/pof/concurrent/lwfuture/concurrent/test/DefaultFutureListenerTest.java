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

import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.DefaultFutureListener;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.Futures;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.ListenableFuture;
import cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent.SettableFuture;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


/**
 * Tests for {@link DefaultFutureListener} class.
 *
 * @author
 * 	Zbynek Vyskovsky, mailto:kvr@centrum.cz http://kvr.znj.cz/software/java/ListenableFuture/ http://github.com/kvr000
 */
public class DefaultFutureListenerTest
{
	@Test
	public void                     testSuccess()
	{
		SettableFuture<Void> f = new SettableFuture<Void>();
		f.addListener(new DefaultFutureListener<Void>());
		f.set(null);
	}

	@Test
	public void                     testFailure()
	{
		SettableFuture<Void> f = new SettableFuture<Void>();
		f.addListener(new DefaultFutureListener<Void>());
		f.setException(new TestingRuntimeException());
	}

	@Test
	public void                     testCancel()
	{
		SettableFuture<Void> f = new SettableFuture<Void>();
		f.addListener(new DefaultFutureListener<Void>());
		f.cancel(true);
	}
}
