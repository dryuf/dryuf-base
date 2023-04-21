package net.dryuf.base.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;


/**
 * Additional collectors.
 */
public class ExtCollectors
{
	/**
	 * Sorting collector, with stable order.
	 *
	 * @param comparator
	 *      comparator to use for sorting
	 *
	 * @return
	 *      Collector performing stable sort and returning {@link Stream}
	 *
	 * @param <T>
	 *      type of collected data
	 */
	public static <T> Collector<T, ArrayList<T>, Stream<T>> toStableSorted(Comparator<T> comparator)
	{
		return new Collector<T, ArrayList<T>, Stream<T>>()
		{
			@Override
			public Supplier<ArrayList<T>> supplier()
			{
				return ArrayList::new;
			}

			@Override
			public BiConsumer<ArrayList<T>, T> accumulator()
			{
				return ArrayList::add;
			}

			@Override
			public BinaryOperator<ArrayList<T>> combiner()
			{
				return (a, b) -> {
					a.addAll(b);
					return a;
				};
			}

			@Override
			public Function<ArrayList<T>, Stream<T>> finisher()
			{
				return (l) -> {
					l.sort(comparator);
					return l.stream();
				};
			}

			@Override
			public Set<Characteristics> characteristics()
			{
				return Collections.emptySet();
			}
		};
	}
}
