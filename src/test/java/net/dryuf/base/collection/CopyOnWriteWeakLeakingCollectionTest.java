package net.dryuf.base.collection;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.testng.annotations.Test;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;


@Log
public class CopyOnWriteWeakLeakingCollectionTest
{
	@Test(timeOut = 10_000L)
	public void iterate_whenReferencing_provideItem()
	{
		Runnable leakage = mock(Runnable.class);
		Collection<Double> collection = new CopyOnWriteWeakLeakingCollection<>(leakage);

		Double d = new Double(5.123456789);
		collection.add(d);

		ArrayList<Double> copy = new ArrayList<>(collection);

		assertEquals(copy, Arrays.asList(d));
		verify(leakage, times(0)).run();
	}

	@Test(timeOut = 10_000L)
	public void iterate_whenGarbageCollected_removed()
	{
		Runnable leakage = mock(Runnable.class);
		Collection<Double> collection = new CopyOnWriteWeakLeakingCollection<>(leakage);

		Double d = new Double(5.123456789);
		WeakReference<Double> wd = new WeakReference<>(d);
		collection.add(d);

		d = null;
		for (;;) {
			System.gc();
			ArrayList<Double> copy = new ArrayList<>(collection);
			if (copy.isEmpty()) {
				break;
			}
		}
		verify(leakage, times(1)).run();
	}

	@Test(timeOut = 10_000L)
	public void iterate_whenGarbageCollectedComplex_removed()
	{
		Runnable leakage = mock(Runnable.class);
		Collection<Double> collection = new CopyOnWriteWeakLeakingCollection<>(leakage);

		Double d0 = new Double(0.123456789);
		Double d1 = new Double(1.123456789);
		Double d2 = new Double(2.123456789);
		collection.add(d0);
		collection.add(d1);
		collection.add(d2);

		d1 = null;
		for (;;) {
			System.gc();
			List<Double> copy = collection.stream().collect(Collectors.toList());
			if (copy.size() == 2) {
				assertEquals(copy, Arrays.asList(d0, d2));
				break;
			}
		}
		verify(leakage, times(1)).run();
	}

	// Do not remove, serves as javadoc example:

	private void example()
	{
		Collection<Connection> connections =
			new CopyOnWriteWeakLeakingCollection<Connection>(this::leakDetector);
	}

	private void leakDetector()
	{
		log.severe("Unreleased connection detected in: " + this);
	}
}
