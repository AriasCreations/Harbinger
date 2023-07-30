/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class OSDDate extends OSD {
	private final long value;

	public OSDDate ( final Date value ) {
		this.value = value.getTime ( );
	}

	@Override
	public OSDType getType ( ) {
		return OSDType.Date;
	}

	@Override
	public String AsString ( ) {
		final SimpleDateFormat df = new SimpleDateFormat ( OSD.FRACT_DATE_FMT );
		df.setTimeZone ( TimeZone.getTimeZone ( "UTC" ) );
		return df.format ( new Date ( this.value ) );
	}

	@Override
	public int AsInteger ( ) {
		return ( int ) this.value / 1000;
	}

	@Override
	public int AsUInteger ( ) {
		return ( int ) ( this.value / 1000 ) & 0xffffffff;
	}

	@Override
	public long AsLong ( ) {
		return ( this.value / 1000 );
	}

	@Override
	public long AsULong ( ) {
		return ( this.value / 1000 ) & 0xffffffffffffffffL;
	}

	@Override
	public byte[] AsBinary ( ) {
		return Helpers.DoubleToBytesL ( this.value / 1000.0 );
	}

	@Override
	public Date AsDate ( ) {
		return new Date ( this.value );
	}

	@Override
	public int hashCode ( ) {
		return ( int ) this.value | ( int ) ( this.value >> 32 );
	}

	@Override
	public boolean equals ( final Object obj ) {
		return null != obj && obj instanceof OSD && this.equals ( ( OSD ) obj );
	}

	public boolean equals ( final OSD osd ) {
		return null != osd && osd.AsLong ( ) == this.value;
	}

	@Override
	public String toString ( ) {
		return this.AsString ( );
	}
}
