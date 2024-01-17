package net.dryuf.base.collection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;


/**
 * Implementation of {@link CountingMap} based on AVL-tree.
 *
 * Counting operations have log2(n) complexity.
 *
 * Submap operations (subMap, headMap, tailMap) are currently not supported.
 *
 * @param <K>
 * @param <V>
 */
public class TreeCountingMap<K, V> extends AbstractMap<K, V> implements CountingMap<K, V>
{
	@Getter
	@Accessors(fluent = true)
	private final Comparator<K> comparator;

	Node<K, V> root;

	int version;

	@SuppressWarnings("unchecked")
	public TreeCountingMap()
	{
		this((Comparator<K>) Comparator.naturalOrder());
	}

	public TreeCountingMap(Comparator<K> comparator)
	{
		this.comparator = comparator;
	}

	@Override
	public int size()
	{
		long size = sizeBig();
		return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
	}

	@Override
	public boolean isEmpty()
	{
		return root == null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key)
	{
		return Optional.ofNullable(findParentNode((K) key))
			.map(n -> n.balance == 0)
			.isPresent();
	}

	@Override
	public long sizeBig()
	{
		return Optional.ofNullable(root)
			.map(Node::getCount)
			.orElse(0L);
	}

	@Override
	public V get(Object key)
	{
		@SuppressWarnings("unchecked")
		FindResult<K, V> found = findParentNode((K) key);
		if (found == null || found.balance != 0) {
			return null;
		}
		return found.node.value;
	}

	@Override
	public V put(K key, V value)
	{
		FindResult<K, V> result = findParentNode(key);
		if (result == null) {
			root = new Node<>(null, key, value);
			++version;
		}
		else {
			Node<K, V> parent = result.node;
			if (result.balance < 0) {
				Node<K, V> node = new Node<K, V>(parent, key, value);
				(parent.left = node).parent = parent;
				propagateCount(parent);
				++version;
			}
			else if (result.balance > 0) {
				Node<K, V> node = new Node<K, V>(parent, key, value);
				(parent.right = node).parent = parent;
				propagateCount(parent);
				++version;
			}
			else {
				V old = parent.value;
				parent.value = value;
				return old;
			}
		}
		return null;
	}

	@Override
	public V remove(Object key)
	{
		@SuppressWarnings("unchecked")
		FindResult<K, V> found = findParentNode((K) key);
		if (found == null || found.balance != 0) {
			return null;
		}
		removeEntry(found.node);
		return found.node.value;
	}

	@Override
	public void clear()
	{
		root = null;
	}

	private void removeEntry(Map.Entry<K, V> entry)
	{
		Node<K, V> node = (Node<K, V>) entry;

		Node<K, V> l = node.left, r = node.right, p = node.parent;
		if (l == null && r == null) {
			replaceParentChild(node.parent, node, null);
			propagateCount(node.parent);
		}
		else if (l == null) {
			r.parent = p;
			replaceParentChild(p, node, r);
			propagateCount(p);
		}
		else if (r == null) {
			l.parent = p;
			replaceParentChild(p, node, l);
			propagateCount(p);
		}
		else if (l.right == null && (r.left != null || node.getBalance() < 0)) {
			l.parent = p;
			if ((l.right = node.right) != null) {
				l.right.parent = l;
			}
			replaceParentChild(p, node, l);
			propagateCount(l);
		}
		else if (r.left == null) {
			r.parent = p;
			if ((r.left = node.left) != null) {
				r.left.parent = r;
			}
			replaceParentChild(p, node, r);
			propagateCount(r);
		}
		else {
			// let find moving candidate, it is not direct child of node:
			l = l.right; r = r.left;
			for (;;) {
				Node<K, V> nl = l.right, nr = r.left;
				if (nl != null && nr != null) {
					l = nl; r = nr;
				}
				else if (nr == null && (nl != null || node.getBalance() < 0)) {
					if (nl != null) {
						for (l = nl; (nl = l.right) != null; ) {
							l = nl;
						}
					}
					Node<K, V> lparent = l.parent;
					if ((lparent.right = l.left) != null) {
						l.left.parent = lparent;
					}
					if ((l.left = node.left) != null) {
						l.left.parent = l;
					}
					if ((l.right = node.right) != null) {
						l.right.parent = l;
					}
					l.parent = node.parent;
					replaceParentChild(node.parent, node, l);
					propagateCount(lparent);
					break;
				}
				else /* nl == null && (nr != null || node.getBalance() >= 0 */ {
					if (nr != null) {
						for (r = nr; (nr = r.left) != null; ) {
							r = nr;
						}
					}
					Node<K, V> rparent = r.parent;
					if ((rparent.left = r.right) != null) {
						r.right.parent = rparent;
					}
					if ((r.right = node.right) != null) {
						r.right.parent = r;
					}
					if ((r.left = node.left) != null) {
						r.left.parent = r;
					}
					r.parent = node.parent;
					replaceParentChild(node.parent, node, r);
					propagateCount(rparent);
					break;
				}
			}
		}
		++version;
	}

