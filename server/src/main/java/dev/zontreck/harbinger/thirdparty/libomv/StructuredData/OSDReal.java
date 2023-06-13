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
package dev.zontreck.harbinger.thirdparty.libomv.StructuredData;

import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

public class OSDReal extends OSD
{
	private final double value;

	@Override
	public OSDType getType()
	{
		return OSDType.Real;
	}

	public OSDReal(final double value)
	{
		this.value = value;
	}

	@Override
	public boolean AsBoolean()
	{
		return (!Double.isNaN(this.value) && 0.0d != value);
	}

	@Override
	public int AsInteger()
	{
		if (Double.isNaN(this.value))
		{
			return 0;
		}
		if (Integer.MAX_VALUE < value)
		{
			return Integer.MAX_VALUE;
		}
		if (Integer.MIN_VALUE > value)
		{
			return Integer.MIN_VALUE;
		}
		return Helpers.roundFromZero(this.value);
	}

	@Override
	public int AsUInteger()
	{
		if (Double.isNaN(this.value))
		{
			return 0;
		}
		if ((2 * (double) Integer.MAX_VALUE + 1) < value)
		{
			return (0xffffffff);
		}
		if (0.0f > value)
		{
			return 0;
		}
		return Helpers.roundFromZero(this.value);
	}

	@Override
	public long AsLong()
	{
		if (Double.isNaN(this.value))
		{
			return 0;
		}
		if (Long.MAX_VALUE < value)
		{
			return Long.MAX_VALUE;
		}
		if (Long.MIN_VALUE > value)
		{
			return Long.MIN_VALUE;
		}
		return Helpers.roundFromZero(this.value);
	}

	@Override
	public long AsULong()
	{
		if (Double.isNaN(this.value))
		{
			return 0;
		}
		if ((2 * (double) Long.MAX_VALUE + 1) < value)
		{
			return 0xffffffffffL;
		}
		if (0.0d > value)
		{
			return 0;
		}
		return Helpers.roundFromZero(this.value);
	}

	@Override
	public double AsReal()
	{
		return this.value;
	}

	@Override
	public String AsString()
	{
		if (Double.isNaN(this.value))
		{
			return "NaN";
		}
		else if (Double.isInfinite(this.value))
		{
			return (0 > value) ? "-Inf" : "Inf";
		}
		return Double.toString(this.value);
	}

	@Override
	public byte[] AsBinary()
	{
		return Helpers.DoubleToBytesB(this.value);
	}

	@Override
	public int hashCode()
	{
		final long v = Double.doubleToLongBits(this.value);
		return (int)(v ^ (v >>> 32));
	}
	
	@Override
	public boolean equals(final Object obj)
	{
		return null != obj && obj instanceof OSD && this.equals((OSD)obj);
	}

	public boolean equals(final OSD osd)
	{
		return null != osd && Double.doubleToLongBits(osd.AsReal()) == Double.doubleToLongBits(this.value);
	}

	@Override
	public String toString()
	{
		return this.AsString();
	}
}
