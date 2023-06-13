/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package dev.zontreck.harbinger.thirdparty.libomv.types;

import java.nio.ByteBuffer;

import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

public class PacketHeader
{
	// This header flag signals that ACKs are appended to the packet
	public static final byte MSG_APPENDED_ACKS = 0x10;

	// This header flag signals that this packet has been sent before
	public static final byte MSG_RESENT = 0x20;

	// This header flags signals that an ACK is expected for this packet
	public static final byte MSG_RELIABLE = 0x40;

	// This header flag signals that the message is compressed using zerocoding
	public static final byte MSG_ZEROCODED = (byte) 0x80;

	public byte[] Data;
	public byte[] Extra;
	private final byte fixedLen = 6;
	private byte frequency;
	private byte length;

	public byte getFlags()
	{
		return this.Data[0];
	}

	public void setFlags(final byte value)
	{
		this.Data[0] = value;
	}

	public boolean getReliable()
	{
		return 0 != (Data[0] & MSG_RELIABLE);
	}

	public void setReliable(final boolean value)
	{
		if (value)
		{
			this.Data[0] |= PacketHeader.MSG_RELIABLE;
		}
		else
		{
			this.Data[0] -= PacketHeader.MSG_RELIABLE;
		}
	}

	public boolean getResent()
	{
		return 0 != (Data[0] & MSG_RESENT);
	}

	public void setResent(final boolean value)
	{
		if (value)
		{
			this.Data[0] |= PacketHeader.MSG_RESENT;
		}
		else
		{
			this.Data[0] -= PacketHeader.MSG_RESENT;
		}
	}

	public boolean getZerocoded()
	{
		return 0 != (Data[0] & MSG_ZEROCODED);
	}

	public void setZerocoded(final boolean value)
	{
		if (value)
		{
			this.Data[0] |= PacketHeader.MSG_ZEROCODED;
		}
		else
		{
			this.Data[0] -= PacketHeader.MSG_ZEROCODED;
		}
	}

	public boolean getAppendedAcks()
	{
		return 0 != (Data[0] & MSG_APPENDED_ACKS);
	}

	public void setAppendedAcks(final boolean value)
	{
		if (value)
		{
			this.Data[0] |= PacketHeader.MSG_APPENDED_ACKS;
		}
		else
		{
			this.Data[0] -= PacketHeader.MSG_APPENDED_ACKS;
		}
	}

	public int getSequence()
	{
		return (((this.Data[1] & 0xff) >> 24) + ((this.Data[2] & 0xff) << 16) + ((this.Data[3] & 0xff) << 8) + ((this.Data[4] & 0xff) << 0));
	}

	public int getExtraLength()
	{
		return this.Data[5];
	}

	public short getID()
	{
		switch (this.frequency)
		{
			case PacketFrequency.Low:
				return (short)(((this.Data[8 + this.getExtraLength()] & 0xFF) << 8) + ((this.Data[9 + this.getExtraLength()] & 0xff) << 0));
			case PacketFrequency.Medium:
				return this.Data[7];
			case PacketFrequency.High:
				return this.Data[6];
			default:
				break;
		}
		return 0;
	}

	public void setID(final int value)
	{
		switch (this.frequency)
		{
			case PacketFrequency.Low:
				this.Data[8 + this.getExtraLength()] = (byte) ((value >> 8) & 0xFF);
				this.Data[9 + this.getExtraLength()] = (byte) ((value >> 0) & 0xFF);
				break;
			case PacketFrequency.Medium:
				this.Data[7] = (byte) (value & 0xFF);
				break;
			case PacketFrequency.High:
				this.Data[6] = (byte) (value & 0xFF);
				break;
			default:
				break;
		}
	}

	public byte getFrequency()
	{
		return this.frequency;
	}

	private void setFrequency(final byte frequency)
	{
		this.frequency = frequency;
		switch (frequency)
		{
			case PacketFrequency.Low:
				length = 10;
				break;
			case PacketFrequency.Medium:
				length = 8;
				break;
			case PacketFrequency.High:
				length = 7;
				break;
			default:
				break;
		}

	}

	private void BuildHeader(final ByteBuffer bytes) throws Exception
	{
		if (bytes.limit() < length)
		{
			throw new Exception("Not enough bytes for " + PacketFrequency.Names[this.frequency] + "Header");
		}
		this.Data = new byte[length];
		bytes.get(this.Data, 0, this.fixedLen);
		final int extra = this.getExtraLength();
		if (0 < extra)
		{
			this.Extra = new byte[extra];
			bytes.get(this.Extra, 0, extra);
		}
		bytes.get(this.Data, this.fixedLen, length - this.fixedLen);
	}