	@Override
	public long lowerCount(K key)
	{
		//                         T
		//                  UL            [st]
		//             [st]     K
		//                     L  R
		FindResult<K, V> result = findParentNode(key);
		if (result == null) {
			return 0L;
		}
		Node<K, V> node = result.node;
		long count = Node.getCountNullsafe(node.left) + (result.balance > 0 ? 1 : 0);
		for (Node<K, V> previous = node, parent = node.parent; parent != null; parent = (previous = parent).parent) {
			if (parent.right == previous) {
				count += Node.getCountNullsafe(parent.left) + 1L;
			}
		}
		return count;
	}

	@Override
	public long floorCount(K key)
	{
		//                         T
		//                  UL            [st]
		//             [st]     K
		//                     L  R
		FindResult<K, V> result = findParentNode(key);
		if (result == null) {
			return 0L;
		}
		Node<K, V> node = result.node;
		long count = Node.getCountNullsafe(node.left) + (result.balance >= 0 ? 1 : 0);
		for (Node<K, V> previous = node, parent = node.parent; parent != null; parent = (previous = parent).parent) {
			if (parent.right == previous) {
				count += Node.getCountNullsafe(parent.left) + 1L;
			}
		}
		return count;
	}

	@Override
	public long ceilingCount(K key)
	{
		//                         T
		//                [st]               UR
		//                              K          [st]
		//                            L   R
		FindResult<K, V> result = findParentNode(key);
		if (result == null) {
			return 0L;
		}
		Node<K, V> node = result.node;
		long count = Node.getCountNullsafe(node.right) + (result.balance <= 0 ? 1 : 0);
		for (Node<K, V> previous = node, parent = node.parent; parent != null; parent = (previous = parent).parent) {
			if (parent.left == previous) {
				count += Node.getCountNullsafe(parent.right) + 1L;
			}
		}
		return count;
	}

	@Override
	public long higherCount(K key)
	{
		//                         T
		//                [st]               UR
		//                              K          [st]
		//                            L   R
		// Count: UL - K + L
		FindResult<K, V> result = findParentNode(key);
		if (result == null) {
			return 0L;
		}
		Node<K, V> node = result.node;
		long count = Node.getCountNullsafe(node.right) + (result.balance < 0 ? 1 : 0);
		for (Node<K, V> previous = node, parent = node.parent; parent != null; parent = (previous = parent).parent) {
			if (parent.left == previous) {
				count += Node.getCountNullsafe(parent.right) + 1L;
			}
		}
		return count;
	}

	private FindResult<K, V> findParentNode(K key)
	{
		if (root == null) {
			return null;
		}
		for (Node<K, V> current = root; ; ) {
			int compare = comparator().compare(key, current.key);
			if (compare < 0) {
				if (current.left == null) {
					return FindResult.of((byte) -1, current);
				}
				current = current.left;
			}
			else if (compare > 0) {
				if (current.right == null) {
					return FindResult.of((byte) 1, current);
				}
				current = current.right;
			}
			else {
				return FindResult.of((byte) 0, current);
			}
		}
	}

