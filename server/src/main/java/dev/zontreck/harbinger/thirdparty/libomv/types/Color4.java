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
package dev.zontreck.harbinger.thirdparty.libomv.types;

import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParser;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParserException;
import dev.zontreck.harbinger.thirdparty.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * An 8-bit color structure including an alpha channel
 */
public final class Color4 {
	/**
	 * A Color4 with zero RGB values and fully opaque (alpha 1.0)
	 */
	public static final Color4 Black = new Color4 ( 0.0f , 0.0f , 0.0f , 1.0f );
	/**
	 * A Color4 with full RGB values (1.0) and fully opaque (alpha 1.0)
	 */
	public static final Color4 White = new Color4 ( 1.0f , 1.0f , 1.0f , 1.0f );
	/**
	 * Red
	 */
	public float R;
	/**
	 * Green
	 */
	public float G;
	/**
	 * Blue
	 */
	public float B;
	/**
	 * Alpha
	 */
	public float A;

	/**
	 * Builds a color from four values
	 *
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public Color4 ( final byte r , final byte g , final byte b , final byte a ) {
		final float quanta = 1.0f / 255.0f;

		this.R = r * quanta;
		this.G = g * quanta;
		this.B = b * quanta;
		this.A = a * quanta;
	}

	/**
	 * Builds a color from four values
	 *
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @throws IllegalArgumentException if one of the values is outside 0 .. 1.0
	 */
	public Color4 ( final float r , final float g , final float b , final float a ) {
		// Quick check to see if someone is doing something obviously wrong
		// like using float values from 0.0 - 255.0
		if ( 1.0f < r || 1.0f < g || 1.0f < b || 1.0f < a ) {
			throw new IllegalArgumentException ( String.format (
					"Attempting to initialize Color4 with out of range values <%f,%f,%f,%f>" , r , g , b , a ) );
		}

		// Valid range is from 0.0 to 1.0
		this.R = Helpers.Clamp ( r , 0.0f , 1.0f );
		this.G = Helpers.Clamp ( g , 0.0f , 1.0f );
		this.B = Helpers.Clamp ( b , 0.0f , 1.0f );
		this.A = Helpers.Clamp ( a , 0.0f , 1.0f );
	}

	/**
	 * Constructor, builds a Color4 from an XML reader
	 *
	 * @param parser XML pull parser reader
	 */
	public Color4 ( final XmlPullParser parser ) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Vector3
		final int eventType = parser.getEventType ( );
		if ( XmlPullParser.START_TAG != eventType )
			throw new XmlPullParserException ( "Unexpected Tag event " + eventType + " for tag name " + parser.getName ( ) , parser , null );

