package dev.zontreck.harbinger.simulator.types;

import com.google.common.base.Charsets;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class osUTF8 implements Comparable {
	public static final osUTF8 Empty = new osUTF8();
	byte[] data;
	int len;

	public osUTF8() {
		this(0);
	}

	public osUTF8(final int capacity) {
		this(new byte[capacity]);
	}

	public osUTF8(final byte[] source) {
		this(source, source.length);
	}

	public osUTF8(final byte[] source, final int len) {
		this.data = source;
		this.len = len;
	}

	public osUTF8(final osUTF8 source) {
		this.data = source.ToArray();
		this.len = source.Length();
	}

	public osUTF8(final String source) {
		this(source, -1);
	}

	public osUTF8(final String source, int max) {
		if (-1 == max) max = source.length();
		this.data = SimUtils.StringToBytesNoTerm(source, max);
		this.len = this.data.length;
	}

	public byte getIndex(int i) {
		if (i >= this.len)
			i = this.len - 1;
		if (0 > i)
			i = 0;
		else if (i >= this.data.length)
			i = this.data.length - 1;

		return this.data[i];
	}

	public void setIndex(final int i, final byte dat) {
		if (0 < i && i < this.len)
			this.data[i] = dat;
	}

	public int Length() {
		return this.len;
	}

	public int Capacity() {
		return this.data.length;
	}

	public int GetHashCode() {
		int hash = this.len;
		for (int i = 0; i < this.Capacity(); ++i) {
			hash += this.data[i];
			hash <<= 3;
			hash += hash >> 26;
		}

		return hash & 0x7fffffff;
	}

	@Override
	public String toString() {
		if (0 == len)
			return "";

		return new String(this.data, StandardCharsets.UTF_8);
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj)
			return false;

		if (obj instanceof osUTF8)
			return this.equals((osUTF8) obj);

		if (obj instanceof byte[])
			return this.equals(new osUTF8((byte[]) obj));

		return false;
	}

	public boolean equals(final osUTF8 str) {
		if (null == str || this.len != str.len) {
			return false;
		}

		final byte[] other = str.data;
		for (int i = 0; i < other.length; i++) {
			if (this.getIndex(i) != other[i]) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int compareTo(@NotNull final Object o) {
		final boolean eq = this.equals(o);
		if (eq) return 0;
		else return 1;
	}

	public void Clear() {
		this.len = 0;
	}

	public byte[] ToArray() {
		return Arrays.copyOf(this.data, this.len);
	}

	public void CheckCapacity(final int needed) {
		int need = needed + this.Length();
		int cur = this.Capacity();
		if (need > cur) {
			cur *= 2;

			if (need < cur)
				need = cur;

			if (0x7FFFFFC7 < need)
				need = 0x7FFFFFC7;

			this.data = Arrays.copyOf(this.data, need);
		}
	}

	public void AppendASCII(final char c) {
		this.AppendByte((byte) c);
	}

	public void AppendByte(final byte b) {
		this.CheckCapacity(1);
		this.data[this.len] = b;
		this.len++;
	}

	public void AppendASCII(final String str) {
		final byte[] arr = SimUtils.osUTF8GetBytes(str, str.length());
		this.Append(arr);
	}

	public void Append(final byte[] arr) {
		for (final byte b :
				arr) {
			this.AppendByte(b);
		}
	}

	public void Append(final osUTF8 utf) {
		this.Append(utf.ToArray());
	}

	public static byte[] GetASCIIBytes(final String str) {
		return str.getBytes(Charsets.US_ASCII);
	}

	public void AppendInt(final int v) {
		this.Append(SimUtils.IntToByteString(v));
	}

}