	private void propagateCount(Node<K, V> node)
	{
		for (Node<K, V> current = node; current != null; ) {
			byte balance = current.updateStatsFromChildren();
			if (balance < -1) {
				if (current.left.getBalance() <= 0) {
					// single right rotation
					//                P                        L
					//          L          [R]            AL        P
					//      AL    [AR]                  [smtg]  [AR]  [R]
					//    [smtg]
					Node<K, V> parent = current.parent, l = current.left, r = current.right, ar = l.right;
					(l.right = current).parent = l;
					if ((current.left = ar) != null) {
						ar.parent = current;
					}
					l.parent = parent;
					replaceParentChild(parent, current, l);
					current.updateStatsFromChildren();
					l.updateStatsFromChildren();
					current = parent;
				}
				else {
					// double right rotation
					//                P                        AR
					//          L          [R]            L           P
					//     [AL]    AR                 [AL]  [ML]  [MR]  [R]
					//          [ML] [MR]
					Node<K, V> parent = current.parent, l = current.left, r = current.right, ar = l.right, ml = ar.left, mr = ar.right;
					if ((l.right = ml) != null) {
						ml.parent = l;
					}
					if ((current.left = mr) != null) {
						mr.parent = current;
					}
					(ar.left = l).parent = ar;
					(ar.right = current).parent = ar;
					ar.parent = parent;
					replaceParentChild(parent, current, ar);
					l.updateStatsFromChildren();
					current.updateStatsFromChildren();
					ar.updateStatsFromChildren();
					current = parent;
				}
			}
			else if (balance > 1) {
				if (current.right.getBalance() >= 0) {
					// single left rotation
					//                P                           R
					//          [L]           R            P            AR
					//                    [AL]    AR    [L]  [AL]     [smtg]
					//                          [smtg]
					Node<K, V> parent = current.parent, l = current.left, r = current.right, al = r.left;
					(r.left = current).parent = r;
					if ((current.right = al) != null) {
						al.parent = current;
					}
					r.parent = parent;
					replaceParentChild(parent, current, r);
					current.updateStatsFromChildren();
					r.updateStatsFromChildren();
					current = parent;
				}
				else {
					// double left rotation
					//                 P                             AL
					//         [L]            R                P            R
					//                    AL     [AR]      [L]  [ML]    [MR]  [AR]
					//                [ML] [MR]
					Node<K, V> parent = current.parent, l = current.left, r = current.right, al = r.left, ml = al.left, mr = al.right;
					if ((r.left = mr) != null) {
						mr.parent = r;
					}
					if ((current.right = ml) != null) {
						ml.parent = current;
					}
					(al.right = r).parent = al;
					(al.left = current).parent = al;
					al.parent = parent;
					replaceParentChild(parent, current, al);
					r.updateStatsFromChildren();
					current.updateStatsFromChildren();
					al.updateStatsFromChildren();
					current = parent;
				}
			}
			else {
				current = current.parent;
			}
		}
	}

