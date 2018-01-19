package com.healthme.common.util;

import java.io.InputStream;

public class BitInput {

	private InputStream input;
	private int value;
	private int next = 7;

	public BitInput(InputStream input) {
		this.input = input;
	}

	public int readBit() throws Exception {

		if (next == 7) {
			value = input.read();
			if (value == -1) {
				return -1;
			}
		}

		int result = (value & (1 << next)) >>> next;
		next--;

		if (next == -1) {
			next = 7;
		}

		return result;
	}


	public int[] readBits(int size) throws Exception {
		int[] result = new int[size];
		for (int i = 0; i < size; i++) {
			int v = readBit();
			if (v == -1) {
				throw new RuntimeException("End of inputstream");
			}
			result[i] = v;
		}
		return result;
	}
}