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

import org.testng.annotations.Test;


/**
 * Tests for {@link DefaultFutureListener} class.
 *
 * @author
 * Copyright 2015-2022 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
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
