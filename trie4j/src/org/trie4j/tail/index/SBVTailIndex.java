package org.trie4j.tail.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.tail.TailIndex;

public class SBVTailIndex implements TailIndex{
	public SBVTailIndex() {
		bv = new BytesSuccinctBitVector();
	}

	public SBVTailIndex(int initialCapacity) {
		bv = new BytesSuccinctBitVector(initialCapacity);
	}

	public BytesSuccinctBitVector getSBV(){
		return bv;
	}

	@Override
	public void add(int start, int end) {
		for(int i = start; i < end; i++){
			bv.append1();
		}
		bv.append0();
	}
	
	@Override
	public void addEmpty() {
		bv.append0();
	}

	@Override
	public int get(int nodeId) {
		if(nodeId == 0){
			if(bv.isZero(0)) return -1;
			else return 0;
		}
		int s = bv.select0(nodeId);
		if(bv.isZero(s + 1)) return -1;
		return bv.rank1(s);
	}

	@Override
	public void trimToSize() {
		bv.trimToSize();
	}

	@Override
	public void load(InputStream is) throws IOException {
		bv.load(is);
	}

	@Override
	public void save(OutputStream os) throws IOException {
		bv.save(os);
	}

	private BytesSuccinctBitVector bv;
}
