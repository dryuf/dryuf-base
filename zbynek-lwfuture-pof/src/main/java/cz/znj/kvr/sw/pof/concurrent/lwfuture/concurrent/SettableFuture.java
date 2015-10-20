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

package cz.znj.kvr.sw.pof.concurrent.lwfuture.concurrent;


/**
 * Asynchronous Future that can be finished externally.
 *
 * @param <V>
 *      future result type
 */
public class SettableFuture<V> extends AbstractFuture<V>
{
	public boolean                  set(V result)
	{
		return super.set(result);
	}

	public boolean                  setException(Throwable ex)
	{
		return super.setException(ex);
	}
}
