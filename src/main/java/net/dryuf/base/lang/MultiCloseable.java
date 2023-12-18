package net.dryuf.base.lang;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * {@link AutoCloseable} implementation wrapping multiple objects.
 *
 * Note that the implementation has voluntary support for checked exception, they must be checked by developer.
 */
public class MultiCloseable<X extends Exception> implements AutoCloseable
{
	private final ArrayList<AutoCloseable> stored;

	/**
	 * Creates empty MultiCloseable.
	 */
	public MultiCloseable()
	{
		this.stored = new ArrayList<>();
	}

	/**
	 * Creates MultiCloseable, initiating from already existing list.  Automatically closes the objects when
	 * initialization fails.
	 *
	 * @param objects
	 *      objects to be closed
	 *
	 * @throws X
	 *      if closing the objects fail
	 */
	public MultiCloseable(List<AutoCloseable> objects) throws X
	{
		try {
			this.stored = new ArrayList<>(objects);
		}
		catch (Throwable ex) {
			try {
				closeList(objects);
			}
			catch (Throwable more) {
				ex.addSuppressed(more);
			}
			throw ex;
		}
	}

	/**
	 * Creates MultiCloseable, initiating from already existing list.  Automatically closes the objects when
	 * initialization fails.
	 *
	 * @param objects
	 *      objects to be closed
	 *
	 * @throws X
	 *      if closing the objects fail
	 */
	public MultiCloseable(AutoCloseable... objects) throws X
	{
		try {
			this.stored = new ArrayList<>(Arrays.asList(objects));
		}
		catch (Throwable ex) {
			try {
				closeArray(objects);
			}
			catch (Throwable more) {
				ex.addSuppressed(more);
			}
			throw ex;
		}
	}

	/**
	 * Adds object to to-be-closed objects.  Automatically closes resource if it cannot be added.
	 *
	 * @param object
	 * 		object to close
	 * @param <T>
	 *     	type of added object
	 */
	@SneakyThrows
	public <T extends AutoCloseable> void add(T object) throws X
	{
		boolean added = false;
		try {
			stored.add(object);
			added = true;
		}
		finally {
			if (!added) {
				object.close();
			}
		}
	}

	/**
	 * Closes this object and all registered AutoCloseable objects.  They are closed in reverse order.  It closes all
	 * objects, adding exception to suppressed if there are more failures.
	 *
	 * @throws X
	 *      if registered object throws exception.
	 */
	@Override
	public void close() throws X
	{
		closeList(this.stored);
	}

	@SneakyThrows
	public void closeList(List<AutoCloseable> objects) throws X
	{
		Throwable first = null;
		for (int i = objects.size(); --i >= 0; ) {
			try {
				objects.get(i).close();
			}
			catch (Throwable ex) {
				if (first == null) {
					first = ex;
				}
				else {
					first.addSuppressed(ex);
				}
			}
		}
		if (first != null) {
			throw first;
		}
	}

	@SneakyThrows
	public void closeArray(AutoCloseable... objects) throws X
	{
		Throwable first = null;
		for (int i = objects.length; --i >= 0; ) {
			try {
				objects[i].close();
			}
			catch (Throwable ex) {
				if (first == null) {
					first = ex;
				}
				else {
					first.addSuppressed(ex);
				}
			}
		}
		if (first != null) {
			throw first;
		}
	}
}
