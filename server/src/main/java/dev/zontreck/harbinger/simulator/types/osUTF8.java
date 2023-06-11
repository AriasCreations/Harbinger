package dev.zontreck.harbinger.simulator.types;

import com.google.common.base.Charsets;
import dev.zontreck.harbinger.utils.SimUtils;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class osUTF8 implements Comparable
{
	public static final osUTF8 Empty = new osUTF8();
	byte[] data;
	int len;

	public osUTF8(){
		this(0);
	}

	public osUTF8(int capacity){
		this(new byte[capacity]);
	}

	public osUTF8(byte[] source)
	{
		this(source, source.length);
	}

	public osUTF8(byte[] source, int len){
		data=source;
		this.len=len;
	}

	public osUTF8(osUTF8 source){
		data=source.ToArray();
		len=source.Length();
	}

	public osUTF8(String source){
		this(source, -1);
	}

	public osUTF8(String source, int max){
		if(max==-1)max=source.length();
		data = SimUtils.StringToBytesNoTerm(source,max);
		len = data.length;
	}

	public byte getIndex(int i) {
		if(i >= len)
			i = len - 1;
		if(i < 0)
			i = 0;
		else if(i >= data.length)
			i = data.length-1;

		return data[i];
	}

	public void setIndex(int i, byte dat)
	{
		if(i > 0 && i < len)
			data[i] = dat;
	}

	public int Length()
	{
		return len;
	}

	public int Capacity()
	{
		return data.length;
	}

	public int GetHashCode()
	{
		int hash = len;
		for(int i = 0; i < Capacity(); ++i)
		{
			hash += data[i];
			hash <<= 3;
			hash += hash >> 26;
		}

		return hash & 0x7fffffff;
	}

	@Override
	public String toString()
	{
		if(len == 0)
			return "";

		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
			return false;

		if(obj instanceof osUTF8)
			return equals((osUTF8)obj);

		if(obj instanceof byte[])
			return equals(new osUTF8((byte[])obj));

		return false;
	}

	public boolean equals(osUTF8 str)
	{
		if(str == null || len != str.len)
		{
			return false;
		}

		byte[] other = str.data;
		for (int i = 0; i < other.length; i++) {
			if(getIndex(i) != other[i])
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public int compareTo(@NotNull Object o) {
		boolean eq = equals(o);
		if(eq)return 0;
		else return 1;
	}

	public void Clear()
	{
		len=0;
	}

	public byte[] ToArray()
	{
		return Arrays.copyOf(data, len);
	}

	public void CheckCapacity(int needed)
	{
		int need = needed + Length();
		int cur = Capacity();
		if(need > cur)
		{
			cur *= 2;

			if(need < cur)
				need=cur;

			if(need > 0x7FFFFFC7)
				need = 0x7FFFFFC7;

			data = Arrays.copyOf(data, need);
		}
	}

	public void AppendASCII(char c)
	{
		AppendByte((byte)c);
	}

	public void AppendByte(byte b){
		CheckCapacity(1);
		data[len] = b;
		len++;
	}

	public void AppendASCII(String str) {
		byte[] arr = SimUtils.osUTF8GetBytes(str, str.length());
		Append(arr);
	}
	public void Append(byte[] arr){
		for (byte b :
				arr) {
			AppendByte(b);
		}
	}

	public void Append(osUTF8 utf)
	{
		Append(utf.ToArray());
	}

	public static byte[] GetASCIIBytes(String str)
	{
		return str.getBytes(Charsets.US_ASCII);
	}

	public void AppendInt(int v)
	{
		Append(SimUtils.IntToByteString(v));
	}

}
