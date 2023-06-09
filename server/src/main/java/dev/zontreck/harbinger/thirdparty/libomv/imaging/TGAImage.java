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

import org.apache.commons.io.input.SwappedDataInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TGAImage extends ManagedImage {
	public TGAImage ( final ManagedImage image ) throws IOException {
		super ( image );
	}

	protected TGAImage ( final int width , final int height , final byte type ) throws Exception {
		super ( width , height , type );
	}

	public static TGAImage decode ( final InputStream is ) throws Exception {
		final SwappedDataInputStream sis = is instanceof SwappedDataInputStream ? ( SwappedDataInputStream ) is : new SwappedDataInputStream ( is );
		final TGAHeader header = new TGAHeader ( sis );
		byte channels = 0;

		if ( 4096 < header.ImageSpec.Width ||
				4096 < header.ImageSpec.Height )
			throw new IllegalArgumentException ( "Image too large." );

		if ( 8 != header.ImageSpec.PixelDepth &&
				16 != header.ImageSpec.PixelDepth &&
				24 != header.ImageSpec.PixelDepth &&
				32 != header.ImageSpec.PixelDepth )
			throw new IllegalArgumentException ( "Not a supported tga file." );

		if ( 0 < header.ColorMap.alphaBits ) {
			channels = ImageChannels.Alpha;
		}
		if ( 0 < header.ColorMap.colorBits ) {
			channels += ImageChannels.Color;
		}
		else if ( header.ColorMap.bits > header.ColorMap.alphaBits ) {
			channels += ImageChannels.Gray;
		}

		final TGAImage image = new TGAImage ( header.ImageSpec.Width , header.ImageSpec.Height , channels );

		if ( header.getRleEncoded ( ) )
			image.decodeRle ( sis , header.ImageSpec.PixelDepth / 8 , header.ColorMap , header.ImageSpec.getBottomUp ( ) );
		else
			image.decodePlain ( sis , header.ImageSpec.PixelDepth / 8 , header.ColorMap , header.ImageSpec.getBottomUp ( ) );
		return image;
	}

	public static int encode ( final OutputStream os , final ManagedImage image ) throws Exception {
		final TGAHeader header = new TGAHeader ( image );
		header.write ( os );

		int len = 18;
		final int n = image.getWidth ( ) * image.getHeight ( );

		if ( 0 != ( image.getChannels ( ) & ImageChannels.Alpha ) ) {
			if ( 0 != ( image.getChannels ( ) & ImageChannels.Color ) ) {
				// RGBA
				for ( int i = 0 ; i < n ; i++ ) {
					os.write ( image.getBlue ( )[ i ] );
					os.write ( image.getGreen ( )[ i ] );
					os.write ( image.getRed ( )[ i ] );
					os.write ( image.getAlpha ( )[ i ] );
				}
			}
			else if ( 0 != ( image.getChannels ( ) & ImageChannels.Gray ) ) {
				for ( int i = 0 ; i < n ; i++ ) {
					os.write ( image.getRed ( )[ i ] );
					os.write ( image.getAlpha ( )[ i ] );
				}
			}
			else {
				// Alpha only
				for ( int i = 0 ; i < n ; i++ ) {
					os.write ( image.getAlpha ( )[ i ] );
				}
			}
			len += n * 4;
		}
		else {
			if ( 0 != ( image.getChannels ( ) & ImageChannels.Color ) ) {
				// RGB
				for ( int i = 0 ; i < n ; i++ ) {
					os.write ( image.getBlue ( )[ i ] );
					os.write ( image.getGreen ( )[ i ] );
					os.write ( image.getRed ( )[ i ] );
				}
			}
			else if ( 0 != ( image.getChannels ( ) & ImageChannels.Gray ) ) {
				for ( int i = 0 ; i < n ; i++ ) {
					os.write ( image.getRed ( )[ i ] );
				}
			}
			len += n * 3;
		}
		return len;
	}

	private void UnpackColor ( final int[] values , int pixel , TGAHeader.TGAColorMap cd ) {
		for ( int x = 0 ; x < getWidth ( ) ; x++ , pixel++ ) {
			int val = values[ x ];
			if ( 0 == cd.RMask && 0 == cd.GMask && 0 == cd.BMask && 0xFF == cd.AMask ) {
				// Special case to deal with 8-bit TGA files that we treat as alpha masks
				setAlpha ( pixel , ( byte ) val );
			}
			else if ( 0 < cd.length ) {
				setRed ( pixel , cd.RedM[ val ] );
				setGreen ( pixel , cd.GreenM[ val ] );
				setBlue ( pixel , cd.BlueM[ val ] );
				setAlpha ( pixel , cd.AlphaM[ val ] );
			}
			else {
				setRed ( pixel , ( byte ) ( ( val >> cd.RShift ) & cd.RMask ) );
				setGreen ( pixel , ( byte ) ( ( val >> cd.GShift ) & cd.GMask ) );
				setBlue ( pixel , ( byte ) ( ( val >> cd.BShift ) & cd.BMask ) );
				setAlpha ( pixel , ( byte ) ( ( val >> cd.AShift ) & cd.AMask ) );
			}
		}
	}

	/**
	 * Reads the pixmap as RLE encode stream
	 *
	 * @param is       the DataInputStream in little endian format
	 * @param byp      the number of bytes to read per pixel value
	 * @param cd       the color map structure that contains the information how to interpret the color value of the pixel entry
	 * @param bottomUp indicates if the bitmap is stored in bottemUp format
	 * @throws IOException
	 */
	private void decodeRle ( SwappedDataInputStream is , int byp , TGAHeader.TGAColorMap cd , boolean bottomUp ) throws IOException {
		int[] vals = new int[ getWidth ( ) + 128 ];
		int x = 0, pixel = bottomUp ? ( getHeight ( ) - 1 ) * getWidth ( ) : 0;

		// RLE compressed
		for ( int y = 0 ; y < getHeight ( ) ; y++ ) {
			while ( x < getWidth ( ) ) {
				int nb = is.readUnsignedByte ( ); // num of pixels
				if ( 0 == ( nb & 0x80 ) ) { // 0x80 = dec 128, bits 10000000
					for ( int i = 0 ; i <= nb ; i++ , x++ ) {
						for ( int k = 0 ; k < byp ; k++ ) {
							vals[ x ] |= is.readUnsignedByte ( ) << ( k << 3 );
						}
					}
				}
				else {
					int val = 0;
					for ( int k = 0 ; k < byp ; k++ ) {
						val |= is.readUnsignedByte ( ) << ( k << 3 );
					}
					nb &= 0x7f;
					for ( int j = 0 ; j <= nb ; j++ , x++ ) {
						vals[ x ] = val;
					}
				}
			}
			UnpackColor ( vals , pixel , cd );
			if ( x > getWidth ( ) ) {
				System.arraycopy ( vals , getWidth ( ) , vals , 0 , x - getWidth ( ) );
				x -= getWidth ( );
			}
			else {
				x = 0;
			}
			pixel += bottomUp ? - getWidth ( ) : getWidth ( );
		}
	}

	/**
	 * Reads the pixmap as unencoded stream
	 *
	 * @param is       the DataInputStream in little endian format
	 * @param byp      the number of bytes to read per pixel value
	 * @param cd       the color map structure that contains the information how to interpret the color value of the pixel entry
	 * @param bottomUp indicates if the bitmap is stored in bottemUp format
	 * @throws IOException
	 */
	private void decodePlain ( SwappedDataInputStream is , int byp , final TGAHeader.TGAColorMap cd , final boolean bottomUp ) throws IOException {
		final int[] vals = new int[ this.getWidth ( ) ];
		int pixel = bottomUp ? ( this.getHeight ( ) - 1 ) * this.getWidth ( ) : 0;
		for ( int y = 0 ; y < this.getHeight ( ) ; y++ ) {
			for ( int x = 0 ; x < this.getWidth ( ) ; x++ ) {
				for ( int k = 0 ; k < byp ; k++ ) {
					vals[ x ] |= is.readUnsignedByte ( ) << ( k << 3 );
				}
			}
			this.UnpackColor ( vals , pixel , cd );
			pixel += bottomUp ? - this.getWidth ( ) : this.getWidth ( );
		}
	}

	@Override
	public int encode ( final OutputStream os ) throws Exception {
		return encode ( os , this );
	}
}
