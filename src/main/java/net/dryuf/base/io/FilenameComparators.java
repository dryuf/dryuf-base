package net.dryuf.base.io;


import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;


/**
 * Various filename comparators.
 */
public class FilenameComparators
{
	private static final Comparator<String> DIR_FIRST_COMPARATOR = new DirFirstComparator();

	private static final Comparator<Path> DIR_FIRST_PATH_COMPARATOR = new DirFirstPathComparator();

	/**
	 * Makes native filename slashified, ie all native file separator will be replaced by '/'
	 *
	 * @param nativeFilename
	 *      native filename
	 *
	 * @return
	 *      slashified filename
	 */
	public static String slashify(Path nativeFilename)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0, l = nativeFilename.getNameCount(); i < l; ++i) {
			sb.append(nativeFilename.subpath(i, i+1));
			sb.append('/');
		}
		return sb.length() == 0? "" : sb.substring(0, sb.length() - 1);
	}

	/**
	 * Makes native filename slashified, ie all native file separator will be replaced by '/'
	 *
	 * @param nativeFilename
	 *      native filename
	 *
	 * @return
	 *      slashified filename
	 */
	public static String slashify(String nativeFilename)
	{
		return nativeFilename.replace(File.separatorChar, '/');
	}

	/**
	 * Comparator putting directories first, comparing slashified strings.
	 *
	 * @return
	 *      comparator putting directories first.
	 */
	public static Comparator<String> dirFirstComparator()
	{
		return DIR_FIRST_COMPARATOR;
	}


	/**
	 * Comparator putting directories first.
	 *
	 * @return
	 *      comparator putting directories first.
	 */
	public static Comparator<Path> dirFirstPathComparator()
	{
		return DIR_FIRST_PATH_COMPARATOR;
	}

	private FilenameComparators()
	{
	}

	private static class DirFirstComparator implements Comparator<String>
	{
		@Override
		public int compare(String o1, String o2)
		{
			for (int i = 0, l = Math.min(o1.length(), o2.length()); i < l; ++i) {
				char c1 = o1.charAt(i);
				char c2 = o2.charAt(i);
				if (c1 == c2) {
					continue;
				}
				if (c1 == '/') {
					return -1;
				}
				else if (c2 == '/') {
					return 1;
				}
				else {
					return Character.compare(c1, c2);
				}
			}
			return Integer.compare(o1.length(), o2.length());
		}
	}

	private static class DirFirstPathComparator implements Comparator<Path>
	{
		@Override
		public int compare(Path o1, Path o2)
		{
			for (int i = 0, l = Math.min(o1.getNameCount(), o2.getNameCount()); i < l; ++i) {
				Path c1 = o1.subpath(i, i+1);
				Path c2 = o2.subpath(i, i+1);
				int r = c1.compareTo(c2);
				if (r != 0) {
					return r;
				}
			}
			return Integer.compare(o1.getNameCount(), o2.getNameCount());
		}
	}
}
