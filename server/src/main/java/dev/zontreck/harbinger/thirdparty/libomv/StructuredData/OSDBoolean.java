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

public class OSDBoolean extends OSD {
	private static final byte[] trueBinary = { 0x31 };
	private static final byte[] falseBinary = { 0x30 };
	private final boolean value;

	public OSDBoolean ( final boolean value ) {
		this.value = value;
	}

	@Override
	public OSDType getType ( ) {
		return OSDType.Boolean;
	}

	@Override
	public boolean AsBoolean ( ) {
		return this.value;
	}

	@Override
	public int AsInteger ( ) {
		return this.value ? 1 : 0;
	}

	@Override
	public double AsReal ( ) {
		return this.value ? 1.0d : 0.0d;
	}

	@Override
	public String AsString ( ) {
		return this.value ? "1" : "0";
	}

	@Override
	public byte[] AsBinary ( ) {
		return this.value ? OSDBoolean.trueBinary : OSDBoolean.falseBinary;
	}

	@Override
	public int hashCode ( ) {
		return this.value ? 1 : 0;
	}

	@Override
	public boolean equals ( final Object obj ) {
		return null != obj && obj instanceof OSD && this.equals ( ( OSD ) obj );
	}

	public boolean equals ( final OSD osd ) {
		return null != osd && osd.AsBoolean ( ) == this.value;
	}

	@Override
	public String toString ( ) {
		return this.AsString ( );
	}
}