package net.dryuf.base.io;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;


public class FilenameComparatorsTest
{
	@Test
	public void slashify_whenNativeSeparator_thenSlash()
	{
		String slashified = FilenameComparators.slashify("a" +File.separatorChar + "b");

		assertEquals(slashified, "a/b");
	}

	@Test
	public void slashifyPath_whenMulti_thenSlashified()
	{

		String slashified = FilenameComparators.slashify(Paths.get("a", "b", "c"));

		assertEquals(slashified, "a/b/c");
	}

	@Test
	public void dirFirstComparator_whenRegularFiles_thenAlphabetically()
	{
		int resultN = FilenameComparators.dirFirstComparator().compare("a", "b");

		assertEquals(resultN, -1);

		int resultP = FilenameComparators.dirFirstComparator().compare("b", "a");

		assertEquals(resultP, 1);
	}

	@Test
	public void dirFirstComparator_whenDirectory_thenDirectoryFirst()
	{
		int resultN = FilenameComparators.dirFirstComparator().compare("a/b", "a.txt");

		assertEquals(resultN, -1);

		int resultP = FilenameComparators.dirFirstComparator().compare("a.txt", "a/b");

		assertEquals(resultP, 1);
	}

	@Test
	public void dirFirstComparator_whenDirectories_thenSubname()
	{
		int resultN = FilenameComparators.dirFirstComparator().compare("a/a", "a/b");

		assertEquals(resultN, -1);

		int resultP = FilenameComparators.dirFirstComparator().compare("a/b", "a/a");

		assertEquals(resultP, 1);
	}

	@Test
	public void dirFirstComparator_whenSame_thenEqual()
	{
		int resultF = FilenameComparators.dirFirstComparator().compare("a", "a");

		assertEquals(resultF, 0);

		int resultS = FilenameComparators.dirFirstComparator().compare("a/b", "a/b");

		assertEquals(resultS, 0);
	}

	@Test
	public void dirFirstComparator_whenShorter_thenLower()
	{
		int resultN = FilenameComparators.dirFirstComparator().compare("a", "ab");

		assertEquals(resultN, -1);

		int resultP = FilenameComparators.dirFirstComparator().compare("ab", "a");

		assertEquals(resultP, 1);
	}


	@Test
	public void dirFirstPathComparator_whenRegularFiles_thenAlphabetically()
	{
		int resultN = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a"), Paths.get("b"));

		assertEquals(resultN, -1);

		int resultP = FilenameComparators.dirFirstPathComparator().compare(Paths.get("b"), Paths.get("a"));

		assertEquals(resultP, 1);
	}

	@Test
	public void dirFirstPathComparator_whenDirectory_thenDirectoryFirst()
	{
		int resultN = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a/b"), Paths.get("a.txt"));

		assertThat(resultN, lessThan(0));

		int resultP = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a.txt"), Paths.get("a/b"));

		assertThat(resultP, greaterThan(0));
	}

	@Test
	public void dirFirstPathComparator_whenDirectories_thenSubname()
	{
		int resultN = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a/a"), Paths.get("a/b"));

		assertEquals(resultN, -1);

		int resultP = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a/b"), Paths.get("a/a"));

		assertEquals(resultP, 1);
	}

	@Test
	public void dirFirstPathComparator_whenSame_thenEqual()
	{
		int resultF = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a"), Paths.get("a"));

		assertEquals(resultF, 0);

		int resultS = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a/b"), Paths.get("a/b"));

		assertEquals(resultS, 0);
	}

	@Test
	public void dirFirstPathComparator_whenShorter_thenLower()
	{
		int resultN = FilenameComparators.dirFirstPathComparator().compare(Paths.get("a"), Paths.get("ab"));

		assertThat(resultN, lessThan(0));

		int resultP = FilenameComparators.dirFirstPathComparator().compare(Paths.get("ab"), Paths.get("a"));

		assertThat(resultP, greaterThan(0));
	}
}
