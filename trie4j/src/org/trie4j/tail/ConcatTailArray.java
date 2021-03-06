package org.trie4j.tail;

import org.trie4j.tail.builder.ConcatTailBuilder;
import org.trie4j.tail.index.ArrayTailIndex;

public class ConcatTailArray extends AbstractTailArray implements TailArray{
	public ConcatTailArray(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	protected TailBuilder newTailBuilder(StringBuilder tails) {
		return new ConcatTailBuilder(tails);
	}

	@Override
	protected TailIndex newTailIndex(int initialCapacity) {
		return new ArrayTailIndex(initialCapacity);
	}
}
