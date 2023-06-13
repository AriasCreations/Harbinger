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

import libomv.utils.Helpers;

public class OSDInteger extends OSD
{
	private final int value;

	@Override
	public OSDType getType()
	{
		return OSDType.Integer;
	}

	public OSDInteger(final int value)
	{
		this.value = value;
	}

	@Override
	public boolean AsBoolean()
	{
		return 0 != value;
	}

	@Override
	public int AsInteger()
	{
		return this.value;
	}

	@Override
	public int AsUInteger()
	{
		return (this.value & 0xFFFFFFFF);
	}

	@Override
	public long AsLong()
	{
		return this.value;
	}

	@Override
	public long AsULong()
	{
		return (this.value & 0xFFFFFFFF);
	}

	@Override
	public double AsReal()
	{
		return this.value;
	}

	@Override
	public String AsString()
	{
		return ((Integer) this.value).toString();
	}

	@Override
	public byte[] AsBinary()
	{
		return Helpers.Int32ToBytesB(this.value);
	}

	@Override
	public int hashCode()
	{
		return this.value;
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof OSD && this.equals((OSD)obj);
	}

	public boolean equals(final OSD osd)
	{
		return null != osd && osd.AsInteger() == this.value;
	}

	@Override
	public String toString()
	{
		return this.AsString();
	}
}
