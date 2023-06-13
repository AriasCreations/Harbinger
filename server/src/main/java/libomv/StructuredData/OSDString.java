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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import libomv.types.Color4;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;
import libomv.utils.Helpers;

public class OSDString extends OSD
{
	private final String value;

	@Override
	public OSDType getType()
	{
		return OSDType.String;
	}

	public OSDString(final String value)
	{
		// Refuse to hold null pointers
		if (null != value)
			this.value = value;
		else
			this.value = Helpers.EmptyString;
	}

	@Override
	public boolean AsBoolean()
	{
		if (null == value || this.value.isEmpty() || "false".equalsIgnoreCase(value))
			return false;

		try
		{
			if (0.0 == Double.parseDouble(value))
				return false;
		}
		catch (final NumberFormatException ex)
		{}
			
		return true;
	}

	@Override
	public int AsInteger()
	{
		try
		{
			return (int) Math.floor(Double.parseDouble(this.value));
		}
		catch (final NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public int AsUInteger()
	{
		try
		{
			return ((int) Math.floor(Double.parseDouble(this.value)) & 0xffffffff);
		}
		catch (final NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public long AsLong()
	{
		try
		{
			return (long) Math.floor(Double.parseDouble(this.value));
		}
		catch (final NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public long AsULong()
	{
		try
		{
			return ((long) Math.floor(Double.parseDouble(this.value)) & 0xffffffffffffffffL);
		}
		catch (final NumberFormatException ex)
		{
			return 0;
		}
	}

	@Override
	public double AsReal()
	{
		return Helpers.TryParseDouble(this.value);
	}

	@Override
	public Vector2 AsVector2()
	{
		try
		{
		    return OSDParser.deserialize(value, OSDFormat.Notation).AsVector2();
	    }
		catch (final Exception ex)
		{
			return Vector2.Zero;
		}
	}

	@Override
	public Vector3 AsVector3()
	{
		try
		{
		    return OSDParser.deserialize(value, OSDFormat.Notation).AsVector3();
	    }
		catch (final Exception ex)
		{
			return Vector3.Zero;
		}
	}

	@Override
	public Vector3d AsVector3d()
	{
		try
		{
		    return OSDParser.deserialize(value, OSDFormat.Notation).AsVector3d();
	    }
		catch (final Exception ex)
		{
			return Vector3d.Zero;
		}
	}

	@Override
	public Vector4 AsVector4()
	{
		try
		{
		    return OSDParser.deserialize(value, OSDFormat.Notation).AsVector4();
	    }
		catch (final Exception ex)
		{
			return Vector4.Zero;
		}
	}

	@Override
	public Quaternion AsQuaternion()
	{
		try
		{
		    return OSDParser.deserialize(value, OSDFormat.Notation).AsQuaternion();
	    }
		catch (final Exception ex)
		{
			return Quaternion.Identity;
		}
	}

	@Override
	public Color4 AsColor4()
	{
		try
		{
		    return OSDParser.deserialize(value, OSDFormat.Notation).AsColor4();
	    }
		catch (final Exception ex)
		{
			return Color4.Black;
		}
	}
	
	@Override
	public String AsString()
	{
		return this.value;
	}

	@Override
	public byte[] AsBinary()
	{
		return Helpers.StringToBytes(this.value);
	}

	@Override
	public InetAddress AsInetAddress()
	{
		try
		{
			final int i = this.value.indexOf(':');
			if (0 > i)
				return InetAddress.getByName(this.value);
			return InetAddress.getByName(this.value.substring(0, i));
		}
		catch (final UnknownHostException ex)
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
	public Date AsDate()
	{
		final SimpleDateFormat df = new SimpleDateFormat(OSD.FRACT_DATE_FMT);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try
		{
			return df.parse(this.value);
		}
		catch (final ParseException ex1)
		{
			try
			{
				df.applyPattern(OSD.WHOLE_DATE_FMT);
				return df.parse(this.value);
			}
			catch (final ParseException ex2)
			{
				return Helpers.Epoch;
			}
		}
	}

	@Override
	public URI AsUri()
	{
		try
		{
			return new URI(this.value);
		}
		catch (final URISyntaxException ex)
		{
			return null;
		}
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
		return null != osd && osd.AsString().equals(this.value);
	}

	@Override
	public String toString()
	{
		return this.AsString();
	}
}
