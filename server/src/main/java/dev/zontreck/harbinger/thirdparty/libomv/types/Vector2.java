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
import dev.zontreck.harbinger.thirdparty.libomv.utils.RefObject;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParser;
import dev.zontreck.harbinger.thirdparty.v1.XmlPullParserException;
import dev.zontreck.harbinger.thirdparty.v1.XmlSerializer;
import org.apache.commons.io.input.SwappedDataInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * A two-dimensional vector with floating-point values
 */
public final class Vector2 {
	/**
	 * A vector with a value of 0,0
	 */
	public static final Vector2 Zero = new Vector2 ( );
	/**
	 * A vector with a value of 1,1
	 */
	public static final Vector2 One = new Vector2 ( 1.0f , 1.0f );
	/**
	 * A vector with a value of 1,0
	 */
	public static final Vector2 UnitX = new Vector2 ( 1.0f , 0.0f );
	/**
	 * A vector with a value of 0,1
	 */
	public static final Vector2 UnitY = new Vector2 ( 0.0f , 1.0f );
	/**
	 * X value
	 */
	public float X;
	/**
	 * Y value
	 */
	public float Y;

	/**
	 * Simple Constructors
	 */
	public Vector2 ( ) {
		this.X = this.Y = 0.0f;
	}

	public Vector2 ( final float x , final float y ) {
		this.X = x;
		this.Y = y;
	}

	public Vector2 ( final float value ) {
		this.X = value;
		this.Y = value;
	}

	public Vector2 ( final Vector2 vector ) {
		this.X = vector.X;
		this.Y = vector.Y;
	}

	/**
	 * Constructor, builds a vector from an XML reader
	 *
	 * @param parser XML pull parser reader
	 */
	public Vector2 ( final XmlPullParser parser ) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Quaternion
		final int eventType = parser.getEventType ( );
		if ( XmlPullParser.START_TAG != eventType )
			throw new XmlPullParserException ( "Unexpected Tag event " + eventType + " for tag name " + parser.getName ( ) , parser , null );

		while ( XmlPullParser.START_TAG == parser.nextTag ( ) ) {
			final String name = parser.getName ( );
			if ( "X".equals ( name ) ) {
				this.X = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else if ( "Y".equals ( name ) ) {
				this.Y = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else {
				Helpers.skipElement ( parser );
			}
		}
	}

	/**
	 * Constructor, builds a vector from a data stream
	 *
	 * @param is Data stream to read the binary data from
	 * @throws IOException
	 */
	public Vector2 ( final DataInputStream is ) throws IOException {
		this.X = this.Y = 0.0f;
		this.fromBytes ( is );
	}

	public Vector2 ( final SwappedDataInputStream is ) throws IOException {
		this.X = this.Y = 0.0f;
		this.fromBytes ( is );
	}

	/**
	 * Constructor, builds a vector from a byte array
	 *
	 * @param byteArray Byte array containing two four-byte floats
	 * @param pos       Beginning position in the byte array
	 */
	public Vector2 ( final byte[] byteArray , final int pos ) {
		this.X = this.Y = 0.0f;
		this.fromBytes ( byteArray , pos , false );
	}

	public Vector2 ( final byte[] byteArray , final int pos , final boolean le ) {
		this.X = this.Y = 0.0f;
		this.fromBytes ( byteArray , pos , le );
	}

	/**
	 * Constructor, builds a vector from a byte array
	 *
	 * @param byteArray ByteBuffer containing three four-byte floats
	 */
	public Vector2 ( final ByteBuffer byteArray ) {
		this.X = byteArray.getFloat ( );
		this.Y = byteArray.getFloat ( );
	}

	public static Vector2 parse ( final XmlPullParser parser ) throws XmlPullParserException, IOException {
		return new Vector2 ( parser );
	}

	public static boolean isZero ( final Vector2 v ) {
		if ( null != v )
			return v.equals ( Vector2.Zero );
		return false;
	}

	public static boolean isZeroOrNull ( final Vector2 v ) {
		if ( null != v )
			return v.equals ( Vector2.Zero );
		return true;
	}

	public static Vector2 clamp ( final Vector2 value1 , final Vector2 min , final Vector2 max ) {
		return new Vector2 ( Helpers.Clamp ( value1.X , min.X , max.X ) , Helpers.Clamp ( value1.Y , min.Y , max.Y ) );
	}

	public static float distance ( final Vector2 value1 , final Vector2 value2 ) {
		return ( float ) Math.sqrt ( Vector2.distanceSquared ( value1 , value2 ) );
	}

	public static float distanceSquared ( final Vector2 value1 , final Vector2 value2 ) {
		return ( value1.X - value2.X ) * ( value1.X - value2.X ) + ( value1.Y - value2.Y ) * ( value1.Y - value2.Y );
	}

	public static float dot ( final Vector2 value1 , final Vector2 value2 ) {
		return value1.X * value2.X + value1.Y * value2.Y;
	}

	public static Vector2 lerp ( final Vector2 value1 , final Vector2 value2 , final float amount ) {
		return new Vector2 ( Helpers.Lerp ( value1.X , value2.X , amount ) , Helpers.Lerp ( value1.Y , value2.Y , amount ) );
	}

	public static Vector2 max ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( Math.max ( value1.X , value2.X ) , Math.max ( value1.Y , value2.Y ) );
	}

