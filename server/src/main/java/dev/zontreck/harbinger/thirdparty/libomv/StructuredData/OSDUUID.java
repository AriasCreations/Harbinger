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

import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;

public class OSDUUID extends OSD
{
	private UUID value;

	@Override
	public OSDType getType()
	{
		return OSDType.UUID;
	}

	public OSDUUID(final UUID value)
	{
		this.value = value;
	}

	@Override
	public boolean AsBoolean()
	{
		return null != value && !this.value.equals(UUID.Zero);
	}

	@Override
	public String AsString()
	{
		return this.value.toString();
	}

	@Override
	public UUID AsUUID()
	{
		return this.value;
	}

	@Override
	public byte[] AsBinary()
	{
		return this.value.getBytes();
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
		return null != osd && osd.AsUUID().equals(this.value);
	}

	@Override
	public OSD clone()
	{
		final OSDUUID osd = (OSDUUID)super.clone();
		osd.value = new UUID(value);
		return osd;
	}

	@Override
	public String toString()
	{
		return this.AsString();
	}
}
