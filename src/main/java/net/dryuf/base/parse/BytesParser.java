package net.dryuf.base.parse;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;


@RequiredArgsConstructor
public class BytesParser
{
	private final byte[] buffer;

	private int pos;

	public int position()
	{
		return pos;
	}

	public void reset(int pos)
	{
		this.pos = pos;
	}

	public boolean hasNext()
	{
		return pos < buffer.length;
	}

	public byte next()
	{
		if (pos >= buffer.length) {
			throw new NoSuchElementException("Iterating behind end: pos=" + pos);
		}
		return buffer[pos++];
	}

	public void skipSpaces()
	{
		while (pos < buffer.length && buffer[pos] != '\n' && Character.isWhitespace((char) (buffer[pos] & 0x7f))) {
			++pos;
		}
	}

	public boolean needSpaces()
	{
		if (pos >= buffer.length || buffer[pos] == '\n' || !Character.isWhitespace((char) (buffer[pos] & 0x7f))) {
			return false;
		}
		skipSpaces();
		return true;
	}

	public boolean matchesWord(byte[] sequence)
	{
		if (pos + sequence.length > buffer.length) {
			return false;
		}
		for (int i = 0; i < sequence.length; ++i) {
			if (buffer[pos + i] != sequence[i]) {
				return false;
			}
		}
		return true;
	}

	public String readLinedUntil(byte delimiter)
	{
		for (int e = pos; ; ++e) {
			if (e >= buffer.length || buffer[e] == '\n' || buffer[e] == delimiter) {
				String result = new String(buffer, pos, e, StandardCharsets.UTF_8);
				pos = e;
				return result;
			}
		}
	}

	public boolean readUntilEol()
	{
		for (int e = pos; ; ++e) {
			if (e >= buffer.length) {
				pos = e;
				return false;
			}
			else if (buffer[e] == '\n') {
				pos = e+1;
				return true;
			}
		}
	}
}
