package org.trie4j.louds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.AbstractTrieTest;
import org.trie4j.Node;
import org.trie4j.Trie;
import org.trie4j.patricia.simple.PatriciaTrie;

public class TailLOUDSPPTrieWithConcatTailBuilderTest extends AbstractTrieTest{
	@Override
	protected Trie buildSecondTrie(Trie firstTrie) {
		return new TailLOUDSPPTrie(firstTrie);
	}

	@Test
	public void test() throws Exception{
		String[] words = {"こんにちは", "さようなら", "おはよう", "おおきなかぶ", "おおやまざき"};
		Trie trie = new PatriciaTrie();
		for(String w : words) trie.insert(w);
		
//		final SBVTailIndex ti = new SBVTailIndex();
		TailLOUDSPPTrie lt = new TailLOUDSPPTrie(trie);
//		System.out.println(lt.getBvTree());
//		System.out.println(ti.getSBV());
//		Algorithms.dump(lt.getRoot(), new OutputStreamWriter(System.out));
		for(String w : words){
			Assert.assertTrue(w, lt.contains(w));
		}
		Assert.assertFalse(lt.contains("おやすみなさい"));

		StringBuilder b = new StringBuilder();
		Node[] children = lt.getRoot().getChildren();
		for(Node n : children){
			char[] letters = n.getLetters();
			b.append(letters[0]);
		}
		Assert.assertEquals("おこさ", b.toString());
	}

	@Test
	public void test_save_load() throws Exception{
		String[] words = {"こんにちは", "さようなら", "おはよう", "おおきなかぶ", "おおやまざき"};
		Trie trie = new PatriciaTrie();
		for(String w : words) trie.insert(w);
		TailLOUDSPPTrie lt = new TailLOUDSPPTrie(trie);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		lt.save(baos);
		lt = new TailLOUDSPPTrie();
		lt.load(new ByteArrayInputStream(baos.toByteArray()));
		for(String w : words){
			Assert.assertTrue(lt.contains(w));
		}
		Assert.assertFalse(lt.contains("おやすみなさい"));

		StringBuilder b = new StringBuilder();
		Node[] children = lt.getRoot().getChildren();
		for(Node n : children){
			char[] letters = n.getLetters();
			b.append(letters[0]);
		}
		Assert.assertEquals("おこさ", b.toString());
	}
}
