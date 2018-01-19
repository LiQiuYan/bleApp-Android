package com.healthme.common.util;

import java.io.OutputStream;
public class BitOutput {

	private OutputStream out;
	private int value;
	private int next = 7;

	public BitOutput(OutputStream out) {
		this.out = out;
	}

	public void writeBitOne() throws Exception {
		write(1);
	}

	public void writeBitZero() throws Exception {
		write(0);
	}

	private void write(int bit) throws Exception {
		value = value | (bit << next);
		next--;

		if (next < 0) {
			out.write(value);
			value = 0;
			next = 7;
		}
	}

	public void writeBits(String bits) throws Exception {
		for (int i = 0; i < bits.length(); i++) {
			char v = bits.charAt(i);
			if (v == '0' || v == '1') {
				write(v - '0');
				continue;
			}
			throw new RuntimeException();
		}
	}

	public void writeBits(int[] bits) throws Exception {
		for (int bit : bits) {
			if (bit == 0 || bit == 1) {
				write(bit);
				continue;
			}
			throw new RuntimeException();
		}
	}

	public void close() throws Exception {
		if (next == 7) {
			return;
		}
		out.write(value);
	}
}