/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
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

public class Vector3 {
	/**
	 * A vector with a value of 0,0,0
	 */
	public static final Vector3 Zero = new Vector3 ( 0.0f );
	/**
	 * A vector with a value of 1,1,1
	 */
	public static final Vector3 One = new Vector3 ( 1.0f , 1.0f , 1.0f );
	/**
	 * A unit vector facing forward (X axis), value 1,0,0
	 */
	public static final Vector3 UnitX = new Vector3 ( 1.0f , 0.0f , 0.0f );
	/**
	 * A unit vector facing left (Y axis), value 0,1,0
	 */
	public static final Vector3 UnitY = new Vector3 ( 0.0f , 1.0f , 0.0f );
	/**
	 * A unit vector facing up (Z axis), value 0,0,1
	 */
	public static final Vector3 UnitZ = new Vector3 ( 0.0f , 0.0f , 1.0f );
	public float X;
	public float Y;
	public float Z;

	public Vector3 ( final float val ) {
		this.X = this.Y = this.Z = val;
	}

	public Vector3 ( final float[] arr ) {
		Vector3.fromArray ( this , arr , 0 );
	}

	public Vector3 ( final float[] arr , final int offset ) {
		Vector3.fromArray ( this , arr , offset );
	}

	public Vector3 ( final Vector3 v ) {
		this.X = v.X;
		this.Y = v.Y;
		this.Z = v.Z;
	}

	public Vector3 ( final Vector3d vector ) {
		this.X = ( float ) vector.X;
		this.Y = ( float ) vector.Y;
		this.Z = ( float ) vector.Z;
	}

	public Vector3 ( final ByteBuffer byteArray ) {
		this.X = byteArray.getFloat ( );
		this.Y = byteArray.getFloat ( );
		this.Z = byteArray.getFloat ( );
	}