	byte replaceParentChild(Node<K, V> parent, Node<K, V> old, Node<K, V> child)
	{
		if (parent == null) {
			assert root == old;
			root = child;
			return 0;
		}
		else if (parent.left == old) {
			parent.left = child;
			return -1;
		}
		else if (parent.right == old) {
			parent.right = child;
			return +1;
		}
		else {
			assert false;
			throw new IllegalStateException("parent non-null but child neither left nor right");
		}
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey)
	{
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SortedMap<K, V> headMap(K toKey)
	{
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey)
	{
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public K firstKey()
	{
		if (root == null) {
			throw new NoSuchElementException("map is empty");
		}
		for (Node<K, V> left = root; ; left = left.left) {
			if (left.left == null) {
				return left.getKey();
			}
		}
	}

	@Override
	public K lastKey()
	{
		if (root == null) {
			throw new NoSuchElementException("map is empty");
		}
		for (Node<K, V> right = root; ; right = right.right) {
			if (right.right == null) {
				return right.getKey();
			}
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return new EntrySetView();
	}

	class EntrySetView extends AbstractSet<Entry<K, V>>
	{
		@Override
		public int size()
		{
			return TreeCountingMap.this.size();
		}

		@Override
		public boolean contains(Object o)
		{
			// Let fail with ClassCastException
//			if (!(o instanceof Map.Entry)) {
//				throw new IllegalArgumentException("");
//			}
			@SuppressWarnings("unchecked")
			Entry<K, V> entry = (Entry<K, V>) o;
			V v = TreeCountingMap.this.get(((Entry<?, ?>) o).getKey());
			return Objects.equals(v, entry.getValue());
		}

		@Override
		public boolean add(Entry<K, V> kvEntry)
		{
			return put(kvEntry.getKey(), kvEntry.getValue()) == null;
		}

		@Override
		public boolean remove(Object o)
		{
			// Let fail with ClassCastException
//			if (!(o instanceof Map.Entry)) {
//				return false;
//			}
			@SuppressWarnings("unchecked")
			Entry<K, V> entry = (Entry<K, V>) o;
			return TreeCountingMap.this.remove(entry.getKey(), entry.getValue());
		}

		@Override
		public void clear()
		{
			TreeCountingMap.this.clear();
		}

		@Override
		public Iterator<Entry<K, V>> iterator()
		{
			return new Iterator<Entry<K, V>>()
			{
				final int snapshotVersion = TreeCountingMap.this.version;

				Node<K, V> next;

				{
					if (root != null) {
						for (next = root; next.left != null; ) {
							next = next.left;
						}
					}
				}

				@Override
				public boolean hasNext()
				{
					if (snapshotVersion != TreeCountingMap.this.version) {
						throw new ConcurrentModificationException();
					}
					return next != null;
				}

				@Override
				public Entry<K, V> next()
				{
					Node<K, V> ret = next;
					if (next.right != null) {
						for (next = next.right; next.left != null; ) {
							next = next.left;
						}
					}
					else {
						for (;;) {
							next = next.parent;
							if (next == null) {
								break;
							}
							if (next.left == ret) {
								break;
							}
							else {
								continue;
							}
						}
					}
					return ret;
				}
			};
		}
	}

	@RequiredArgsConstructor(staticName = "of")
	static class FindResult<K, V>
	{
		final byte balance;

		final Node<K, V> node;
	}

	@EqualsAndHashCode
	static class Node<K, V> implements Map.Entry<K, V>
	{
		static final byte HEIGHT_POSITION = Long.SIZE-8;
		static final long COUNT_MASK = (1<<HEIGHT_POSITION)-1;
		static final long HEIGHT_MASK = Long.MAX_VALUE&~COUNT_MASK;

		@Getter
		K key;

		@Getter
		V value;

		Node<K, V> parent, left, right;

		private long countAndHeight;

		public Node(Node<K, V> parent, K key, V value)
		{
			this.key = key;
			this.value = value;
			setCount(1);
			setHeight((byte) 1);
		}

		public long getCount()
		{
			return countAndHeight&COUNT_MASK;
		}

		public byte getHeight()
		{
			return (byte) (countAndHeight>>HEIGHT_POSITION);
		}

		public void setHeight(byte height)
		{
			countAndHeight = (countAndHeight&COUNT_MASK) | ((long) height<<HEIGHT_POSITION);
		}

		public void setCount(long count)
		{
			this.countAndHeight = (this.countAndHeight&~COUNT_MASK) + count;
		}

		public byte getBalance()
		{
			return (byte) (Optional.ofNullable(right).map(Node::getHeight).orElse((byte) 0) -
				Optional.ofNullable(left).map(Node::getHeight).orElse((byte) 0));
		}

		public byte updateStatsFromChildren()
		{
			long leftCh = left == null ? 0 : left.countAndHeight;
			long rightCh = right == null ? 0 : right.countAndHeight;
			countAndHeight = ((leftCh + rightCh)&COUNT_MASK) +
				(Math.max(leftCh, rightCh)&HEIGHT_MASK) + ((1L<<HEIGHT_POSITION) + 1);
			return (byte) ((rightCh>>HEIGHT_POSITION) - (leftCh>>HEIGHT_POSITION));
		}

		static <K, V> long getCountNullsafe(Node<K, V> node)
		{
			return Optional.ofNullable(node).map(Node::getCount).orElse(0L);
		}

		@Override
		public V setValue(V value)
		{
			V old = this.value;
			this.value = value;
			return old;
		}

	}
}
