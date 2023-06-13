/**
 * Copyright (c) 2010-2017, Frederick Martian
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
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
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
package libomv.imaging;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.input.SwappedDataInputStream;

public class TGAHeader
{
	protected class TGAColorMap
    {
        public int offset;
        public int length;
        public byte bits;
        int alphaBits;
        int colorBits;
        
        public int RMask, GMask, BMask, AMask;
        public int RShift, GShift, BShift, AShift;

        byte[] RedM;
        byte[] GreenM;
        byte[] BlueM;
        byte[] AlphaM;

        public TGAColorMap()
        {
        }

        public TGAColorMap(final SwappedDataInputStream is) throws IOException
        {
			this.offset = is.readUnsignedShort();
			this.length = is.readUnsignedShort();
			this.bits = is.readByte();
        }
        
        public void writeHeader(final OutputStream os) throws IOException
        {
        	os.write((byte)(this.offset & 0xFF));
        	os.write((byte)((this.offset >> 8) & 0xFF));
        	os.write((byte)(this.length & 0xFF));
        	os.write((byte)((this.length >> 8) & 0xFF));
        	os.write(this.bits);
        }
        
        public void readMap(final SwappedDataInputStream is, final boolean gray) throws IOException
        {
        	if (8 == bits || 32 == bits)
        	{
				this.alphaBits = 8;
        	}
        	else if (16 == bits)
        	{
				this.alphaBits = 1;
        	}

			this.colorBits = this.bits - this.alphaBits;
        	if (!gray)
        	{
				this.colorBits /= 3;

				this.BMask = ((2 ^ this.colorBits) - 1);
				this.GShift = this.BShift + this.colorBits;
				this.GMask = ((2 ^ this.colorBits) - 1);
				this.RShift = this.GShift + this.colorBits;

                if (0 < GMask)
					this.GreenM = new byte[this.length];
                if (0 < BMask)
					this.BlueM = new byte[this.length];
        	}

			this.RMask = ((2 ^ this.colorBits) - 1);
			this.AShift = this.RShift + this.colorBits;
			this.AMask = ((2 ^ this.alphaBits) - 1);

            if (0 < RMask)
				this.RedM = new byte[this.length];
            if (0 < AMask)
				this.AlphaM = new byte[this.length];
        	
        	
        	for (int i = 0; i < this.length; i++)
        	{
                long x = 0;
                for (int k = 0; k < this.bits; k += 8)
                {
                    x |= (long) is.readUnsignedByte() << k;
                }

        		if (null != RedM)
					this.RedM[i] = (byte)((x >> this.RShift) & this.RMask);
        		if (null != GreenM)
					this.GreenM[i] = (byte)((x >> this.GShift) & this.GMask);
        		if (null != BlueM)
					this.BlueM[i] = (byte)((x >> this.BShift) & this.BMask);
        		if (null != AlphaM)
					this.AlphaM[i] = (byte)((x >> this.AShift) & this.AMask);
            }
        }
        
        public void setBits(final TGAImageSpec spec, final boolean gray)
        {
            // Treat 8 bit images as alpha channel
            if (0 == alphaBits && (8 == spec.PixelDepth || 32 == spec.PixelDepth))
            {
				this.alphaBits = 8;
            }

			this.length = 0;
			this.bits = spec.PixelDepth;
			this.colorBits = this.bits - this.alphaBits;
        	if (!gray)
        	{
				this.colorBits /= 3;

				this.BMask = ((2 ^ Math.round(this.colorBits)) - 1);
				this.GShift = this.BShift + Math.round(this.colorBits);
				this.GMask = ((2 ^ (int)Math.ceil(this.colorBits)) - 1);
				this.RShift = this.GShift + (int)Math.ceil(this.colorBits);
        	}
			this.RMask = ((2 ^ (int)Math.floor(this.colorBits)) - 1);
			this.AShift = this.RShift + (int)Math.floor(this.colorBits);
			this.AMask = ((2 ^ this.alphaBits) - 1);
        }
    }

	protected class TGAImageSpec
    {
        public int XOrigin;
        public int YOrigin;
        public int Width;
        public int Height;
        public byte PixelDepth;
        public byte Descriptor;

    	public TGAImageSpec(final ManagedImage image)
        {
			this.Width = image.getWidth();
			this.Height = image.getHeight();
    		
    		if (ManagedImage.ImageChannels.Gray == image.getChannels())
    		{
				this.PixelDepth = 8;
    		}
    		else if (ManagedImage.ImageChannels.Color == image.getChannels())
    		{
				this.PixelDepth = 24;
    		}
    		if (ManagedImage.ImageChannels.Alpha == image.getChannels())
    		{
				this.Descriptor = 0 < PixelDepth ? (byte)0x28 : (byte)0x20;
				this.PixelDepth += 8;
    		}
        }
    	
    	public TGAImageSpec(final SwappedDataInputStream is) throws IOException
        {
			this.XOrigin = is.readUnsignedShort();
			this.YOrigin = is.readUnsignedShort();
			this.Width = is.readUnsignedShort();
			this.Height = is.readUnsignedShort();
			this.PixelDepth = is.readByte();
			this.Descriptor = is.readByte();
        }

        public void write(final OutputStream os) throws IOException
        {
        	os.write((byte)(this.XOrigin & 0xFF));
        	os.write((byte)((this.XOrigin >> 8) & 0xFF));
        	os.write((byte)(this.YOrigin & 0xFF));
        	os.write((byte)((this.YOrigin >> 8) & 0xFF));
        	os.write((byte)(this.Width & 0xFF));
        	os.write((byte)((this.Width >> 8) & 0xFF));
        	os.write((byte)(this.Height & 0xFF));
        	os.write((byte)((this.Height >> 8) & 0xFF));
        	os.write(this.PixelDepth);
        	os.write(this.Descriptor);
        }

        public byte getAlphaBits()
        {
            return (byte)(this.Descriptor & 0xF);
        }

        public void setAlphaBits(final int value)
        {
			this.Descriptor = (byte)((this.Descriptor & ~0xF) | (value & 0xF));
        }

        public boolean getBottomUp()
        {
            return 0x20 == (Descriptor & 0x20);
        }
        
        public void setBottomUp(final boolean value)
        {
			this.Descriptor = (byte)((this.Descriptor & ~0x20) | (value ? 0x0 : 0x20));
        }
    }

    public byte IdLength;
    public byte ColorMapType;
    public byte ImageType;

    public TGAColorMap ColorMap;
    public TGAImageSpec ImageSpec;

    public TGAHeader(final ManagedImage image)
    {
		this.ColorMap = new TGAColorMap();
		this.ImageSpec = new TGAImageSpec(image);
    }
    
    public TGAHeader(final SwappedDataInputStream is) throws IOException
    {
		this.IdLength = is.readByte();
		this.ColorMapType = is.readByte();
		this.ImageType = is.readByte();
		this.ColorMap = new TGAColorMap(is);
		this.ImageSpec = new TGAImageSpec(is);
		this.ColorMap.alphaBits = this.ImageSpec.getAlphaBits();

		is.skipBytes(this.IdLength); // Skip any ID Length data

		if (0 != ColorMapType)
        {
        	if (8 != ColorMap.bits &&
					16 != ColorMap.bits &&
					24 != ColorMap.bits &&
					24 != ColorMap.bits)
                throw new IllegalArgumentException("Not a supported tga file.");

			this.ColorMap.readMap(is, 3 == ImageType % 8);
        }
        else
        {
			this.ColorMap.setBits(this.ImageSpec, 3 == ImageType % 8);
        }
    }

    public void write(final OutputStream os) throws IOException
    {
    	os.write(this.IdLength);
    	os.write(this.ColorMapType);
    	os.write(this.ImageType);
		this.ColorMap.writeHeader(os);
		this.ImageSpec.write(os);
    }

    public boolean getRleEncoded()
    {
         return 8 <= ImageType;
    }
}
