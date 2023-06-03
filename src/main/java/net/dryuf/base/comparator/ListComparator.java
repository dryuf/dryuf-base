/*
 * Copyright 2015-2023 Zbynek Vyskovsky mailto:kvr000@gmail.com http://github.com/kvr000/ https://github.com/dryuf/ https://www.linkedin.com/in/zbynek-vyskovsky/
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

package net.dryuf.base.comparator;

import java.util.Comparator;
import java.util.List;


/**
 * Comparator comparing lists.
 *
 * @param <L>
 *      type of list
 * @param <X>
 *      type of element
 */
public class ListComparator<L extends List<? extends X>, X> implements Comparator<L>
{
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final ListComparator NATURAL = new ListComparator(Comparator.naturalOrder());

	private final Comparator<? super X> elementComparator;

	/**
	 * Returns Comparator comparing lists in lexicographical order based on natural order of items.
	 *
	 * @return
	 *       Comparator comparing lists in lexicographical order based on natural order of items.
	 *
	 * @param <L>
	 *       type of list
	 * @param <X>
	 *       type of item
	 */
	@SuppressWarnings("unchecked")
	public static <L extends List<X>, X extends Comparable<X>> ListComparator<L, X> natural()
	{
		return NATURAL;
	}

	/**
	 * Returns Comparator comparing lists in lexicographical order based on provided Comparator for items.
	 *
	 * @return
	 *       Comparator comparing lists in lexicographical order based on provided Comparator for items.
	 *
	 * @param <L>
	 *       type of list
	 * @param <X>
	 *       type of item
	 */
	public static <L extends List<X>, S, X extends S> ListComparator<L, X> of(Comparator<? super X> elementComparator)
	{
		return new ListComparator<>(elementComparator);
	}

	protected ListComparator(Comparator<? super X> elementComparator)
	{
		this.elementComparator = elementComparator;
	}

	@Override
	public int compare(L a, L b)
	{
		int size = Math.min(a.size(), b.size());
		for (int i = 0; i < size; i++) {
			int result = elementComparator.compare(a.get(i), b.get(i));
			if (result != 0) {
				return result;
			}
		}
		return Integer.compare(a.size(), b.size());
	}
}
