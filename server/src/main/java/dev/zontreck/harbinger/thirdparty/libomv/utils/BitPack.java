/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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
package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import dev.zontreck.harbinger.thirdparty.libomv.types.Color4;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;

public class BitPack
{
    public byte[] Data;

    public int getBytePos()
    {
        if (0 != bytePos && 0 == bitPos)
            return this.bytePos - 1;
		return this.bytePos;
    }

    public int getBitPos()
    {
    	return this.bitPos;
    }

    private final int MAX_BITS = 8;
    private static final byte[] ON = { 1 };
    private static final byte[] OFF = { 0 };

    private int bytePos;
    private int bitPos;


    /**
     * Default constructor, initialize the bit packer / bit unpacker
     * with a byte array and starting position
     *
     * @param data Byte array to pack bits in to or unpack from
     */
    public BitPack(final byte[] data)
    {
		this.Data = data;
		this.bytePos = 0;
    }

    /**
     * Default constructor, initialize the bit packer / bit unpacker
     * with a byte array and starting position
     *
     * @param data Byte array to pack bits in to or unpack from
     * @param pos Starting position in the byte array
     */
    public BitPack(final byte[] data, final int pos)
    {
		this.Data = data;
		this.bytePos = pos;
    }

    public byte[] getData()
    {
    	final byte[] dest = new byte[this.getBytePos()];
    	System.arraycopy(this.Data, 0, dest, 0, this.getBytePos());
    	return dest;
    }
    
    /**
     * Pack a number of bits from a byte array in to the data
     *
     * @param data byte array to pack
     */
    public void PackBits(final byte[] data, final int count)
    {
		this.PackBitArray(data, count);
    }

    /**
     * Pack a number of bits from an integer in to the data
     *
     * @param data integer to pack
     */
    public void PackBits(final int data, final int count)
    {
		this.PackBitArray(Helpers.UInt32ToBytesL(data), count);
    }

    /**
     * Pack a floating point value in to the data
     *
     * @param data Floating point value to pack
     */
    public void PackFloat(final float data)
    {
		this.PackBitArray(Helpers.FloatToBytesL(data), 32);
    }

    /**
     * Pack a single bit in to the data
     *
     * @bit Bit to pack
     */
    public void PackBit(final boolean bit)
    {
        if (bit)
			this.PackBitArray(BitPack.ON, 1);
        else
			this.PackBitArray(BitPack.OFF, 1);
    }

    /**
     * Pack a fixed floating point in to the data
     * 
     * @param data
     * @param isSigned
     * @param intBits
     * @param fracBits
     */
    public void PackFixed(final float data, final boolean isSigned, final int intBits, final int fracBits)
    {
        int totalBits = intBits + fracBits;
        if (isSigned) totalBits++;
        final byte[] dest = new byte[(totalBits + 7) / 8];
        Helpers.FixedToBytesL(dest, 0, data, isSigned, intBits, fracBits);
		this.PackBitArray(dest, totalBits);
    }

    /**
     * Pack an UUID in to the data
     * 
     * @param data
     */
    public void PackUUID(final UUID data)
    {
    	if (0 < bitPos)
    	{
			this.bitPos = 0;
			this.bytePos++;
    	}
		this.PackBitArray(data.getBytes(), 128);
    }

    public void PackString(final String str) throws UnsupportedEncodingException
    {
    	if (0 < bitPos)
    	{
			this.bitPos = 0;
			this.bytePos++;
    	}
		this.PackBitArray(str.getBytes(StandardCharsets.UTF_8), str.length());
    }

    /*
     *
     * 
     * @param data
     */
    public void PackColor(final Color4 data)
    {
		this.PackBitArray(data.getBytes(), 32);
    }

    /**
     * Unpacking a floating point value from the data
     *
     * @returns Unpacked floating point value
     */
    public float UnpackFloat()
    {
        return Helpers.BytesToFloatL(this.UnpackBitsArray(32), 0);
    }

    /**
     * Unpack a variable number of bits from the data in to integer format
     *
     * @param totalCount Number of bits to unpack
     * @returns An integer containing the unpacked bits
     * @remarks This function is only useful up to 32 bits
     */
    public int UnpackBits(final int totalCount)
    {
        return Helpers.BytesToInt32L(this.UnpackBitsArray(totalCount), 0);
    }

