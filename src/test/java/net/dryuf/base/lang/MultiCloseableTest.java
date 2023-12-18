package net.dryuf.base.lang;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;


public class MultiCloseableTest {
	@Test
	public void close_whenSuccess_doNothing() {
		try (MultiCloseable<RuntimeException> multi = new MultiCloseable<>(Arrays.asList(mock(AutoCloseable.class)))) {
		}
	}

	@Test
	public void close_whenOneError_doFirst() throws Exception {
		AutoCloseable erroring = mock(AutoCloseable.class);
		doThrow(new IOException()).when(erroring).close();

		Throwable error = expectThrows(IOException.class, () -> {
			try (MultiCloseable<RuntimeException> multi = new MultiCloseable<>(Arrays.asList(erroring))) {
			}
		});

		assertThat(error, instanceOf(IOException.class));
		assertEquals(error.getSuppressed().length, 0);
	}

	@Test
	public void close_whenMultiError_doFirstAndSuppress() throws Exception {
		AutoCloseable erroring1 = mock(AutoCloseable.class);
		doThrow(new NumberFormatException()).when(erroring1).close();
		AutoCloseable erroring2 = mock(AutoCloseable.class);
		doThrow(new IOException()).when(erroring2).close();

		Throwable error = expectThrows(IOException.class, () -> {
			try (MultiCloseable<RuntimeException> multi = new MultiCloseable<>(erroring1, erroring2)) {
			}
		});

		assertThat(error, instanceOf(IOException.class));
		assertEquals(error.getSuppressed().length, 1);
		assertThat(error.getSuppressed()[0], instanceOf(NumberFormatException.class));
	}
}