	/**
	 * Constructor, builds a vector from an XML reader
	 *
	 * @param parser XML pull parser reader
	 */
	public Vector3 ( final XmlPullParser parser ) throws XmlPullParserException, IOException {
		// entering with event on START_TAG for the tag name identifying the Vector3
		final int eventType = parser.getEventType ( );
		if ( XmlPullParser.START_TAG != eventType )
			throw new XmlPullParserException ( "Unexpected Tag event " + eventType + " for tag name " + parser.getName ( ) , parser , null );

		while ( XmlPullParser.START_TAG == parser.nextTag ( ) ) {
			final String name = parser.getName ( );
			if ( "X".equalsIgnoreCase ( name ) ) {
				this.X = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else if ( "Y".equalsIgnoreCase ( name ) ) {
				this.Y = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else if ( "Z".equalsIgnoreCase ( name ) ) {
				this.Z = Helpers.TryParseFloat ( parser.nextText ( ).trim ( ) );
			}
			else {
				Helpers.skipElement ( parser );
			}
		}
	}

/*
  	public Vector3(String value)
 
	{
		// TODO Auto-generated constructor stub
	}
*/

	/**
	 * Constructor, builds a vector from a data stream
	 *
	 * @param is Data stream to read the binary data from
	 * @throws IOException
	 */
	public Vector3 ( final DataInputStream is ) throws IOException {
		this.X = this.Y = this.Z = 0.0f;
		this.fromBytes ( is );
	}

	public Vector3 ( final SwappedDataInputStream is ) throws IOException {
		this.X = this.Y = this.Z = 0.0f;
		this.fromBytes ( is );
	}

	/**
	 * Constructor, builds a vector from a byte array
	 *
	 * @param byteArray Byte array containing three four-byte floats
	 * @param pos       Beginning position in the byte array
	 * @param le        is the byte array in little endian format
	 */
	public Vector3 ( final byte[] byteArray , final int pos ) {
		this.X = this.Y = this.Z = 0.0f;
		this.fromBytes ( byteArray , pos , false );
	}

	public Vector3 ( final byte[] byteArray , final int pos , final boolean le ) {
		this.X = this.Y = this.Z = 0.0f;
		this.fromBytes ( byteArray , pos , le );
	}

	public Vector3 ( final float x , final float y , final float z ) {
		this.X = x;
		this.Y = y;
		this.Z = z;
	}

	public static Vector3 parse ( final XmlPullParser parser ) throws XmlPullParserException, IOException {
		return new Vector3 ( parser );
	}

	/**
	 * Initializes a vector from a flaot array
	 *
	 * @param vec The vector to intialize
	 * @param arr Is the float array
	 * @param pos Beginning position in the float array
	 */
	public static Vector3 fromArray ( final Vector3 vec , final float[] arr , final int pos ) {
		if ( arr.length >= ( pos + 3 ) ) {
			vec.X = arr[ pos ];
			vec.Y = arr[ pos + 1 ];
			vec.Z = arr[ pos + 2 ];
		}
		return vec;
	}

	public static boolean isZero ( final Vector3 v ) {
		if ( null != v )
			return v.equals ( Vector3.Zero );
		return false;
	}

	public static boolean isZeroOrNull ( final Vector3 v ) {
		if ( null != v )
			return v.equals ( Vector3.Zero );
		return true;
	}

	public static Vector3 cross ( final Vector3 value1 , final Vector3 value2 ) {
		return new Vector3 ( value1 ).cross ( value2 );
	}

	public static float distance ( final Vector3 value1 , final Vector3 value2 ) {
		return ( float ) Math.sqrt ( Vector3.distanceSquared ( value1 , value2 ) );
	}

	public static float distanceSquared ( final Vector3 value1 , final Vector3 value2 ) {
		return ( value1.X - value2.X ) * ( value1.X - value2.X )
				+ ( value1.Y - value2.Y ) * ( value1.Y - value2.Y )
				+ ( value1.Z - value2.Z ) * ( value1.Z - value2.Z );
	}

	public static float dot ( final Vector3 value1 , final Vector3 value2 ) {
		return value1.X * value2.X + value1.Y * value2.Y + value1.Z * value2.Z;
	}

	public static Vector3 lerp ( final Vector3 value1 , final Vector3 value2 , final float amount ) {

		return new Vector3 (
				Helpers.Lerp ( value1.X , value2.X , amount ) ,
				Helpers.Lerp ( value1.Y , value2.Y , amount ) ,
				Helpers.Lerp ( value1.Z , value2.Z , amount )
		);
	}

	public static float mag ( final Vector3 value ) {
		return ( float ) Math.sqrt ( ( value.X * value.X ) + ( value.Y * value.Y ) + ( value.Z * value.Z ) );
	}

	public static Vector3 max ( final Vector3 value1 , final Vector3 value2 ) {
		return new Vector3 ( Math.max ( value1.X , value2.X ) , Math.max ( value1.Y , value2.Y ) , Math.max ( value1.Z , value2.Z ) );
	}

	public static Vector3 min ( final Vector3 value1 , final Vector3 value2 ) {
		return new Vector3 ( Math.min ( value1.X , value2.X ) , Math.min ( value1.Y , value2.Y ) , Math.min ( value1.Z , value2.Z ) );
	}

	public static Vector3 normalize ( final Vector3 value ) {
		return new Vector3 ( value ).normalize ( );
	}

	public static Vector3 clamp ( final Vector3 value , final Vector3 min , final Vector3 max ) {
		return new Vector3 ( value ).clamp ( min , max );
	}

	public static Vector3 clamp ( final Vector3 value , final float min , final float max ) {
		return new Vector3 ( value ).clamp ( min , max );
	}

	/**
	 * Calculate the rotation between two vectors
	 *
	 * @param a Normalized directional vector (such as 1,0,0 for forward
	 *          facing)
	 * @param b Normalized target vector
	 */
	public static Quaternion rotationBetween ( final Vector3 a , final Vector3 b ) {
		final float dotProduct = Vector3.dot ( a , b );
		final Vector3 crossProduct = Vector3.cross ( a , b );
		final float magProduct = a.length ( ) * b.length ( );
		final double angle = Math.acos ( dotProduct / magProduct );
		final Vector3 axis = crossProduct.normalize ( );
		final float s = ( float ) Math.sin ( angle / 2.0d );

		return new Quaternion ( axis.X * s , axis.Y * s , axis.Z * s , ( float ) Math.cos ( angle / 2.0d ) );
	}

	/**
	 * Interpolates between two vectors using a cubic equation
	 */
	public static Vector3 smoothStep ( final Vector3 value1 , final Vector3 value2 , final float amount ) {
		return new Vector3 ( Helpers.SmoothStep ( value1.X , value2.X , amount ) , Helpers.SmoothStep ( value1.Y , value2.Y ,
				amount
		) , Helpers.SmoothStep ( value1.Z , value2.Z , amount ) );
	}

	public static Vector3 transform ( final Vector3 position , final Matrix4 matrix ) {
		return new Vector3 ( ( position.X * matrix.M11 ) + ( position.Y * matrix.M21 ) + ( position.Z * matrix.M31 )
				+ matrix.M41 , ( position.X * matrix.M12 ) + ( position.Y * matrix.M22 ) + ( position.Z * matrix.M32 )
				+ matrix.M42 , ( position.X * matrix.M13 ) + ( position.Y * matrix.M23 ) + ( position.Z * matrix.M33 )
				+ matrix.M43 );
	}

	public static Vector3 transformNormal ( final Vector3 position , final Matrix4 matrix ) {
		return new Vector3 (
				( position.X * matrix.M11 ) + ( position.Y * matrix.M21 ) + ( position.Z * matrix.M31 ) ,
				( position.X * matrix.M12 ) + ( position.Y * matrix.M22 ) + ( position.Z * matrix.M32 ) ,
				( position.X * matrix.M13 ) + ( position.Y * matrix.M23 ) + ( position.Z * matrix.M33 )
		);
	}

	/**
	 * Parse a vector from a string
	 *
	 * @param val A string representation of a 3D vector, enclosed in arrow
	 *            brackets and separated by commas
	 */
	public static Vector3 Parse ( final String val ) {
		final String splitChar = ",";
		final String[] split = val.replace ( "<" , "" ).replace ( ">" , "" ).split ( splitChar );
		return new Vector3 ( Float.parseFloat ( split[ 0 ].trim ( ) ) , Float.parseFloat ( split[ 1 ].trim ( ) ) ,
				Float.parseFloat ( split[ 2 ].trim ( ) )
		);
	}

	public static Vector3 TryParse ( final String val ) {
		try {
			return Vector3.Parse ( val );
		} catch ( final Throwable t ) {
			return Vector3.Zero;
		}
	}

	public static boolean TryParse ( final String val , final RefObject<Vector3> result ) {
		try {
			result.argvalue = Vector3.Parse ( val );
			return true;
		} catch ( final Throwable t ) {
			result.argvalue = Vector3.Zero;
			return false;
		}
	}

	public static Vector3 negate ( final Vector3 value ) {
		return new Vector3 ( value ).negate ( );
	}

	public static Vector3 add ( final Vector3 val1 , final Vector3 val2 ) {
		return new Vector3 ( val1 ).add ( val2 );
	}

	public static Vector3 subtract ( final Vector3 val1 , final Vector3 val2 ) {
		return new Vector3 ( val1 ).subtract ( val2 );
	}

	public static Vector3 multiply ( final Vector3 value1 , final Vector3 value2 ) {
		return new Vector3 ( value1 ).multiply ( value2 );
	}

	public static Vector3 multiply ( final Vector3 value1 , final float scaleFactor ) {
		return new Vector3 ( value1 ).multiply ( scaleFactor );
	}

	public static Vector3 multiply ( final Vector3 vec , final Quaternion rot ) {
		return new Vector3 ( vec ).multiply ( rot );
	}

	public static Vector3 multiply ( final Vector3 vector , final Matrix4 matrix ) {
		return Vector3.transform ( vector , matrix );
	}

	public static Vector3 divide ( final Vector3 value1 , final Vector3 value2 ) {
		return new Vector3 ( value1 ).divide ( value2 );
	}

	public static Vector3 divide ( final Vector3 value , final float divider ) {
		return new Vector3 ( value ).divide ( divider );
	}

	/**
	 * Returns the raw bytes for this vector
	 *
	 * @return An eight-byte array containing X and Y
	 */
	public byte[] getBytes ( ) {
		final byte[] byteArray = new byte[ 12 ];
		this.toBytes ( byteArray , 0 , false );
		return byteArray;
	}

	/**
	 * Writes the raw data for this vector to a ByteBuffer
	 *
	 * @param byteArray buffer to copy the 12 bytes for X, Y, and Z
	 * @param le        True for writing little endian data
	 * @throws IOException
	 */
	public void write ( final ByteBuffer byteArray ) {
		byteArray.putFloat ( this.X );
		byteArray.putFloat ( this.Y );
		byteArray.putFloat ( this.Z );
	}

	/**
	 * Writes the raw data for this vector to a OutputStream
	 *
	 * @param stream OutputStream to copy the 12 bytes for X, Y, and Z
	 * @param le     True for writing little endian data
	 * @throws IOException
	 */
	public void write ( final OutputStream stream , final boolean le ) throws IOException {
		if ( le ) {
			stream.write ( Helpers.FloatToBytesL ( this.X ) );
			stream.write ( Helpers.FloatToBytesL ( this.Y ) );
			stream.write ( Helpers.FloatToBytesL ( this.Z ) );
		}
		else {
			stream.write ( Helpers.FloatToBytesB ( this.X ) );
			stream.write ( Helpers.FloatToBytesB ( this.Y ) );
			stream.write ( Helpers.FloatToBytesB ( this.Z ) );
		}
	}

	public void serializeXml ( final XmlSerializer writer , final String namespace , final String name ) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag ( namespace , name );
		writer.startTag ( namespace , "X" ).text ( Float.toString ( this.X ) ).endTag ( namespace , "X" );
		writer.startTag ( namespace , "Y" ).text ( Float.toString ( this.Y ) ).endTag ( namespace , "Y" );
		writer.startTag ( namespace , "Z" ).text ( Float.toString ( this.Z ) ).endTag ( namespace , "Z" );
		writer.endTag ( namespace , name );
	}

	public void serializeXml ( final XmlSerializer writer , final String namespace , final String name , final Locale locale ) throws IllegalArgumentException, IllegalStateException, IOException {
		writer.startTag ( namespace , name );
		writer.startTag ( namespace , "X" ).text ( String.format ( locale , "%f" , this.X ) ).endTag ( namespace , "X" );
		writer.startTag ( namespace , "Y" ).text ( String.format ( locale , "%f" , this.Y ) ).endTag ( namespace , "Y" );
		writer.startTag ( namespace , "Z" ).text ( String.format ( locale , "%f" , this.Z ) ).endTag ( namespace , "Z" );
		writer.endTag ( namespace , name );
	}

	@Override
	public String toString ( ) {
		return String.format ( Helpers.EnUsCulture , "<%.3f, %.3f, %.3f>" , this.X , this.Y , this.Z );
	}

	@Override
	public int hashCode ( ) {
		return ( ( Float ) this.X ).hashCode ( ) * 31 * 31 + ( ( Float ) this.Y ).hashCode ( ) * 31 + ( ( Float ) this.Z ).hashCode ( );
	}

	/**
	 * Builds a vector from a byte array
	 *
	 * @param byteArray Byte array containing a 12 byte vector
	 * @param pos       Beginning position in the byte array
	 * @param le        Is the byte array in little endian format
	 */
	public void fromBytes ( final byte[] bytes , final int pos , final boolean le ) {
		if ( le ) {
			/* Little endian architecture */
			this.X = Helpers.BytesToFloatL ( bytes , pos );
			this.Y = Helpers.BytesToFloatL ( bytes , pos + 4 );
			this.Z = Helpers.BytesToFloatL ( bytes , pos + 8 );
		}
		else {
			this.X = Helpers.BytesToFloatB ( bytes , pos );
			this.Y = Helpers.BytesToFloatB ( bytes , pos + 4 );
			this.Z = Helpers.BytesToFloatB ( bytes , pos + 8 );
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
		this.Z = is.readFloat ( );
	}

	public void fromBytes ( final SwappedDataInputStream is ) throws IOException {
		this.X = is.readFloat ( );
		this.Y = is.readFloat ( );
		this.Z = is.readFloat ( );
	}

	/**
	 * Writes the raw bytes for this vector to a byte array
	 *
	 * @param dest Destination byte array
	 * @param pos  Position in the destination array to start writing. Must be at
	 *             least 12 bytes before the end of the array
	 */
	public void toBytes ( final byte[] dest , final int pos ) {
		this.toBytes ( dest , pos , false );
	}

	public void toBytes ( final byte[] dest , final int pos , final boolean le ) {
		if ( le ) {
			Helpers.FloatToBytesL ( this.X , dest , pos );
			Helpers.FloatToBytesL ( this.Y , dest , pos + 4 );
			Helpers.FloatToBytesL ( this.Z , dest , pos + 8 );
		}
		else {
			Helpers.FloatToBytesB ( this.X , dest , pos );
			Helpers.FloatToBytesB ( this.Y , dest , pos + 4 );
			Helpers.FloatToBytesB ( this.Z , dest , pos + 8 );
		}
	}

	public float length ( ) {
		return ( float ) Math.sqrt ( Vector3.distanceSquared ( this , Vector3.Zero ) );
	}

	public float lengthSquared ( ) {
		return Vector3.distanceSquared ( this , Vector3.Zero );
	}

	public Vector3 normalize ( ) {
		// Catch very small rounding errors when normalizing
		final float length = this.length ( );
		if ( Helpers.FLOAT_MAG_THRESHOLD < length ) {
			return this.divide ( length );
		}
		this.X = 0.0f;
		this.Y = 0.0f;
		this.Z = 0.0f;
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
	public boolean approxEquals ( final Vector3 vec , final float tolerance ) {
		final Vector3 diff = Vector3.subtract ( this , vec );
		return ( diff.lengthSquared ( ) <= tolerance * tolerance );
	}

	public int compareTo ( final Vector3 vector ) {
		return ( ( Float ) this.length ( ) ).compareTo ( vector.length ( ) );
	}

	/**
	 * Test if this vector is composed of all finite numbers
	 */
	public boolean isFinite ( ) {
		return ( Helpers.IsFinite ( this.X ) && Helpers.IsFinite ( this.Y ) && Helpers.IsFinite ( this.Z ) );
	}

	public boolean isZero ( ) {
		return this.equals ( Vector3.Zero );
	}

	public Vector3 clamp ( final Vector3 min , final Vector3 max ) {
		this.X = Helpers.Clamp ( this.X , min.X , max.X );
		this.Y = Helpers.Clamp ( this.Y , min.Y , max.Y );
		this.Z = Helpers.Clamp ( this.Z , min.Z , max.Z );
		return this;
	}

	public Vector3 clamp ( final float min , final float max ) {
		this.X = Helpers.Clamp ( this.X , min , max );
		this.Y = Helpers.Clamp ( this.Y , min , max );
		this.Z = Helpers.Clamp ( this.Z , min , max );
		return this;
	}

	public float mag ( ) {
		return Vector3.mag ( this );
	}

	@Override
	public boolean equals ( final Object obj ) {
		return null != obj && ( ( obj instanceof Vector3 ) && this.equals ( ( Vector3 ) obj ) || ( obj instanceof Vector3d ) && this.equals ( ( Vector3d ) obj ) );
	}

	public boolean equals ( final Vector3 val ) {
		return null != val && this.X == val.X && this.Y == val.Y && this.Z == val.Z;
	}

	public boolean equals ( final Vector3d val ) {
		return null != val && this.X == val.X && this.Y == val.Y && this.Z == val.Z;
	}

	public Vector3 negate ( ) {
		this.X = - this.X;
		this.Y = - this.Y;
		this.Z = - this.Z;
		return this;
	}

	public Vector3 add ( final Vector3 val ) {
		this.X += val.X;
		this.Y += val.Y;
		this.Z += val.Z;
		return this;
	}

	public Vector3 subtract ( final Vector3 val ) {
		this.X -= val.X;
		this.Y -= val.Y;
		this.Z -= val.Z;
		return this;
	}

	public Vector3 multiply ( final float scaleFactor ) {
		this.X *= scaleFactor;
		this.Y *= scaleFactor;
		this.Z *= scaleFactor;
		return this;
	}

	public Vector3 multiply ( final Vector3 value ) {
		this.X *= value.X;
		this.Y *= value.Y;
		this.Z *= value.Z;
		return this;
	}

	public Vector3 multiply ( final Quaternion rot ) {
		// From http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/transforms/
		final float x = rot.W * rot.W * this.X + 2.0f * rot.Y * rot.W * this.Z - 2.0f * rot.Z * rot.W * this.Y + rot.X * rot.X * this.X
				+ 2.0f * rot.Y * rot.X * this.Y + 2.0f * rot.Z * rot.X * this.Z - rot.Z * rot.Z * this.X - rot.Y * rot.Y * this.X;
		final float y = 2.0f * rot.X * rot.Y * this.X + rot.Y * rot.Y * this.Y + 2.0f * rot.Z * rot.Y * this.Z + 2.0f * rot.W * rot.Z * this.X
				- rot.Z * rot.Z * this.Y + rot.W * rot.W * this.Y - 2.0f * rot.X * rot.W * this.Z - rot.X * rot.X * this.Y;
		this.Z = 2.0f * rot.X * rot.Z * this.X + 2.0f * rot.Y * rot.Z * this.Y + rot.Z * rot.Z * this.Z - 2.0f * rot.W * rot.Y * this.X
				- rot.Y * rot.Y * this.Z + 2.0f * rot.W * rot.X * this.Y - rot.X * rot.X * this.Z + rot.W * rot.W * this.Z;
		this.X = x;
		this.Y = y;
		return this;
	}

	public Vector3 divide ( final Vector3 value ) {
		this.X /= value.X;
		this.Y /= value.Y;
		this.Z /= value.Z;
		return this;
	}

	public Vector3 divide ( final float divider ) {
		final float factor = 1.0f / divider;
		this.X *= factor;
		this.Y *= factor;
		this.Z *= factor;
		return this;
	}

	public Vector3 cross ( final Vector3 value ) {
		this.X = this.Y * value.Z - value.Y * this.Z;
		this.Y = this.Z * value.X - value.Z * this.X;
		this.Z = this.X * value.Y - value.X * this.Y;
		return this;
	}
}