	// Constructors
	public PacketHeader(final byte frequency)
	{
		this.setFrequency(frequency);
		this.Data = new byte[length];
		this.Data[5] = 0;
		switch (frequency)
		{
			case PacketFrequency.Low:
				this.Data[7] = (byte) 0xFF;
			case PacketFrequency.Medium:
				this.Data[6] = (byte) 0xFF;
			default:
				break;
		}
	}

	public PacketHeader(final ByteBuffer bytes, final byte frequency) throws Exception
	{
		this.setFrequency(frequency);
		this.BuildHeader(bytes);
		this.CreateAckList(bytes);
	}

	public PacketHeader(final ByteBuffer bytes) throws Exception
	{
		if ((byte) 0xFF == bytes.get(6))
		{
			if ((byte) 0xFF == bytes.get(7))
			{
				this.setFrequency(PacketFrequency.Low);
			}
			else
			{
				this.setFrequency(PacketFrequency.Medium);
			}
		}
		else
		{
			this.setFrequency(PacketFrequency.High);
		}
		this.BuildHeader(bytes);
		this.CreateAckList(bytes);
	}

	public byte getLength()
	{
		return this.length;
	}

	public void ToBytes(final ByteBuffer bytes)
	{
		bytes.put(this.Data, 0, this.fixedLen - 1);
		if (null == Extra)
		{
			bytes.put((byte) 0);
		}
		else
		{
			bytes.put((byte) (this.Extra.length & 0xFF));
			bytes.put(this.Extra);
		}
		bytes.put(this.Data, this.fixedLen, length - this.fixedLen);
	}

	/**
	 * Encode a byte array with zerocoding. Used to compress packets marked with
	 * the zerocoded flag. Any zeroes in the array are compressed down to a
	 * single zero byte followed by a count of how many zeroes to expand out. A
	 * single zero becomes 0x00 0x01, two zeroes becomes 0x00 0x02, three zeroes
	 * becomes 0x00 0x03, etc. The first four bytes are copied directly to the
	 * output buffer.
	 * 
	 * @param src
	 *            The byte buffer to encode
	 * @param dest
	 *            The output byte array to encode to
	 * @return The length of the output buffer
	 */
	public static int zeroEncode(final ByteBuffer src, final byte[] dest)
	{
		final int bodylen;
		int zerolen = 6 + src.get(5);
		byte zerocount = 0;
		final int srclen = src.position();

		src.position(0);
		src.get(dest, 0, zerolen);

		if (0 == (src.get(0) & MSG_APPENDED_ACKS))
		{
			bodylen = srclen;
		}
		else
		{
			bodylen = srclen - src.get(srclen - 1) * 4 - 1;
		}

		int i;
		for (i = zerolen; i < bodylen; i++)
		{
			if (0x00 == src.get(i))
			{
				zerocount++;

				if (0 == zerocount)
				{
					dest[zerolen] = 0x00;
					zerolen++;
					dest[zerolen] = (byte) 0xff;
					zerolen++;
					zerocount++;
				}
			}
			else
			{
				if (0 != zerocount)
				{
					dest[zerolen] = 0x00;
					zerolen++;
					dest[zerolen] = zerocount;
					zerolen++;
					zerocount = 0;
				}
				dest[zerolen] = src.get(i);
				zerolen++;
			}
		}

		if (0 != zerocount)
		{
			dest[zerolen] = 0x00;
			zerolen++;
			dest[zerolen] = zerocount;
			zerolen++;
		}
		// copy appended ACKs
		for (; i < srclen; i++)
		{
			dest[zerolen] = src.get(i);
			zerolen++;
		}
		return zerolen;
	}

	public int[] AckList;

	private void CreateAckList(final ByteBuffer bytes)
	{
		if (this.getAppendedAcks())
		{
			int packetEnd = bytes.limit() - 1;
			this.AckList = new int[bytes.get(packetEnd)];
			final byte[] array = bytes.array();

			for (int i = this.AckList.length; 0 < i;)
			{
				packetEnd -= 4;
				--i;
				this.AckList[i] = (int) Helpers.BytesToUInt32B(array, packetEnd);
			}
			bytes.limit(packetEnd);
		}
	}
}
