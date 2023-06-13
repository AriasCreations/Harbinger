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
package libomv.StructuredData;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import libomv.types.UUID;
import libomv.utils.Helpers;

public class OSDBinary extends OSD
{
	private byte[] value;

	@Override
	public OSDType getType()
	{
		return OSDType.Binary;
	}

	public OSDBinary(final OSDBinary value)
	{
		if (null != value)
		{
			this.value = value.value;
		}
		else
		{
			this.value = Helpers.EmptyBytes;
		}
	}

	public OSDBinary(final byte[] value)
	{
		if (null != value)
		{
			this.value = value;
		}
		else
		{
			this.value = Helpers.EmptyBytes;
		}
	}

	public OSDBinary(final int value)
	{
		this.value = Helpers.Int32ToBytesB(value);
	}

	public OSDBinary(final long value)
	{
		this.value = Helpers.Int64ToBytesB(value);
	}

	@Override
	public String AsString()
	{
		try {
			return Helpers.BytesToString(this.value);
		}
		catch (final UnsupportedEncodingException e) { }
		return null;
	}

	@Override
	public byte[] AsBinary()
	{
		return this.value;
	}

	@Override
	public InetAddress AsInetAddress()
	{
		try
		{
			return InetAddress.getByAddress(this.value);
		}
		catch (final UnknownHostException e)
		{
			return null;
		}
	}

	@Override
	public UUID AsUUID()
	{
		return new UUID(this.value);
	}

	@Override
	public int AsUInteger()
	{
		return (int) Helpers.BytesToUInt32B(this.value);
	}

	@Override
	public long AsLong()
	{
		return Helpers.BytesToInt64B(this.value);
	}

	@Override
	public long AsULong()
	{
		return Helpers.BytesToUInt64B(this.value);
	}

	@Override
	public int hashCode()
	{
		return this.value.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof OSD && this.equals((OSD)obj);
	}

	public boolean equals(final OSD osd)
	{
		return null != osd && OSDType.Binary == osd.getType() && ((OSDBinary)osd).value.equals(this.value);
	}

	@Override
	public OSD clone()
	{
		final OSDBinary osd = (OSDBinary)super.clone();
		osd.value = new byte[value.length];
		System.arraycopy(value, 0, osd.value, 0, value.length);
		return osd;
	}

	@Override
	public String toString()
	{
		return Helpers.BytesToHexString(this.value, null);
	}
}
