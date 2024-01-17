package net.dryuf.base.collection;

import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;


public class CountingTreeMapTest
{
	@Test
	public void insert_singleRightRotateWithoutChildren_keptBalanced()
	{
		int[] items = new int[]{ 10, 5, 3 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_singleRightRotateWithChildren_keptBalanced()
	{
		int[] items = new int[]{ 10, 5, 15, 3, 7, 1 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_doubleRightRotateWithoutChildren_keptBalanced()
	{
		int[] items = new int[]{ 10, 5, 7 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_doubleRightRotateWithChildrenMr_keptBalanced()
	{
		int[] items = new int[]{ 10, 5, 15, 3, 7, 8 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_doubleRightRotateWithChildrenMl_keptBalanced()
	{
		int[] items = new int[]{ 10, 5, 15, 3, 7, 6 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_singleLeftRotateWithoutChildren_keptBalanced()
	{
		int[] items = new int[]{ -10, -5, -3 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_singleLeftRotateWithChildren_keptBalanced()
	{
		int[] items = new int[]{ -10, -5, -15, -3, -7, -1 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_doubleLeftRotateWithoutChildren_keptBalanced()
	{
		int[] items = new int[]{ -10, -5, -7 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_doubleLeftRotateWithChildrenMl_keptBalanced()
	{
		int[] items = new int[]{ -10, -5, -15, -3, -7, -8 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_doubleLeftRotateWithChildrenMr_keptBalanced()
	{
		int[] items = new int[]{ -10, -5, -15, -3, -7, -6 };
		TreeCountingMap<Integer, Integer> tree = buildTree(items);
	}

	@Test
	public void insert_random_keptBalanced()
	{
		int[] items = new int[]{ 1, 5, 8, 10, 4, 20, 3, 9, 30, 64, 40, 2, 9, 60, 50, 55, 7, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i*2);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertEquals(tree.get(i), i*2);
		}
	}

	@Test
	public void insert_right_keptBalanced()
	{
		int[] items = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i*2);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertEquals(tree.get(i), i*2);
		}
	}

	@Test
	public void insert_left_keptBalanced()
	{
		int[] items = new int[]{ -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16, -17, -18, -19, -20 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i*2);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertEquals(tree.get(i), i*2);
		}
	}
	@Test
	public void insertAndRemove_rightRight_keptBalanced()
	{
		int[] items = new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertEquals(tree.remove(i), i);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertNull(tree.get(i));
		}
	}

	@Test
	public void insertAndRemove_rightLeft_keptBalanced()
	{
		Integer[] items = new Integer[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i);
			checkConsistency(tree);
		}
		for (Integer i: Arrays.asList(items).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
			assertEquals(tree.remove(i), i);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertNull(tree.get(i));
		}
	}


	@Test
	public void insertAndRemove_leftLeft_keptBalanced()
	{
		int[] items = new int[]{ -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16, -17, -18, -19, -20 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertEquals(tree.remove(i), i);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertNull(tree.get(i));
		}
	}

	@Test
	public void insertAndRemove_leftRight_keptBalanced()
	{
		Integer[] items = new Integer[]{ -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16, -17, -18, -19, -20 };
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: items) {
			tree.put(i, i);
			checkConsistency(tree);
		}
		for (Integer i: Arrays.asList(items).stream().sorted().collect(Collectors.toList())) {
			assertEquals(tree.remove(i), i);
			checkConsistency(tree);
		}
		for (int i: items) {
			assertNull(tree.get(i));
		}
	}

	@Test
	public void get_empty_returnNull()
	{
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		assertEquals(tree.get(0), null);
	}

	@Test
	public void get_nonExisting_returnNull()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(1, 2, 3);
		assertEquals(tree.get(4), null);
	}

	@Test
	public void remove_empty_returnNull()
	{
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		assertEquals(tree.remove(0), null);
	}

	@Test
	public void remove_nonExisting_returnNull()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(1, 2, 3);
		assertEquals(tree.remove(4), null);
	}

	@Test
	public void remove_rootWithEmptyChildren_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(10);
		assertEquals(tree.remove(10), 20);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithLeftChild_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(10, 5);
		assertEquals(tree.remove(10), 20);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithRightChild_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(10, 15);
		assertEquals(tree.remove(10), 20);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithChildren_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(10, 5, 15);
		assertEquals(tree.remove(10), 20);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithChildrenMr_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(10, 5, 15, 7);
		assertEquals(tree.remove(10), 20);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithChildrenMl_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(10, 5, 15, 13);
		assertEquals(tree.remove(10), 20);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithDeepTwoChildrenLeft_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                       25,
			/**/     3
		);
		assertEquals(tree.remove(20), 40);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithDeepThreeChildrenRight_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                 20,
			/**/          10,                               30,
			/**/     5,          15,                     25,          37,
			/**/                     17,           23
		);
		assertEquals(tree.remove(20), 40);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithDeeperLeftChildrenLeft_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                     25,      35,
			/**/      3,  7,       14,  17,                     26, 34,  37,
			/**/       4,         13,  16, 19,                     33,
			/**/                          18
		);
		assertEquals(tree.remove(20), 40);
		checkConsistency(tree);
	}

	@Test
	public void remove_rootWithDeeperRightChildrenRight_remove()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                     25,      35,
			/**/      4,  7,       14,                     23,  26, 34,  37,
			/**/            8,                           21, 24, 27,    36,
			/**/                                          22
		);
		assertEquals(tree.remove(20), 40);
		checkConsistency(tree);
	}

	@Test
	public void countAny_whenEmpty_returnZero()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree();
		assertEquals(tree.lowerCount(20), 0L);
		assertEquals(tree.floorCount(19), 0L);
		assertEquals(tree.ceilingCount(10), 0L);
		assertEquals(tree.higherCount(11), 0L);
	}

	@Test
	public void lowerCount_any_countBefore()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                     25,      35,
			/**/      4,  7,       14,                     23,  26, 34,  37,
			/**/            8,                           21, 24, 27,    36,
			/**/                                          22
		);
		assertEquals(tree.lowerCount(20), 7L);
		assertEquals(tree.lowerCount(19), 7L);
		assertEquals(tree.lowerCount(10), 4L);
		assertEquals(tree.lowerCount(11), 5L);
		assertEquals(tree.lowerCount(25), 12L);
		assertEquals(tree.lowerCount(35), 17L);
	}