		this.A = 1.0f;
		while ( XmlPullParser.START_TAG == parser.nextTag ( ) ) {
			final String name = parser.getName ( );
			if ( "R".equalsIgnoreCase ( name ) ) {
				this.R = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else if ( "G".equalsIgnoreCase ( name ) ) {
				this.G = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else if ( "B".equalsIgnoreCase ( name ) ) {
				this.B = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else if ( "A".equalsIgnoreCase ( name ) ) {
				final String element = parser.nextText ( ).trim ( );
				if ( ! Helpers.isEmpty ( element ) )
					this.A = Helpers.TryParseFloat ( element );
			}
			else {
				Helpers.skipElement ( parser );
			}
		}
	}

	/**
	 * Builds a color from a byte array
	 *
	 * @param byteArray Byte array containing a 16 byte color
	 * @param pos       Beginning position in the byte array
	 * @param inverted  True if the byte array stores inverted values, otherwise
	 *                  false. For example the color black (fully opaque) inverted
	 *                  would be 0xFF 0xFF 0xFF 0x00
	 */
	public Color4 ( final byte[] byteArray , final int pos , final boolean inverted ) {
		this.fromBytes ( byteArray , pos , inverted );
	}

	/**
	 * Returns the raw bytes for this vector
	 *
	 * @param byteArray     Byte array containing a 16 byte color
	 * @param pos           Beginning position in the byte array
	 * @param inverted      True if the byte array stores inverted values, otherwise
	 *                      false. For example the color black (fully opaque) inverted
	 *                      would be 0xFF 0xFF 0xFF 0x00
	 * @param alphaInverted True if the alpha value is inverted in addition to whatever
	 *                      the inverted parameter is. Setting inverted true and
	 *                      alphaInverted true will flip the alpha value back to
	 *                      non-inverted, but keep the other color bytes inverted
	 * @return A 16 byte array containing R, G, B, and A
	 */
	public Color4 ( final byte[] byteArray , final int pos , final boolean inverted , final boolean alphaInverted ) {
		this.fromBytes ( byteArray , pos , inverted , alphaInverted );
	}

	/**
	 * Copy constructor
	 *
	 * @param color Color to copy
	 */
	public Color4 ( final Color4 color ) {
		this.R = color.R;
		this.G = color.G;
		this.B = color.B;
		this.A = color.A;
	}

	/**
	 * Create an RGB color from a hue, saturation, value combination
	 *
	 * @param hue        Hue
	 * @param saturation Saturation
	 * @param value      Value
	 * @return An fully opaque RGB color (alpha is 1.0)
	 */
	public static Color4 fromHSV ( final double hue , final double saturation , final double value ) {
		double r = 0.0d;
		double g = 0.0d;
		double b = 0.0d;

		if ( 0.0d == saturation ) {
			// If s is 0, all colors are the same.
			// This is some flavor of gray.
			r = value;
			g = value;
			b = value;
		}
		else {
			final double p;
			final double q;
			final double t;

			final double fractionalSector;
			final int sectorNumber;
			final double sectorPos;

			// The color wheel consists of 6 sectors.
			// Figure out which sector we're in.
			sectorPos = hue / 60.0d;
			sectorNumber = ( int ) ( Math.floor ( sectorPos ) );

			// get the fractional part of the sector.
			// That is, how many degrees into the sector
			// are you?
			fractionalSector = sectorPos - sectorNumber;

			// Calculate values for the three axes
			// of the color.
			p = value * ( 1.0d - saturation );
			q = value * ( 1.0d - ( saturation * fractionalSector ) );
			t = value * ( 1.0d - ( saturation * ( 1.0d - fractionalSector ) ) );

			// Assign the fractional colors to r, g, and b
			// based on the sector the angle is in.
			switch ( sectorNumber ) {
				case 0:
					r = value;
					g = t;
					b = p;
					break;
				case 1:
					r = q;
					g = value;
					b = p;
					break;
				case 2:
					r = p;
					g = value;
					b = t;
					break;
				case 3:
					r = p;
					g = q;
					b = value;
					break;
				case 4:
					r = t;
					g = p;
					b = value;
					break;
				case 5:
					r = value;
					g = p;
					b = q;
					break;
				default:
					break;
			}
		}

		return new Color4 ( ( float ) r , ( float ) g , ( float ) b , 1.0f );
	}

	/**
	 * Performs linear interpolation between two colors
	 *
	 * @param value1 Color to start at
	 * @param value2 Color to end at
	 * @param amount Amount to interpolate
	 * @return The interpolated color
	 */
	public static Color4 lerp ( final Color4 value1 , final Color4 value2 , final float amount ) {
		return new Color4 ( Helpers.Lerp ( value1.R , value2.R , amount ) , Helpers.Lerp ( value1.G , value2.G , amount ) ,
				Helpers.Lerp ( value1.B , value2.B , amount ) , Helpers.Lerp ( value1.A , value2.A , amount )
		);
	}

	public static Color4 parse ( final XmlPullParser parser ) throws XmlPullParserException, IOException {
		return new Color4 ( parser );
	}

	public static boolean equals ( final Color4 lhs , final Color4 rhs ) {
		return null == lhs ? lhs == rhs : lhs.equals ( rhs );
	}

	public static Color4 add ( final Color4 lhs , final Color4 rhs ) {
		lhs.R += rhs.R;
		lhs.G += rhs.G;
		lhs.B += rhs.B;
		lhs.A += rhs.A;
		lhs.clampValues ( );

		return lhs;
	}

	public static Color4 minus ( final Color4 lhs , final Color4 rhs ) {
		lhs.R -= rhs.R;
		lhs.G -= rhs.G;
		lhs.B -= rhs.B;
		lhs.A -= rhs.A;
		lhs.clampValues ( );

		return lhs;
	}

	public static Color4 multiply ( final Color4 lhs , final Color4 rhs ) {
		lhs.R *= rhs.R;
		lhs.G *= rhs.G;
		lhs.B *= rhs.B;
		lhs.A *= rhs.A;
		lhs.clampValues ( );

		return lhs;
	}

	/**
	 * CompareTo implementation
	 * <p>
	 * Sorting ends up like this: |--Grayscale--||--Color--|. Alpha is only used
	 * when the colors are otherwise equivalent
	 */
	public int compareTo ( final Color4 color ) {
		final float thisHue = this.getHue ( );
		final float thatHue = color.getHue ( );

		if ( 0.0f > thisHue && 0.0f > thatHue ) {
			// Both monochromatic
			if ( this.R == color.R ) {
				// Monochromatic and equal, compare alpha
				return ( ( Float ) this.A ).compareTo ( color.A );
			}
			// Compare lightness
			return ( ( Float ) this.R ).compareTo ( this.R );
		}

		if ( thisHue == thatHue ) {
			// RGB is equal, compare alpha
			return ( ( Float ) this.A ).compareTo ( color.A );
		}
		// Compare hues
		return ( ( Float ) thisHue ).compareTo ( thatHue );
	}

	public void fromBytes ( final byte[] byteArray , final int pos , final boolean inverted ) {
		final float quanta = 1.0f / 255.0f;

		if ( inverted ) {
			this.R = ( 255 - ( byteArray[ pos ] & 0xFF ) ) * quanta;
			this.G = ( 255 - ( byteArray[ pos + 1 ] & 0xFF ) ) * quanta;
			this.B = ( 255 - ( byteArray[ pos + 2 ] & 0xFF ) ) * quanta;
			this.A = ( 255 - ( byteArray[ pos + 3 ] & 0xFF ) ) * quanta;
		}
		else {
			this.R = ( byteArray[ pos ] & 0xFF ) * quanta;
			this.G = ( byteArray[ pos + 1 ] & 0xFF ) * quanta;
			this.B = ( byteArray[ pos + 2 ] & 0xFF ) * quanta;
			this.A = ( byteArray[ pos + 3 ] & 0xFF ) * quanta;
		}
	}

	/**
	 * Builds a color from a byte array
	 *
	 * @param byteArray     Byte array containing a 16 byte color
	 * @param pos           Beginning position in the byte array
	 * @param inverted      True if the byte array stores inverted values, otherwise
	 *                      false. For example the color black (fully opaque) inverted
	 *                      would be 0xFF 0xFF 0xFF 0x00
	 * @param alphaInverted True if the alpha value is inverted in addition to whatever
	 *                      the inverted parameter is. Setting inverted true and
	 *                      alphaInverted true will flip the alpha value back to
	 *                      non-inverted, but keep the other color bytes inverted
	 */
	public void fromBytes ( final byte[] byteArray , final int pos , final boolean inverted , final boolean alphaInverted ) {
		this.fromBytes ( byteArray , pos , inverted );

		if ( alphaInverted ) {
			this.A = 1.0f - this.A;
		}
	}

	public byte[] getBytes ( ) {
		final byte[] byteArray = new byte[ 4 ];
		this.toBytes ( byteArray , 0 , false );
		return byteArray;
	}

	public byte[] getBytes ( final boolean inverted ) {
		final byte[] byteArray = new byte[ 4 ];
		this.toBytes ( byteArray , 0 , inverted );
		return byteArray;
	}

	public void write ( final OutputStream stream , final boolean inverted ) throws IOException {
		final byte R = Helpers.FloatToByte ( this.R , 0.0f , 1.0f );
		final byte G = Helpers.FloatToByte ( this.G , 0.0f , 1.0f );
		final byte B = Helpers.FloatToByte ( this.B , 0.0f , 1.0f );
		final byte A = Helpers.FloatToByte ( this.A , 0.0f , 1.0f );

		if ( inverted ) {
			stream.write ( ( byte ) ( 255 - ( R & 0xFF ) ) );
			stream.write ( ( byte ) ( 255 - ( G & 0xFF ) ) );
			stream.write ( ( byte ) ( 255 - ( B & 0xFF ) ) );
			stream.write ( ( byte ) ( 255 - ( A & 0xFF ) ) );
		}
		else {
			stream.write ( R );
			stream.write ( G );
			stream.write ( B );
			stream.write ( A );
		}
	}

	public byte[] getFloatBytes ( ) {
		final byte[] bytes = new byte[ 16 ];
		this.toFloatBytesL ( bytes , 0 );
		return bytes;
	}

	/**
	 * Writes the raw bytes for this color to a byte array
	 *
	 * @param dest Destination byte array
	 * @param pos  Position in the destination array to start writing. Must be at
	 *             least 16 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes ( final byte[] dest , final int pos ) {
		return this.toBytes ( dest , pos , false );
	}

	/**
	 * Serializes this color into four bytes in a byte array
	 *
	 * @param dest     Destination byte array
	 * @param pos      Position in the destination array to start writing. Must be at
	 *                 least 4 bytes before the end of the array
	 * @param inverted True to invert the output (1.0 becomes 0 instead of 255)
	 * @return number of bytes filled to the byte array
	 */
	public int toBytes ( final byte[] dest , final int pos , final boolean inverted ) {
		dest[ pos ] = Helpers.FloatToByte ( this.R , 0.0f , 1.0f );
		dest[ pos + 1 ] = Helpers.FloatToByte ( this.G , 0.0f , 1.0f );
		dest[ pos + 2 ] = Helpers.FloatToByte ( this.B , 0.0f , 1.0f );
		dest[ pos + 3 ] = Helpers.FloatToByte ( this.A , 0.0f , 1.0f );

		if ( inverted ) {
			dest[ pos ] = ( byte ) ( 255 - ( dest[ pos ] & 0xFF ) );
			dest[ pos + 1 ] = ( byte ) ( 255 - ( dest[ pos + 1 ] & 0xFF ) );
			dest[ pos + 2 ] = ( byte ) ( 255 - ( dest[ pos + 2 ] & 0xFF ) );
			dest[ pos + 3 ] = ( byte ) ( 255 - ( dest[ pos + 3 ] & 0xFF ) );
		}
		return 4;
	}

	/**
	 * Writes the raw bytes for this color to a byte array in little endian
	 * format
	 *
	 * @param dest Destination byte array
	 * @param pos  Position in the destination array to start writing. Must be at
	 *             least 16 bytes before the end of the array
	 * @return number of bytes filled to the byte array
	 */
	public int toFloatBytesL ( final byte[] dest , final int pos ) {
		Helpers.FloatToBytesL ( this.R , dest , pos );
		Helpers.FloatToBytesL ( this.G , dest , pos + 4 );
		Helpers.FloatToBytesL ( this.B , dest , pos + 8 );
		Helpers.FloatToBytesL ( this.A , dest , pos + 12 );
		return 4;
	}

	public float getHue ( ) {
		final float HUE_MAX = 360.0f;

		final float max = Math.max ( Math.max ( this.R , this.G ) , this.B );
		final float min = Math.min ( Math.min ( this.R , this.B ) , this.B );

		if ( max == min ) {
			// Achromatic, hue is undefined
			return - 1.0f;
		}
		else if ( this.R == max ) {
			final float bDelta = ( ( ( max - this.B ) * ( HUE_MAX / 6.0f ) ) + ( ( max - min ) / 2.0f ) ) / ( max - min );
			final float gDelta = ( ( ( max - this.G ) * ( HUE_MAX / 6.0f ) ) + ( ( max - min ) / 2.0f ) ) / ( max - min );
			return bDelta - gDelta;
		}
		else if ( this.G == max ) {
			final float rDelta = ( ( ( max - this.R ) * ( HUE_MAX / 6.0f ) ) + ( ( max - min ) / 2.0f ) ) / ( max - min );
			final float bDelta = ( ( ( max - this.B ) * ( HUE_MAX / 6.0f ) ) + ( ( max - min ) / 2.0f ) ) / ( max - min );
			return ( HUE_MAX / 3.0f ) + rDelta - bDelta;
		}
		else
		// B == max
		{
			final float gDelta = ( ( ( max - this.G ) * ( HUE_MAX / 6.0f ) ) + ( ( max - min ) / 2.0f ) ) / ( max - min );
			final float rDelta = ( ( ( max - this.R ) * ( HUE_MAX / 6.0f ) ) + ( ( max - min ) / 2.0f ) ) / ( max - min );
			return ( ( 2.0f * HUE_MAX ) / 3.0f ) + gDelta - rDelta;
		}
	}

	/**
	 * Ensures that values are in range 0-1
	 */
	public void clampValues ( ) {
		if ( 0.0f > R ) {
			this.R = 0.0f;
		}
		if ( 0.0f > G ) {
			this.G = 0.0f;
		}
		if ( 0.0f > B ) {
			this.B = 0.0f;
		}
		if ( 0.0f > A ) {
			this.A = 0.0f;
		}
		if ( 1.0f < R ) {
			this.R = 1.0f;
		}
		if ( 1.0f < G ) {
			this.G = 1.0f;
		}
		if ( 1.0f < B ) {
			this.B = 1.0f;
		}
		if ( 1.0f < A ) {
			this.A = 1.0f;
		}
	}

	public void serializeXml ( final XmlSerializer writer , final String namespace , final String name ) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag ( namespace , name );
		writer.startTag ( namespace , "R" ).text ( Float.toString ( this.R ) ).endTag ( namespace , "R" );
		writer.startTag ( namespace , "G" ).text ( Float.toString ( this.G ) ).endTag ( namespace , "G" );
		writer.startTag ( namespace , "B" ).text ( Float.toString ( this.B ) ).endTag ( namespace , "B" );
		writer.startTag ( namespace , "A" ).text ( Float.toString ( this.A ) ).endTag ( namespace , "A" );
		writer.endTag ( namespace , name );
	}

	public void serializeXml ( final XmlSerializer writer , final String namespace , final String name , final Locale locale ) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag ( namespace , name );
		writer.startTag ( namespace , "R" ).text ( String.format ( locale , "%f" , this.R ) ).endTag ( namespace , "R" );
		writer.startTag ( namespace , "G" ).text ( String.format ( locale , "%f" , this.G ) ).endTag ( namespace , "G" );
		writer.startTag ( namespace , "B" ).text ( String.format ( locale , "%f" , this.B ) ).endTag ( namespace , "B" );
		writer.startTag ( namespace , "A" ).text ( String.format ( locale , "%f" , this.A ) ).endTag ( namespace , "A" );
		writer.endTag ( namespace , name );
	}

	@Override
	public String toString ( ) {
		return String.format ( Helpers.EnUsCulture , "<%f, %f, %f, %f>" , this.R , this.G , this.B , this.A );
	}

	public String ToRGBString ( ) {
		return String.format ( Helpers.EnUsCulture , "<%f, %f, %f>" , this.R , this.G , this.B );
	}

	@Override
	public boolean equals ( final Object obj ) {
		return null != obj && obj instanceof Color4 && this.equals ( ( Color4 ) obj );
	}

	public boolean equals ( final Color4 other ) {
		return null != other && this.R == other.R && this.G == other.G && this.B == other.B && this.A == other.A;
	}

	@Override
	public int hashCode ( ) {
		int hashCode = ( ( Float ) this.R ).hashCode ( );
		hashCode = hashCode * 31 + ( ( Float ) this.G ).hashCode ( );
		hashCode = hashCode * 31 + ( ( Float ) this.B ).hashCode ( );
		hashCode = hashCode * 31 + ( ( Float ) this.A ).hashCode ( );
		return hashCode;
	}
}
