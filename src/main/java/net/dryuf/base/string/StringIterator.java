package net.dryuf.base.string;

import java.util.NoSuchElementException;


/**
 * String Iterator allowing moving forward and back.
 */
public class StringIterator
{
	private final CharSequence input;

	private final int offset;

	private final int end;

	private int position;

	/**
	 * Creates new instance based on offset and end.
	 *
	 * @param input
	 *      input
	 * @param offset
	 *      start of substring
	 * @param end
	 *      end of substring
	 *
	 * @return
	 *      the iterator
	 */
	public static StringIterator fromOffsetEnd(CharSequence input, int offset, int end)
	{
		return new StringIterator(input, offset, end);
	}

	/**
	 * Creates new instance based on offset and length.
	 *
	 * @param input
	 *      input
	 * @param offset
	 *      start of substring
	 * @param length
	 *      length of substring
	 *
	 * @return
	 *      the iterator
	 */
	public static StringIterator fromOffsetLength(CharSequence input, int offset, int length)
	{
		return new StringIterator(input, offset, offset + length);
	}

	/**
	 * Creates new instance based on full input.
	 *
	 * @param input
	 *      input
	 *
	 * @return
	 *      the iterator
	 */
	public static StringIterator fromFull(CharSequence input)
	{
		return new StringIterator(input);
	}

	/**
	 * Creates new instance from full input.
	 *
	 * @param input
	 *      input
	 */
	protected StringIterator(CharSequence input)
	{
		this(input, 0, input.length());
	}

	protected StringIterator(CharSequence input, int offset, int end)
	{
		if (offset < 0) {
			throw new IllegalArgumentException("offset must be positive: offset=" + offset);
		} else if (end < offset || end > input.length()) {
			throw new IllegalArgumentException("end not within input boundaries: offset=" + offset + " end= " + end + " input.length=" + input.length());
		}
		this.input = input;
		this.offset = offset;
		this.end = end;

		this.position = this.offset;
	}

	/**
	 * Checks whether there is next character.
	 *
	 * @return
	 *      true if there next character, false otherwise.
	 */
	public boolean hasNext()
	{
		return position < end;
	}

	/**
	 * Returns next character from string.
	 *
	 * @return
	 *      next character
	 *
	 * @throws NoSuchElementException
	 *      if behind the iterating sequence
	 */
	public char next() throws NoSuchElementException
	{
		if (position >= end) {
			throw new NoSuchElementException("Iterating behind end: position=" + position);
		}
		return input.charAt(position++);
	}

	/**
	 * Moves back one position.
	 *
	 * @throws NoSuchElementException
	 *      if before the iterating sequence
	 */
	public void back() throws NoSuchElementException
	{
		if (position <= offset) {
			throw new NoSuchElementException("Iterating before beginning: position=" + position);
		}
		--position;
	}
}