    /**
     * Unpack a variable number of bits from the data in to unsigned integer format
     *
     * @param totalCount Number of bits to unpack
     * @returns An unsigned integer containing the unpacked bits
     * @remarks This function is only useful up to 32 bits
     */
    public long UnpackUBits(final int totalCount)
    {
        return Helpers.BytesToUInt32L(this.UnpackBitsArray(totalCount), 0);
    }

    /**
     * Unpack a 16-bit signed integer
     *
     * @returns 16-bit signed integer
     */
    public short UnpackShort()
    {
        return Helpers.BytesToInt16L(this.UnpackBitsArray(16), 0);
    }

    /**
     * Unpack a 16-bit unsigned integer
     *
     * @returns 16-bit unsigned integer
     */
    public int UnpackUShort()
    {
        return Helpers.BytesToUInt16L(this.UnpackBitsArray(16), 0);
    }

    /**
     * Unpack a 32-bit signed integer
     *
     * @returns 32-bit signed integer
     */
    public int UnpackInt()
    {
        return Helpers.BytesToInt32L(this.UnpackBitsArray(32), 0);
    }

    /**
     * Unpack a 32-bit unsigned integer
     *
     * @returns 32-bit unsigned integer
     */
    public long UnpackUInt()
    {
        return Helpers.BytesToUInt32L(this.UnpackBitsArray(32), 0);
    }

    public byte UnpackByte()
    {
        final byte[] output = this.UnpackBitsArray(8);
        return output[0];
    }

    public float UnpackFixed(final boolean signed, final int intBits, final int fracBits)
    {
        int totalBits = intBits + fracBits;
        if (signed)
        {
            totalBits++;
        }

    	return Helpers.BytesToFixedL(this.UnpackBitsArray(totalBits), 0, signed, intBits, fracBits);
    }

    public String UnpackString(final int size) throws UnsupportedEncodingException
    {
        if (0 != bitPos || this.bytePos + size > this.Data.length) throw new IndexOutOfBoundsException();

        final String str = new String(this.Data, this.bytePos, size, StandardCharsets.UTF_8);
		this.bytePos += size;
        return str;
    }

    public UUID UnpackUUID()
    {
        if (0 != bitPos) throw new IndexOutOfBoundsException();

        final UUID val = new UUID(this.Data, this.bytePos);
		this.bytePos += 16;
        return val;
    }

    private void PackBitArray(final byte[] data, int totalCount)
    {
        int count = 0;
        int curBytePos = 0;
        int curBitPos = 0;

        while (0 < totalCount)
        {
            if (MAX_BITS < totalCount)
            {
                count = this.MAX_BITS;
                totalCount -= this.MAX_BITS;
            }
            else
            {
                count = totalCount;
                totalCount = 0;
            }

            while (0 < count)
            {
                final byte curBit = (byte)(0x80 >> this.bitPos);

                if (0 != (data[curBytePos] & (0x01 << (count - 1))))
					this.Data[this.bytePos] |= curBit;
                else
					this.Data[this.bytePos] &= (byte)~curBit;

                --count;
                ++this.bitPos;
                ++curBitPos;

                if (MAX_BITS <= bitPos)
                {
					this.bitPos = 0;
                    ++this.bytePos;
                }
                if (MAX_BITS <= curBitPos)
                {
                    curBitPos = 0;
                    ++curBytePos;
                }
            }
        }
    }

    private byte[] UnpackBitsArray(int totalCount)
    {
        int count = 0;
        final byte[] output = new byte[4];
        int curBytePos = 0;
        int curBitPos = 0;

        while (0 < totalCount)
        {
            if (MAX_BITS < totalCount)
            {
                count = this.MAX_BITS;
                totalCount -= this.MAX_BITS;
            }
            else
            {
                count = totalCount;
                totalCount = 0;
            }

            while (0 < count)
            {
                // Shift the previous bits
                output[curBytePos] <<= 1;

                // Grab one bit
                if (0 != (Data[bytePos] & (0x80 >> bitPos++)))
                    ++output[curBytePos];

                --count;
                ++curBitPos;

                if (MAX_BITS <= bitPos)
                {
					this.bitPos = 0;
                    ++this.bytePos;
                }
                if (MAX_BITS <= curBitPos)
                {
                    curBitPos = 0;
                    ++curBytePos;
                }
            }
        }

        return output;
    }
}
