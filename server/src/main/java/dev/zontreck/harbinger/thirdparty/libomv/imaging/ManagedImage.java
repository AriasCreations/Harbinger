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
package dev.zontreck.harbinger.thirdparty.libomv.imaging;

import dev.zontreck.harbinger.thirdparty.libomv.types.Vector3;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ManagedImage implements Cloneable {
	// Red channel data
	protected byte[] red;
	// Green channel data
	protected byte[] green;
	// Blue channel data
	protected byte[] blue;
	// Alpha channel data
	protected byte[] alpha;
	// Bump channel data
	protected byte[] bump;
	// Image width
	private int width;
	// Image height
	private int height;
	// Image channel flags
	private byte channels;
	// BitDepth per channel
	private int bitDepth;

	public ManagedImage ( ) {

	}

	/* Only do a shallow copy of the input image */
	public ManagedImage ( final ManagedImage image ) {
		height = image.height;
		width = image.width;
		channels = image.channels;
		bitDepth = image.bitDepth;
		alpha = image.getAlpha ( );
		bump = image.getBump ( );
		red = image.getRed ( );
		green = image.getGreen ( );
		blue = image.getBlue ( );
	}

	/**
	 * Create a new blank image
	 *
	 * @param width    width
	 * @param height   height
	 * @param channels channel flags
	 */
	public ManagedImage ( final int width , final int height , final byte channels ) {
		this.width = width;
		this.height = height;
		this.channels = channels;
		this.initialize ( );
	}

	public static ManagedImage decode ( final File file ) throws Exception {
		ManagedImage image = null;
		final String ext = Helpers.getFileExtension ( file.getName ( ) );
		final FileInputStream is = new FileInputStream ( file );
		try {
			if ( "j2k".equals ( ext ) || "jp2".equals ( ext ) ) {
				image = ManagedImage.decode ( is , ImageCodec.J2K );
			}
			else if ( "tga".equals ( ext ) ) {
				image = ManagedImage.decode ( is , ImageCodec.TGA );
			}
			else {
				final byte[] data = new byte[ 10 ];
				is.read ( data );
				is.close ( );
			}
		} finally {
			is.close ( );
		}
		return image;
	}

	public static ManagedImage decode ( final InputStream input , final ImageCodec codec ) throws Exception {
		switch ( codec ) {
			case J2K:
				return J2KImage.decode ( input );
			case TGA:
				return TGAImage.decode ( input );
			default:
				return null;
		}
	}

	public int getWidth ( ) {
		return this.width;
	}

	public int getHeight ( ) {
		return this.height;
	}

	public byte getChannels ( ) {
		return this.channels;
	}

	public int getBitDepth ( ) {
		return this.bitDepth;
	}

	public byte[] getRed ( ) {
		return this.red;
	}

	public byte getRed ( final int index ) {
		return this.red[ index ];
	}

	public void setRed ( final int pixelIdx , final byte val ) {
		if ( null != red )
			this.red[ pixelIdx ] = val;
	}

	public byte[] getGreen ( ) {
		return this.green;
	}

	public byte getGreen ( final int index ) {
		return this.green[ index ];
	}

	public void setGreen ( final int pixelIdx , final byte val ) {
		if ( null != green )
			this.green[ pixelIdx ] = val;
	}

	public byte[] getBlue ( ) {
		return this.blue;
	}

	public byte getBlue ( final int index ) {
		return this.blue[ index ];
	}

	public void setBlue ( final int pixelIdx , final byte val ) {
		if ( null != blue )
			this.blue[ pixelIdx ] = val;
	}

	public byte[] getAlpha ( ) {
		return this.alpha;
	}

	public byte getAlpha ( final int index ) {
		return this.alpha[ index ];
	}

	public void setAlpha ( final int pixelIdx , final byte val ) {
		if ( null != alpha )
			this.alpha[ pixelIdx ] = val;
	}

	public byte[] getBump ( ) {
		return this.bump;
	}

	protected void setBump ( final byte[] array ) {
		this.bump = array;
	}

	public void setBump ( final int pixelIdx , final byte val ) {
		if ( null != bump )
			this.bump[ pixelIdx ] = val;
	}

	public void clear ( ) {
		Arrays.fill ( this.red , ( byte ) 0 );
		Arrays.fill ( this.green , ( byte ) 0 );
		Arrays.fill ( this.blue , ( byte ) 0 );
		Arrays.fill ( this.alpha , ( byte ) 0 );
		Arrays.fill ( this.bump , ( byte ) 0 );
	}

	protected int initialize ( ) {
		final int n = this.width * this.height;


		if ( 0 != ( channels & ImageChannels.Gray ) ) {
			this.red = new byte[ n ];
			this.green = null;
			this.blue = null;
		}
		else if ( 0 != ( channels & ImageChannels.Color ) ) {
			this.red = new byte[ n ];
			this.green = new byte[ n ];
			this.blue = new byte[ n ];
		}

		if ( 0 != ( channels & ImageChannels.Alpha ) )
			this.alpha = new byte[ n ];
		else
			this.alpha = null;

		if ( 0 != ( channels & ImageChannels.Bump ) )
			this.bump = new byte[ n ];
		else
			this.bump = null;

		return n;
	}

	protected void deepCopy ( final ManagedImage src ) {
		// Deep copy member fields here
		if ( null != src.getAlpha ( ) ) {
			this.alpha = src.getAlpha ( ).clone ( );
		}
		else {
			this.alpha = null;
		}
		if ( null != src.getRed ( ) ) {
			this.red = src.getRed ( ).clone ( );
		}
		else {
			this.red = null;
		}
		if ( null != src.getGreen ( ) ) {
			this.green = src.getGreen ( ).clone ( );
		}
		else {
			this.green = null;
		}
		if ( null != src.getBlue ( ) ) {
			this.blue = src.getBlue ( ).clone ( );
		}
		else {
			this.blue = null;
		}
		if ( null != src.getBump ( ) ) {
			this.bump = src.getBump ( ).clone ( );
		}
		else {
			this.bump = null;
		}
	}

	/**
	 * Convert the channels in the image. Channels are created or destroyed as required.
	 *
	 * @param channels new channel flags
	 */
	public void convertChannels ( final byte channels ) {
		if ( this.channels == channels )
			return;

		final int n = width * height;
		final byte add = ( byte ) ( this.channels ^ channels & channels );
		final byte del = ( byte ) ( this.channels ^ channels & this.channels );

		if ( 0 != ( add & ImageChannels.Color ) ) {
			this.red = new byte[ n ];
			this.green = new byte[ n ];
			this.blue = new byte[ n ];
		}
		else if ( 0 != ( del & ImageChannels.Color ) ) {
			this.red = null;
			this.green = null;
			this.blue = null;
		}

		if ( 0 != ( add & ImageChannels.Alpha ) ) {
			this.alpha = new byte[ n ];
			Arrays.fill ( getAlpha ( ) , ( byte ) 255 );
		}
		else if ( 0 != ( del & ImageChannels.Alpha ) )
			this.alpha = null;

		if ( 0 != ( add & ImageChannels.Bump ) ) {
			this.bump = new byte[ n ];
		}
		else if ( 0 != ( del & ImageChannels.Bump ) ) {
			this.bump = null;
		}

		this.channels = channels;
	}

	public ArrayList<ArrayList<Vector3>> toRows ( final boolean mirror ) {

		final ArrayList<ArrayList<Vector3>> rows = new ArrayList<ArrayList<Vector3>> ( this.height );

		final float pixScale = 1.0f / 255;

		int rowNdx, colNdx;
		int smNdx = 0;

		for ( rowNdx = 0; rowNdx < this.height ; rowNdx++ ) {
			final ArrayList<Vector3> row = new ArrayList<Vector3> ( this.width );
			for ( colNdx = 0; colNdx < this.width ; colNdx++ ) {
				if ( mirror )
					row.add ( new Vector3 ( - ( this.red[ smNdx ] * pixScale - 0.5f ) , ( this.green[ smNdx ] * pixScale - 0.5f ) , this.blue[ smNdx ] * pixScale - 0.5f ) );
				else
					row.add ( new Vector3 ( this.red[ smNdx ] * pixScale - 0.5f , this.green[ smNdx ] * pixScale - 0.5f , this.blue[ smNdx ] * pixScale - 0.5f ) );

				++ smNdx;
			}
			rows.add ( row );
		}
		return rows;
	}

	/**
	 * Resize or stretch the image using nearest neighbor (ugly) resampling
	 *
	 * @param width  widt new width
	 * @param height new height
	 */
	public void resizeNearestNeighbor ( final int width , final int height ) {
		if ( this.width == width && this.height == height )
			return;

		byte[]
				red = null,
				green = null,
				blue = null,
				alpha = null,
				bump = null;
		final int n = width * height;
		int di = 0, si;

		if ( null != this.red ) red = new byte[ n ];
		if ( null != this.green ) green = new byte[ n ];
		if ( null != this.blue ) blue = new byte[ n ];
		if ( null != this.alpha ) alpha = new byte[ n ];
		if ( null != this.bump ) bump = new byte[ n ];

		for ( int y = 0 ; y < height ; y++ ) {
			for ( int x = 0 ; x < width ; x++ ) {
				si = ( y * height / height ) * width + ( x * width / width );
				if ( null != this.red )
					red[ di ] = this.red[ si ];
				if ( null != this.green )
					green[ di ] = this.green[ si ];
				if ( null != this.blue )
					blue[ di ] = this.blue[ si ];
				if ( null != this.alpha )
					alpha[ di ] = this.alpha[ si ];
				if ( null != this.bump )
					bump[ di ] = this.bump[ si ];
				di++;
			}
		}

		this.width = width;
		this.height = height;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.bump = bump;
	}

	@Override
	public ManagedImage clone ( ) throws CloneNotSupportedException {
		final ManagedImage clone;
		clone = ( ManagedImage ) super.clone ( );

		// Deep copy member fields here
		this.deepCopy ( clone );
		return clone;
	}

	/**
	 * Saves the image data into an output stream with whatever encoding this object supports
	 * <p>
	 * Note: This method does currently nothing as it does not support a native raw image format
	 * This method should be overwritten by derived classes to save the image data with whatever
	 * default options makes most sense for the image format.
	 *
	 * @param os Stream in which to write the image data
	 * @return number of bytes written into the stream or -1 on error
	 * @throws Exception
	 */
	protected int encode ( final OutputStream os ) throws Exception {
		return 0;
	}

	/**
	 * Saves the image data into an output stream with whatever encoding this object supports
	 * <p>
	 * Note: This method does currently nothing as it does not support a native raw image format
	 * This method should be overwritten by derived classes to save the image data with whatever
	 * default options makes most sense for the image format.
	 *
	 * @param os Stream in which to write the image data
	 * @return number of bytes written into the stream or -1 on error
	 * @throws Exception
	 */
	public int encode ( final OutputStream os , final ImageCodec codec ) throws Exception {
		switch ( codec ) {
			case J2K:
				return J2KImage.encode ( os , this , false );
			case TGA:
				return TGAImage.encode ( os , this );
			default:
				return this.encode ( os );
		}
	}

	public enum ImageCodec {
		Invalid, RGB, J2K, BMP, TGA, JPEG, DXT, PNG;

		public static byte getValue ( final ImageCodec value ) {
			return ( byte ) value.ordinal ( );
		}

		public byte getValue ( ) {
			return ( byte ) this.ordinal ( );
		}

		public static ImageCodec setValue ( final int value ) {
			return ImageCodec.values ( )[ value ];
		}
	}

	public enum ImageResizeAlgorithm {
		NearestNeighbor
	}

	// [Flags]
	public class ImageChannels {
		public static final byte Gray = 1;
		public static final byte Color = 2;
		public static final byte Alpha = 4;
		public static final byte Bump = 8;
		private byte _value;

		public byte getValue ( ) {
			return this._value;
		}

		public void setValue ( final int value ) {
			this._value = ( byte ) value;
		}
	}
}