	@Test
	public void floorCount_any_countTill()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                     25,      35,
			/**/      4,  7,       14,                     23,  26, 34,  37,
			/**/            8,                           21, 24, 27,    36,
			/**/                                          22
		);
		assertEquals(tree.floorCount(20), 8L);
		assertEquals(tree.floorCount(19), 7L);
		assertEquals(tree.floorCount(10), 5L);
		assertEquals(tree.floorCount(11), 5L);
		assertEquals(tree.floorCount(25), 13L);
		assertEquals(tree.floorCount(35), 18L);
	}

	@Test
	public void ceilingCount_any_countFrom()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                     25,      35,
			/**/      4,  7,       14,                     23,  26, 34,  37,
			/**/            8,                           21, 24, 27,    36,
			/**/                                          22
		);
		assertEquals(tree.ceilingCount(20), 13L);
		assertEquals(tree.ceilingCount(19), 13L);
		assertEquals(tree.ceilingCount(10), 16L);
		assertEquals(tree.ceilingCount(11), 15L);
		assertEquals(tree.ceilingCount(25), 8L);
		assertEquals(tree.ceilingCount(35), 3L);
	}

	@Test
	public void higherCount_any_countAfter()
	{
		TreeCountingMap<Integer, Integer> tree = buildTree(
			/**/                      20,
			/**/              10,                                30,
			/**/        5,           15,                     25,      35,
			/**/      4,  7,       14,                     23,  26, 34,  37,
			/**/            8,                           21, 24, 27,    36,
			/**/                                          22
		);
		assertEquals(tree.higherCount(20), 12L);
		assertEquals(tree.higherCount(19), 13L);
		assertEquals(tree.higherCount(10), 15L);
		assertEquals(tree.higherCount(11), 15L);
		assertEquals(tree.higherCount(25), 7L);
		assertEquals(tree.higherCount(35), 2L);
	}

	@Test
	public void isEmpty_whenEmpty_returnEmpty()
	{
		TreeCountingMap<Integer, Integer> map = new TreeCountingMap<>();

		assertTrue(map.isEmpty());
	}

	@Test
	public void isEmpty_whenFull_returnNotEmpty()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(1);

		assertFalse(map.isEmpty());
	}

	@Test
	public void containsKey_whenNotExists_returnNotFound()
	{
		TreeCountingMap<Integer, Integer> map = new TreeCountingMap<>();

		assertFalse(map.containsKey(4));
	}

	@Test
	public void containsKey_whenExists_returnFound()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);

		assertTrue(map.containsKey(4));
	}

	@Test
	public void firstKey_whenEmpty_throwException()
	{
		TreeCountingMap<Integer, Integer> map = buildTree();

		expectThrows(NoSuchElementException.class, () -> map.firstKey());
	}

	@Test
	public void firstKey_whenNotEmpty_returnFirst()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);

		assertEquals(map.firstKey(), 4);
	}

	@Test
	public void lastKey_whenEmpty_throwException()
	{
		TreeCountingMap<Integer, Integer> map = buildTree();

		expectThrows(NoSuchElementException.class, () -> map.lastKey());
	}

	@Test
	public void lastKey_whenNotEmpty_returnLast()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);

		assertEquals(map.lastKey(), 6);
	}

	@Test
	public void clear_whenNotEmpty_makeEmpty()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);

		map.clear();

		assertTrue(map.isEmpty());
	}

	@Test
	public void entrySet_size_whenAny_returnSize()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		assertEquals(es.size(), 3);
	}

	@Test
	public void entrySet_contains_whenNoKey_returnFalse()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		assertEquals(es.contains(new AbstractMap.SimpleImmutableEntry<>(3, 0)), false);
	}

	@Test
	public void entrySet_contains_whenWrongValue_returnFalse()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		assertEquals(es.contains(new AbstractMap.SimpleImmutableEntry<>(4, 0)), false);
	}

	@Test
	public void entrySet_contains_whenCorrect_returnTrue()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		assertEquals(es.contains(new AbstractMap.SimpleImmutableEntry<>(4, 8)), true);
	}

	@Test
	public void entrySet_add_whenNew_addToMap()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		es.add(new AbstractMap.SimpleImmutableEntry<>(8, 9));

		assertEquals(map.get(8), 9);
	}

	@Test
	public void entrySet_remove_whenExists_removeFromMap()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		es.remove(new AbstractMap.SimpleImmutableEntry<>(5, 10));

		assertEquals(map.get(5), null);
	}

	@Test
	public void entrySet_clear_whenFull_makeEmpty()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4, 5, 6);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		es.clear();

		assertEquals(map.size(), 0);
	}

	@Test
	public void entrySet_iterator_whenEmpty_iterateEmpty()
	{
		TreeCountingMap<Integer, Integer> map = buildTree();
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		assertFalse(es.iterator().hasNext());
	}

	@Test
	public void entrySet_iterator_whenConcurrentModification_throwException()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(4);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		Iterator<Map.Entry<Integer, Integer>> iterator = es.iterator();
		map.put(5, 6);

		expectThrows(ConcurrentModificationException.class, () -> iterator.hasNext());
	}

	@Test
	public void entrySet_iterator_whenNonEmpty_iterate()
	{
		TreeCountingMap<Integer, Integer> map = buildTree(6, 5, 4, 7, 8, 9, 3, 2, 1);
		Set<Map.Entry<Integer, Integer>> es = map.entrySet();

		Set<Integer> keys = map.keySet();

		assertEquals(keys, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
	}

	private static <K, V> void checkConsistency(TreeCountingMap<K, V> tree)
	{
		assertTrue(tree.root == null || tree.root.parent == null);
		assertEquals(tree.sizeBig(), Optional.ofNullable(tree.root).map(TreeCountingMap.Node::getCount).orElse(0L));
		assertEquals((long) tree.size(), Optional.ofNullable(tree.root).map(TreeCountingMap.Node::getCount).orElse(0L));
		checkConsistency(tree.root);
	}

	private static <K, V> void checkConsistency(TreeCountingMap.Node<K, V> node)
	{
		if (node == null) {
			return;
		}

		checkConsistency(node.left);
		checkConsistency(node.right);

		assertTrue(node.left == null || node.left.parent == node);
		assertTrue(node.right == null || node.right.parent == node);

		assertEquals(
			node.getCount(),
			Optional.ofNullable(node.left).map(TreeCountingMap.Node::getCount).orElse(0L) +
			Optional.ofNullable(node.right).map(TreeCountingMap.Node::getCount).orElse(0L) +
			1L
		);
		assertEquals(
			node.getHeight(),
			Math.max(
				Optional.ofNullable(node.left).map(TreeCountingMap.Node::getHeight).orElse((byte) 0),
				Optional.ofNullable(node.right).map(TreeCountingMap.Node::getHeight).orElse((byte) 0)
			) + 1
		);
		assertEquals(
			node.getBalance(),
			Optional.ofNullable(node.right).map(TreeCountingMap.Node::getHeight).orElse((byte) 0) -
				Optional.ofNullable(node.left).map(TreeCountingMap.Node::getHeight).orElse((byte) 0)
		);

		assertTrue(node.getBalance() >= -1);
		assertTrue(node.getBalance() <= +1);
	}

	private TreeCountingMap<Integer, Integer> buildTree(int... numbers)
	{
		TreeCountingMap<Integer, Integer> tree = new TreeCountingMap<>();
		for (int i: numbers) {
			tree.put(i, i*2);
		}
		return tree;
	}
}
