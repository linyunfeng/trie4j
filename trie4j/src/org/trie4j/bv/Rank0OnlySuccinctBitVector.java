/*
 * Copyright 2012 Takao Nakaguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trie4j.bv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Rank0OnlySuccinctBitVector implements Serializable, BitVector{
	public Rank0OnlySuccinctBitVector(){
		this(16);
	}

	public Rank0OnlySuccinctBitVector(int initialCapacity){
		vector = new byte[initialCapacity / 8 + 1];
		int blockSize = CACHE_WIDTH;
		int size = initialCapacity / blockSize + 1;
		countCache0 = new int[size];
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		int n = Math.min(size, 32);
		for(int i = 0; i < n; i++){
			b.append((vector[(i / 8)] & (0x80 >> (i % 8))) != 0 ? "1" : "0");
		}
		return b.toString();
	}

	public boolean isZero(int index){
		return (vector[index / 8] & BITS[index % 8]) == 0;
	}

	public int size(){
		return this.size;
	}

	public void trimToSize(){
		int vectorSize = size / 8 + 1;
		vector = Arrays.copyOf(vector, Math.min(vector.length, vectorSize));
		int blockSize = CACHE_WIDTH / 8;
		int size = vectorSize / blockSize + (((vectorSize % blockSize) != 0) ? 1 : 0);
		int countCacheSize0 = size;
		countCache0 = Arrays.copyOf(countCache0, Math.min(countCache0.length, countCacheSize0));
	}

	public void append1(){
		int i = size / 8;
		int ci = size / CACHE_WIDTH;
		if(i >= vector.length){
			extend();
		}
		if(size % CACHE_WIDTH == 0 && ci > 0){
			countCache0[ci] = countCache0[ci - 1];
		}
		int r = size % 8;
		vector[i] |= BITS[r];
		size++;
	}

	public void append0(){
		int i = size / 8;
		int ci = size / CACHE_WIDTH;
		if(i >= vector.length){
			extend();
		}
		if(size % CACHE_WIDTH == 0 && ci > 0){
			countCache0[ci] = countCache0[ci - 1];
		}
//		int r = size % 8;
//		vector[i] &= ~BITS[r];
		countCache0[ci]++;
		size++;
	}

	public int rank0(int pos){
		int ret = 0;
		int cn = pos / CACHE_WIDTH;
		if(cn > 0){
			ret = countCache0[cn - 1];
		}
		int n = pos / 8;
		for(int i = (cn * (CACHE_WIDTH / 8)); i < n; i++){
			ret += BITCOUNTS0[vector[i] & 0xff];
		}
		ret += BITCOUNTS0[vector[n] & MASKS[pos % 8]] - 7 + (pos % 8);
		return ret;
	}

	public void save(OutputStream os) throws IOException{
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(size);
		trimToSize();
		dos.writeInt(vector.length);
		dos.write(vector);
		dos.writeInt(countCache0.length);
		for(int e : countCache0){
			dos.writeInt(e);
		}
		dos.flush();
	}

	public void load(InputStream is) throws IOException{
		DataInputStream dis = new DataInputStream(is);
		size = dis.readInt();
		int vectorSize = dis.readInt();
		vector = new byte[vectorSize];
		dis.read(vector, 0, vectorSize);
		int size = dis.readInt();
		countCache0 = new int[size];
		for(int i = 0; i < size; i++){
			countCache0[i] = dis.readInt();
		}
	}

	private void writeObject(ObjectOutputStream s)
	throws IOException {
		trimToSize();
		ObjectOutputStream.PutField fields = s.putFields();
		fields.put("size", size);
		trimToSize();
		fields.put("vector", vector);
		fields.put("countCache0", countCache0);
		s.writeFields();
    }

	private void readObject(ObjectInputStream s)
	throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField fields = s.readFields();
		size = fields.get("size", 0);
		vector = (byte[])fields.get("vector", null);
		countCache0 = (int[])fields.get("countCache0", null);
    }

	private void extend(){
		int vectorSize = (int)(vector.length * 1.2) + 1;
		vector = Arrays.copyOf(vector, vectorSize);
		int blockSize = CACHE_WIDTH / 8;
		int size = vectorSize / blockSize + (((vectorSize % blockSize) != 0) ? 1 : 0);
		countCache0 = Arrays.copyOf(countCache0, size);
	}

	private static final int CACHE_WIDTH = 64;
	private byte[] vector;
	private int size;
	private int[] countCache0;

	private static final int[] MASKS = {
		0x80, 0xc0, 0xe0, 0xf0
		, 0xf8, 0xfc, 0xfe, 0xff
	};
	private static final byte[] BITS = {
		(byte)0x80, (byte)0x40, (byte)0x20, (byte)0x10
		, (byte)0x08, (byte)0x04, (byte)0x02, (byte)0x01
	};
	private static final byte[] BITCOUNTS0 = {
		8, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 4, 
		7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
		7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
		6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
		7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
		6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
		6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
		5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
		7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
		6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
		6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
		5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
		6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
		5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
		5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
		4, 3, 3, 2, 3, 2, 2, 1, 3, 2, 2, 1, 2, 1, 1, 0, 
	};

	private static final long serialVersionUID = -7658605229245494623L;
}
