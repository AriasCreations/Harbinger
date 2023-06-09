/**
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

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.JJ2KExceptionHandler;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder.ImgDecoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.encoder.ImgEncoder;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.fileformat.reader.FileFormatReader;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfileException;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlk;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkInt;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.input.ImgReader;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ISRandomAccessIO;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ParameterList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public class J2KImage extends ManagedImage {
	private J2KImage ( ) {

	}

	public J2KImage ( final int width , final int height , final byte channels ) {
		super ( width , height , channels );
	}

	/* Only do a shallow copy of the input image */
	public J2KImage ( final ManagedImage image ) {
		super ( image );
	}

	/**
	 * Encode a <seealso cref="ManagedImage"/> object into a byte array
	 *
	 * @param os       The <seealso cref="OutputStream"/> to encode the image into
	 * @param image    The <seealso cref="ManagedImage"/> object to encode
	 * @param lossless true to enable lossless conversion, only useful for small images ie: sculptmaps
	 * @return number of bytes written into the stream or -1 on error
	 */
	public static int encode ( final OutputStream os , final ManagedImage image , final boolean lossless ) throws Exception {
		if ( ( 0 != ( image.getChannels ( ) & ImageChannels.Gray ) ) && ( 0 != ( image.getChannels ( ) & ImageChannels.Color ) ) ||
				( 0 != ( image.getChannels ( ) & ImageChannels.Bump ) ) && ( 0 == ( image.getChannels ( ) & ImageChannels.Alpha ) ) )
			throw new IllegalArgumentException ( "JPEG2000 encoding is not supported for this channel combination" );

		int components = 0;
		if ( 0 != ( image.getChannels ( ) & ImageChannels.Gray ) )
			components = 1;
		else if ( 0 != ( image.getChannels ( ) & ImageChannels.Color ) )
			components = 3;
		if ( 0 != ( image.getChannels ( ) & ImageChannels.Alpha ) )
			components++;
		if ( 0 != ( image.getChannels ( ) & ImageChannels.Bump ) )
			components++;

		// Initialize default parameters
		final ParameterList pl;
		final ParameterList defpl = new ParameterList ( );
		final String[][] param = ImgEncoder.getAllParameters ( );

		for ( int i = param.length - 1 ; 0 <= i ; i-- ) {
			if ( null != param[ i ][ 3 ] ) {
				defpl.put ( param[ i ][ 0 ] , param[ i ][ 3 ] );
			}
		}

		// Create parameter list using defaults
		pl = new ParameterList ( defpl );
		if ( lossless ) {
			pl.put ( "lossless" , "on" );
		}

		final ImgEncoder enc = new ImgEncoder ( pl );
		final ImgReaderMI source = new J2KImage ( ).new ImgReaderMI ( components );

		final boolean[] imsigned = new boolean[ components ];
		for ( int i = 0 ; i < components ; i++ ) {
			imsigned[ i ] = source.isOrigSigned ( i );
		}
		return enc.encode ( source , imsigned , components , false , os , true , false );
	}

	public static byte[] encode ( final ManagedImage image , final boolean lossless ) throws Exception {
		final ByteArrayOutputStream os = new ByteArrayOutputStream ( );
		final int length = J2KImage.encode ( os , image , lossless );
		if ( 0 < length )
			return os.toByteArray ( );
		return null;
	}

	public static J2KImage decode ( final InputStream is ) throws IllegalArgumentException, IOException, ICCProfileException {
		final BlkImgDataSrc dataSrc = J2KImage.decodeInternal ( is );

		final int ncomps = dataSrc.getNumComps ( );

		// Check component sizes and bit depths
		int height = dataSrc.getCompImgHeight ( 0 );
		int width = dataSrc.getCompImgWidth ( 0 );
		for ( int i = dataSrc.getNumComps ( ) - 1 ; 0 < i ; i-- ) {
			if ( dataSrc.getCompImgHeight ( i ) != height || dataSrc.getCompImgWidth ( i ) != width ) {
				throw new IllegalArgumentException ( "All components must have the same dimensions and no subsampling" );
			}
			if ( 8 < dataSrc.getNomRangeBits ( i ) ) {
				throw new IllegalArgumentException ( "Depths greater than 8 bits per component is not supported" );
			}
		}

		byte channels = 0;
		switch ( ncomps ) {
			case 5:
				channels = ManagedImage.ImageChannels.Bump;
			case 4:
				channels |= ManagedImage.ImageChannels.Alpha;
			case 3:
				channels |= ManagedImage.ImageChannels.Color;
				break;
			case 2:
				channels = ManagedImage.ImageChannels.Alpha;
			case 1:
				channels |= ManagedImage.ImageChannels.Gray;
				break;
			default:
				throw new IllegalArgumentException ( "Decoded image with unhandled number of components: " + ncomps );
		}

		final J2KImage image = new J2KImage ( height , width , channels );

		int tOffx, tOffy; // Active tile offset
		int tIdx = 0; // index of the current tile
		int off, l, x, y;
		final Coord nT = dataSrc.getNumTiles ( null );
		final DataBlkInt block = new DataBlkInt ( );
		block.ulx = 0;
		block.h = 1;

		final PixelScale[] scale = new PixelScale[ ncomps ];

		for ( int i = 0 ; i < ncomps ; i++ ) {
			scale[ i ] = image.new PixelScale ( );
			scale[ i ].ls = 1 << ( dataSrc.getNomRangeBits ( i ) - 1 );
			scale[ i ].mv = ( 1 << dataSrc.getNomRangeBits ( i ) ) - 1;
			scale[ i ].fb = dataSrc.getFixedPoint ( i );
		}

		// Start the data delivery to the cached consumers tile by tile
		for ( y = 0; y < nT.y ; y++ ) {
			// Loop on horizontal tiles
			for ( x = 0; x < nT.x ; x++ , tIdx++ ) {
				dataSrc.setTile ( x , y );

				// Initialize tile
				height = dataSrc.getTileCompHeight ( tIdx , 0 );
				width = dataSrc.getTileCompWidth ( tIdx , 0 );

				// The offset of the active tiles is the same for all components,
				// since we don't support different component dimensions.
				tOffx = dataSrc.getCompULX ( 0 ) - ( int ) Math.ceil ( dataSrc.getImgULX ( ) / ( double ) dataSrc.getCompSubsX ( 0 ) );
				tOffy = dataSrc.getCompULY ( 0 ) - ( int ) Math.ceil ( dataSrc.getImgULY ( ) / ( double ) dataSrc.getCompSubsY ( 0 ) );
				off = tOffy * image.getWidth ( ) + tOffx;

				// Deliver in lines to reduce memory usage
				for ( l = 0; l < height ; l++ ) {
					block.uly = l;
					block.w = width;

					switch ( ncomps ) {
						case 5:
							dataSrc.getInternCompData ( block , 4 );
							J2KImage.fillLine ( block , scale[ 4 ] , image.getBump ( ) , off );
						case 4:
							dataSrc.getInternCompData ( block , 3 );
							J2KImage.fillLine ( block , scale[ 3 ] , image.getAlpha ( ) , off );
						case 3:
							dataSrc.getInternCompData ( block , 2 );
							J2KImage.fillLine ( block , scale[ 2 ] , image.getBlue ( ) , off );
							dataSrc.getInternCompData ( block , 1 );
							J2KImage.fillLine ( block , scale[ 1 ] , image.getGreen ( ) , off );
							dataSrc.getInternCompData ( block , 0 );
							J2KImage.fillLine ( block , scale[ 0 ] , image.getRed ( ) , off );
							break;
						case 2:
							dataSrc.getInternCompData ( block , 1 );
							J2KImage.fillLine ( block , scale[ 1 ] , image.getAlpha ( ) , off );
						case 1:
							dataSrc.getInternCompData ( block , 0 );
							J2KImage.fillLine ( block , scale[ 0 ] , image.getRed ( ) , off );
							break;
						default:
							throw new InvalidParameterException ( );
					}
				}
			}
		}
		return image;
	}

	private static void fillLine ( final DataBlkInt blk , final PixelScale scale , final byte[] data , final int off ) {
		int k1 = blk.offset + blk.w - 1;
		for ( int i = blk.w - 1 ; 0 <= i ; i-- ) {
			int temp = ( blk.data[ k1 ] >> scale.fb ) + scale.ls;
			k1--;
			temp = ( 0 > temp ) ? 0 : ( ( temp > scale.mv ) ? scale.mv : temp );
			data[ off + i ] = ( byte ) temp;
		}
	}

	public static J2KLayerInfo[] decodeLayerBoundaries ( final byte[] encoded ) {
		return null;
	}

	private static BlkImgDataSrc decodeInternal ( final InputStream is ) throws IOException, ICCProfileException {
		final ParameterList defpl = new ParameterList ( );
		final String[][] param = ImgDecoder.getAllParameters ( );

		for ( int i = param.length - 1 ; 0 <= i ; i-- ) {
			if ( null != param[ i ][ 3 ] ) {
				defpl.put ( param[ i ][ 0 ] , param[ i ][ 3 ] );
			}
		}

		final ImgDecoder decoder = new ImgDecoder ( new ParameterList ( defpl ) );

		final RandomAccessIO in = new ISRandomAccessIO ( is );

		// **** File Format ****
		// If the codestream is wrapped in the jp2 fileformat, Read the file format wrapper
		final FileFormatReader ff = new FileFormatReader ( in );
		ff.readFileFormat ( );
		if ( ff.JP2FFUsed ) {
			in.seek ( ff.getFirstCodeStreamPos ( ) );
		}
		return decoder.decode ( in , ff , false );
	}

	/**
	 * Encode this <seealso cref="ManagedImage"/> object into a byte array
	 *
	 * @param os The <seealso cref="OutputStream"/> to encode the image into
	 * @return number of bytes written into the stream or -1 on error
	 */
	@Override
	public int encode ( final OutputStream os ) throws Exception {
		return J2KImage.encode ( os , this , false );
	}

	/**
	 * Encode this <seealso cref="ManagedImage"/> object into a byte array
	 *
	 * @param os       The <seealso cref="OutputStream"/> to encode the image into
	 * @param lossless true to enable lossless conversion, only useful for small images ie: sculptmaps
	 * @return number of bytes written into the stream or -1 on error
	 */
	public int encode ( final OutputStream os , final boolean lossless ) throws Exception {
		return J2KImage.encode ( os , this , lossless );
	}

	public class J2KLayerInfo {
		public int Start;
		public int End;
	}

	private class PixelScale {
		int ls, mv, fb;
	}

	private class ImgReaderMI extends ImgReader {
		private final byte[][] ptrs;
		/**
		 * Temporary DataBlkInt object (needed when encoder uses floating-point
		 * filters). This avoids allocating new DataBlk at each time
		 */
		private DataBlkInt intBlk;

		public ImgReaderMI ( final int numComp ) {
			this.w = J2KImage.this.getWidth ( );
			this.h = J2KImage.this.getHeight ( );
			this.nc = numComp;
			this.ptrs = new byte[ this.nc ][];
			for ( int i = 0 ; i < this.nc ; i++ ) {
				switch ( i ) {
					case 0:
						this.ptrs[ i ] = J2KImage.this.getRed ( );
						break;
					case 1:
						if ( 2 < nc )
							this.ptrs[ i ] = J2KImage.this.getGreen ( );
						else
							this.ptrs[ i ] = J2KImage.this.getAlpha ( );
						break;
					case 2:
						this.ptrs[ i ] = J2KImage.this.getBlue ( );
						break;
					case 3:
						this.ptrs[ i ] = J2KImage.this.getAlpha ( );
						break;
					case 5:
						this.ptrs[ i ] = J2KImage.this.getBump ( );
						break;
					default:
						throw new InvalidParameterException ( );
				}
			}

		}


		/* BlkImageDataSrc methods */

		/**
		 * Returns the position of the fixed point in the specified component (i.e.
		 * the number of fractional bits), which is always 0 for this ImgReader.
		 *
		 * @param c The index of the component.
		 * @return The position of the fixed-point (i.e. the number of fractional
		 * bits). Always 0 for this ImgReader.
		 */
		@Override
		public int getFixedPoint ( final int c ) {
			// Check component index
			if ( 0 > c || c >= this.nc )
				throw new IllegalArgumentException ( );
			return 0;
		}

		/**
		 * Returns the number of bits corresponding to the nominal range of the data
		 * in the specified component. This is the value rb (range bits) that was
		 * specified in the constructor, which normally is 8 for non bilevel data,
		 * and 1 for bilevel data.
		 *
		 * <p>
		 * If this number is <i>b</b> then the nominal range is between -2^(b-1) and
		 * 2^(b-1)-1, since unsigned data is level shifted to have a nominal avergae
		 * of 0.
		 *
		 * @param c The index of the component.
		 * @return The number of bits corresponding to the nominal range of the
		 * data. For floating-point data this value is not applicable and
		 * the return value is undefined.
		 */
		@Override
		public int getNomRangeBits ( final int c ) {
			// Check component index
			if ( 0 > c || c >= this.nc )
				throw new IllegalArgumentException ( );

			return J2KImage.this.getBitDepth ( );
		}


		/**
		 * Returns, in the blk argument, the block of image data containing the
		 * specifed rectangular area, in the specified component. The data is
		 * returned, as a reference to the internal data, if any, instead of as a
		 * copy, therefore the returned data should not be modified.
		 *
		 * <p>
		 * After being read the coefficients are level shifted by subtracting
		 * 2^(nominal bit range - 1)
		 *
		 * <p>
		 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
		 * 'h' members of the 'blk' argument, relative to the current tile. These
		 * members are not modified by this method. The 'offset' and 'scanw' of the
		 * returned data can be arbitrary. See the 'DataBlk' class.
		 *
		 * <p>
		 * If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one is
		 * created if necessary. The implementation of this interface may choose to
		 * return the same array or a new one, depending on what is more efficient.
		 * Therefore, the data array in <tt>blk</tt> prior to the method call should
		 * not be considered to contain the returned data, a new array may have been
		 * created. Instead, get the array from <tt>blk</tt> after the method has
		 * returned.
		 *
		 * <p>
		 * The returned data always has its 'progressive' attribute unset (i.e.
		 * false).
		 *
		 * <p>
		 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
		 * The exception is passed to its handleException method. The action that is
		 * taken depends on the action that has been registered in
		 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
		 *
		 * <p>
		 * This method implements buffering for the 3 components: When the first one
		 * is asked, all the 3 components are read and stored until they are needed.
		 *
		 * @param blk Its coordinates and dimensions specify the area to return.
		 *            Some fields in this object are modified to return the data.
		 * @param c   The index of the component from which to get the data. Only 0,
		 *            1 and 3 are valid.
		 * @return The requested DataBlk
		 * @see #getCompData
		 * @see JJ2KExceptionHandler
		 */
		@Override
		public DataBlk getInternCompData ( DataBlk blk , final int c ) {
			// Check component index
			if ( 0 > c || c >= this.nc )
				throw new IllegalArgumentException ( );

			// Check type of block provided as an argument
			if ( DataBlk.TYPE_INT != blk.getDataType ( ) ) {
				if ( null == intBlk )
					this.intBlk = new DataBlkInt ( blk.ulx , blk.uly , blk.w , blk.h );
				else {
					this.intBlk.ulx = blk.ulx;
					this.intBlk.uly = blk.uly;
					this.intBlk.w = blk.w;
					this.intBlk.h = blk.h;
				}
				blk = this.intBlk;
			}

			// Get data array
			int[] barr = ( int[] ) blk.getData ( );
			if ( null == barr || barr.length < blk.w * blk.h ) {
				barr = new int[ blk.w * blk.h ];
				blk.setData ( barr );
			}

			int i;
			int j;
			int k;
			final int mi = blk.uly + blk.h;
			final int levShift = 1 << ( J2KImage.this.getBitDepth ( ) - 1 );
			final byte[] buf = this.ptrs[ c ];

			for ( i = blk.uly; i < mi ; i++ ) {
				for ( k = ( i - blk.uly ) * blk.w + blk.w - 1 , j = blk.w - 1; 0 <= j ; k-- ) {
					barr[ k ] = ( ( buf[ j ] & 0xFF ) - levShift );
					j--;
				}
			}

			// Turn off the progressive attribute
			blk.progressive = false;
			// Set buffer attributes
			blk.offset = 0;
			blk.scanw = blk.w;
			return blk;
		}

		/**
		 * Returns, in the blk argument, a block of image data containing the
		 * specifed rectangular area, in the specified component. The data is
		 * returned, as a copy of the internal data, therefore the returned data can
		 * be modified "in place".
		 *
		 * <p>
		 * After being read the coefficients are level shifted by subtracting
		 * 2^(nominal bit range - 1)
		 *
		 * <p>
		 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
		 * 'h' members of the 'blk' argument, relative to the current tile. These
		 * members are not modified by this method. The 'offset' of the returned
		 * data is 0, and the 'scanw' is the same as the block's width. See the
		 * 'DataBlk' class.
		 *
		 * <p>
		 * If the data array in 'blk' is 'null', then a new one is created. If the
		 * data array is not 'null' then it is reused, and it must be large enough
		 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
		 * 'IndexOutOfBoundsException' is thrown by the Java system.
		 *
		 * <p>
		 * The returned data has its 'progressive' attribute unset (i.e. false).
		 *
		 * <p>
		 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
		 * The exception is passed to its handleException method. The action that is
		 * taken depends on the action that has been registered in
		 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
		 *
		 * @param blk Its coordinates and dimensions specify the area to return. If
		 *            it contains a non-null data array, then it must have the
		 *            correct dimensions. If it contains a null data array a new one
		 *            is created. The fields in this object are modified to return
		 *            the data.
		 * @param c   The index of the component from which to get the data. Between null and numComp -1.
		 * @return The requested DataBlk
		 * @see #getInternCompData
		 * @see JJ2KExceptionHandler
		 */
		@Override
		public final DataBlk getCompData ( DataBlk blk , final int c ) {
			// NOTE: can not directly call getInternCompData since that returns
			// internally buffered data.
			final int w;
			final int h;

			// Check type of block provided as an argument
			if ( DataBlk.TYPE_INT != blk.getDataType ( ) ) {
				final DataBlkInt tmp = new DataBlkInt ( blk.ulx , blk.uly , blk.w , blk.h );
				blk = tmp;
			}

			int[] bakarr = ( int[] ) blk.getData ( );
			// Save requested block size
			w = blk.w;
			h = blk.h;
			// Force internal data buffer to be different from external
			blk.setData ( null );
			this.getInternCompData ( blk , c );
			// Copy the data
			if ( null == bakarr ) {
				bakarr = new int[ w * h ];
			}
			if ( 0 == blk.offset && blk.scanw == w ) {
				// Requested and returned block buffer are the same size
				System.arraycopy ( blk.getData ( ) , 0 , bakarr , 0 , w * h );
			}
			else { // Requested and returned block are different
				for ( int i = h - 1 ; 0 <= i ; i-- ) { // copy line by line
					System.arraycopy ( blk.getData ( ) , blk.offset + i * blk.scanw , bakarr , i * w , w );
				}
			}
			blk.setData ( bakarr );
			blk.offset = 0;
			blk.scanw = blk.w;
			return blk;
		}

		/* ImgReader methods */

		/**
		 * Closes the underlying file from where the image data is being read. No
		 * operations are possible after a call to this method.
		 *
		 * @throws IOException If an I/O error occurs.
		 */
		@Override
		public void close ( ) throws IOException {
			// Nothing to do here.
		}

		/**
		 * Returns true if the data read was originally signed in the specified
		 * component, false if not. This method always returns false since PPM data
		 * is always unsigned.
		 *
		 * @param c The index of the component, from 0 to N-1.
		 * @return always false, since PPM data is always unsigned.
		 */
		@Override
		public boolean isOrigSigned ( final int c ) {
			// Check component index
			if ( 0 > c || c >= this.nc )
				throw new IllegalArgumentException ( );
			return false;
		}
	}
}
