package org.trie4j.patricia.tail;

import org.trie4j.AbstractWikipediaTest;
import org.trie4j.Trie;
import org.trie4j.tail.builder.SuffixTrieTailBuilder;

public class TailPatriciaTrieWithSuffixTrieTailBuilderWikipediaTest extends AbstractWikipediaTest {
	@Override
	protected Trie createFirstTrie() {
		return new TailPatriciaTrie(new SuffixTrieTailBuilder());
	}
}