	public static Vector2 min ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( Math.min ( value1.X , value2.X ) , Math.min ( value1.Y , value2.Y ) );
	}

	public static Vector2 normalize ( final Vector2 value ) {
		return new Vector2 ( value ).normalize ( );
	}

	/**
	 * Interpolates between two vectors using a cubic equation
	 */
	public static Vector2 smoothStep ( final Vector2 value1 , final Vector2 value2 , final float amount ) {
		return new Vector2 ( Helpers.SmoothStep ( value1.X , value2.X , amount ) , Helpers.SmoothStep ( value1.Y , value2.Y ,
				amount
		) );
	}

	public static Vector2 transform ( final Vector2 position , final Matrix4 matrix ) {
		return new Vector2 (
				( position.X * matrix.M11 ) + ( position.Y * matrix.M21 ) + matrix.M41 ,
				( position.X * matrix.M12 ) + ( position.Y * matrix.M22 ) + matrix.M42
		);
	}

	public static Vector2 transformNormal ( final Vector2 position , final Matrix4 matrix ) {
		return new Vector2 (
				( position.X * matrix.M11 ) + ( position.Y * matrix.M21 ) ,
				( position.X * matrix.M12 ) + ( position.Y * matrix.M22 )
		);
	}

	/**
	 * Parse a vector from a string
	 *
	 * @param val A string representation of a 2D vector, enclosed in arrow
	 *            brackets and separated by commas
	 */
	public static Vector2 Parse ( final String val ) {
		final String splitChar = ",";
		final String[] split = val.replace ( "<" , "" ).replace ( ">" , "" ).split ( splitChar );
		return new Vector2 ( Float.parseFloat ( split[ 0 ].trim ( ) ) , Float.parseFloat ( split[ 1 ].trim ( ) ) );
	}

	public static boolean TryParse ( final String val , final RefObject<Vector2> result ) {
		try {
			result.argvalue = Vector2.Parse ( val );
			return true;
		} catch ( final Throwable t ) {
			result.argvalue = Vector2.Zero;
			return false;
		}
	}

	public static Vector2 negate ( final Vector2 value ) {
		return new Vector2 ( value ).negate ( );
	}

	public static Vector2 add ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1 ).add ( value2 );
	}

	public static Vector2 subtract ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1 ).subtract ( value2 );
	}

	public static Vector2 multiply ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1 ).multiply ( value2 );
	}

	public static Vector2 multiply ( final Vector2 value1 , final float scaleFactor ) {
		return new Vector2 ( value1 ).multiply ( scaleFactor );
	}

	public static Vector2 divide ( final Vector2 value1 , final Vector2 value2 ) {
		return new Vector2 ( value1 ).divide ( value2 );
	}

	public static Vector2 divide ( final Vector2 value1 , final float divider ) {
		return new Vector2 ( value1 ).divide ( divider );
	}

	/**
	 * Returns the raw bytes for this vector
	 *
	 * @return An eight-byte array containing X and Y
	 */
	public byte[] getBytes ( ) {
		final byte[] byteArray = new byte[ 8 ];
		this.toBytes ( byteArray , 0 , false );
		return byteArray;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 *
	 * @param byteArray Buffer to copy the 8 bytes for X and Y
	 */
	public void write ( final ByteBuffer byteArray ) {
		byteArray.putFloat ( this.X );
		byteArray.putFloat ( this.Y );
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 *
	 * @param stream OutputStream to copy the 8 bytes for X and Y
	 * @param le     True for writing little endian data
	 * @throws IOException
	 */
	public void write ( final OutputStream stream , final boolean le ) throws IOException {
		if ( le ) {
			stream.write ( Helpers.FloatToBytesL ( this.X ) );
			stream.write ( Helpers.FloatToBytesL ( this.Y ) );
		}
		else {
			stream.write ( Helpers.FloatToBytesB ( this.X ) );
			stream.write ( Helpers.FloatToBytesB ( this.Y ) );
		}
	}

	public void serializeXml ( final XmlSerializer writer , final String namespace , final String name ) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag ( namespace , name );
		writer.startTag ( namespace , "X" ).text ( Float.toString ( this.X ) ).endTag ( namespace , "X" );
		writer.startTag ( namespace , "Y" ).text ( Float.toString ( this.Y ) ).endTag ( namespace , "Y" );
		writer.endTag ( namespace , name );
	}

	public void serializeXml ( final XmlSerializer writer , final String namespace , final String name , final Locale locale ) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag ( namespace , name );
		writer.startTag ( namespace , "X" ).text ( String.format ( locale , "%f" , this.X ) ).endTag ( namespace , "X" );
		writer.startTag ( namespace , "Y" ).text ( String.format ( locale , "%f" , this.Y ) ).endTag ( namespace , "Y" );
		writer.endTag ( namespace , name );
	}

	/**
	 * Get a formatted string representation of the vector
	 *
	 * @return A string representation of the vector
	 */
	@Override
	public String toString ( ) {
		return String.format ( Helpers.EnUsCulture , "<%f, %f>" , this.X , this.Y );
	}

	/**
	 * Get a string representation of the vector elements with up to three
	 * decimal digits and separated by spaces only
	 *
	 * @return Raw string representation of the vector
	 */
	public String toRawString ( ) {
		return String.format ( Helpers.EnUsCulture , "%.3f, %.3f" , this.X , this.Y );
	}

	/**
	 * Creates a hash code for the vector
	 */
	@Override
	public int hashCode ( ) {
		return ( ( Float ) this.X ).hashCode ( ) * 31 + ( ( Float ) this.Y ).hashCode ( );
	}

	/**
	 * Builds a vector from a byte array
	 *
	 * @param byteArray Byte array containing a 12 byte vector
	 * @param pos       Beginning position in the byte array
	 * @param le        is the byte array in little endian format
	 */
	public void fromBytes ( final byte[] bytes , final int pos , final boolean le ) {
		if ( le ) {
			/* Little endian architecture */
			this.X = Helpers.BytesToFloatL ( bytes , pos );
			this.Y = Helpers.BytesToFloatL ( bytes , pos + 4 );
		}
		else {
			this.X = Helpers.BytesToFloatB ( bytes , pos );
			this.Y = Helpers.BytesToFloatB ( bytes , pos + 4 );
		}
	}

	/**
	 * Builds a vector from a data stream
	 *
	 * @param is DataInputStream to read the vector from
	 * @throws IOException
	 */
	public void fromBytes ( final DataInputStream is ) throws IOException {
		this.X = is.readFloat ( );
		this.Y = is.readFloat ( );
	}

	public void fromBytes ( final SwappedDataInputStream is ) throws IOException {
		this.X = is.readFloat ( );
		this.Y = is.readFloat ( );
	}

	/**
	 * Writes the raw bytes for this vector to a byte array
	 *
	 * @param dest Destination byte array
	 * @param pos  Position in the destination array to start writing. Must be at
	 *             least 12 bytes before the end of the array
	 */
	public void toBytes ( final byte[] dest , final int pos , final boolean le ) {
		if ( le ) {
			Helpers.FloatToBytesL ( this.X , dest , pos );
			Helpers.FloatToBytesL ( this.Y , dest , pos + 4 );
		}
		else {
			Helpers.FloatToBytesB ( this.X , dest , pos );
			Helpers.FloatToBytesB ( this.Y , dest , pos + 4 );
		}
	}

	public float length ( ) {
		return ( float ) Math.sqrt ( Vector2.distanceSquared ( this , Vector2.Zero ) );
	}

	public float lengthSquared ( ) {
		return Vector2.distanceSquared ( this , Vector2.Zero );
	}

	public Vector2 normalize ( ) {
		final float length = this.length ( );
		if ( Helpers.FLOAT_MAG_THRESHOLD < length ) {
			return this.divide ( length );
		}
		this.X = 0.0f;
		this.Y = 0.0f;
		return this;
	}

	/**
	 * Test if this vector is equal to another vector, within a given tolerance
	 * range
	 *
	 * @param vec       Vector to test against
	 * @param tolerance The acceptable magnitude of difference between the two vectors
	 * @return True if the magnitude of difference between the two vectors is
	 * less than the given tolerance, otherwise false
	 */
	public boolean approxEquals ( final Vector2 vec , final float tolerance ) {
		final Vector2 diff = this.subtract ( vec );
		return ( diff.lengthSquared ( ) <= tolerance * tolerance );
	}

	public int compareTo ( final Vector2 vector ) {
		return ( ( Float ) this.length ( ) ).compareTo ( vector.length ( ) );
	}

	/**
	 * Test if this vector is composed of all finite numbers
	 */
	public boolean isFinite ( ) {
		return Helpers.IsFinite ( this.X ) && Helpers.IsFinite ( this.Y );
	}

	public boolean isZero ( ) {
		return this.equals ( Vector2.Zero );
	}

	public boolean equals ( final Vector3 val ) {
		return null != val && this.X == val.X && this.Y == val.Y;
	}

	@Override
	public boolean equals ( final Object obj ) {
		return null != obj && ( obj instanceof Vector2 ) && this.equals ( ( Vector2 ) obj );
	}

	public boolean equals ( final Vector2 o ) {
		return null != o && o.X == this.X && o.Y == this.Y;
	}

	public Vector2 flip ( ) {
		this.X = 1.0f - this.X;
		this.Y = 1.0f - this.Y;
		return this;
	}

	public Vector2 negate ( ) {
		this.X = - this.X;
		this.Y = - this.Y;
		return this;
	}

	public Vector2 add ( final Vector2 value ) {
		this.X += value.X;
		this.Y += value.Y;
		return this;
	}

	public Vector2 subtract ( final Vector2 value ) {
		this.X -= value.X;
		this.Y -= value.X;
		return this;
	}

	public Vector2 multiply ( final Vector2 value ) {
		this.X *= value.X;
		this.Y *= value.Y;
		return this;
	}

	public Vector2 multiply ( final float scaleFactor ) {
		this.X *= scaleFactor;
		this.Y *= scaleFactor;
		return this;
	}

	public Vector2 divide ( final Vector2 value ) {
		this.X /= value.X;
		this.Y /= value.Y;
		return this;
	}

	public Vector2 divide ( final float divider ) {
		final float factor = 1 / divider;
		this.X *= factor;
		this.Y *= factor;
		return this;
	}
}
