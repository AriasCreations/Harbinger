/*
 * CVS identifier:
 *
 * $Id: BlkImgDataSrcImageProducer.java,v 1.19 2002/08/08 14:08:42 grosbois Exp $
 *
 * Class:                   BlkImgDataSrcImageProducer
 *
 * Description:             Creates an Java AWT ImapeProducer from a
 *                          BlkImgDataSrc
 *
 *
 *
 * COPYRIGHT:
 *
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 *
 * Copyright (c) 1999/2000 JJ2000 Partners.
 */
package dev.zontreck.harbinger.thirdparty.jj2000.disp;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.Coord;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlkInt;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.security.InvalidParameterException;
import java.util.Vector;

/**
 * This class provides an ImageProducer for the BlkImgDataSrc interface. It will
 * request data from the BlkImgDataSrc source and deliver it to the registered
 * image consumers. The data is requested line by line, starting at the top of
 * each tile. The tiles are requested in raster-scan order.
 *
 * <p>
 * The image data is not rescaled to fit the available dynamic range (not even
 * the alpha values for RGBA data).
 *
 * <p>
 * BlkImgDataSrc sources with 1, 3 and 4 components are supported. If 1, it is
 * assumed to be gray-level data. If 3 it is assumed to be RGB data, in that
 * order. If 4 it is assumed to be RGBA data (RGB plus alpha plane), in that
 * order. All components must have the same size.
 *
 * @see ImageProducer
 * @see BlkImgDataSrc
 */
public class BlkImgDataSrcImageProducer implements ImageProducer {

	/**
	 * The gray-level image type (256 levels). For this type the source of image
	 * data must have only 1 component.
	 */
	private static final int GRAY = 0;
	/**
	 * The color image type (24 bits RGB). No alpha plane. For this type the
	 * source of image data must have 3 components, which are considered to be
	 * R, G and B, in that order
	 */
	private static final int RGB = 1;
	/**
	 * The color image type (32 bits RGBA). For this type the source of image
	 * data must have 4 components, which are considered to be R, G, B and A, in
	 * that order.
	 */
	private static final int RGBA = 2;
	/**
	 * The default color model (0xAARRGGBB) used in Java
	 */
	private static final ColorModel cm = ColorModel.getRGBdefault ( );
	/**
	 * The list of image consumers for this image producer
	 */
	private final Vector<ImageConsumer> consumers;
	/**
	 * The source of image data
	 */
	private final BlkImgDataSrc src;
	/**
	 * The type of image: GRAY, RGB or RGBA
	 */
	private final int type;

	/**
	 * Creates an image producer which uses 'src' as the source of image data.
	 * If 'once' is true then the image is produced only once.
	 *
	 * @param src The source of image data.
	 */
	public BlkImgDataSrcImageProducer ( final BlkImgDataSrc src ) {
		int i;

		// Check for image type
		switch ( src.getNumComps ( ) ) {
			case 1:
				this.type = BlkImgDataSrcImageProducer.GRAY;
				break;
			case 3:
				this.type = BlkImgDataSrcImageProducer.RGB;
				break;
			case 4:
				this.type = BlkImgDataSrcImageProducer.RGBA;
				break;
			default:
				throw new IllegalArgumentException ( "Only 1, 3, and 4 components supported" );
		}
		// Check component sizes and bit depths
		final int imh = src.getCompImgHeight ( 0 );
		final int imw = src.getCompImgWidth ( 0 );
		for ( i = src.getNumComps ( ) - 1; 0 <= i ; i-- ) {
			if ( src.getCompImgHeight ( i ) != imh || src.getCompImgWidth ( i ) != imw ) {
				throw new IllegalArgumentException ( "All components must have the same dimensions and "
						+ "no subsampling" );
			}
			if ( 8 < src.getNomRangeBits ( i ) ) {
				throw new IllegalArgumentException ( "Depths greater than 8 bits per component is " + "not supported" );
			}
		}
		this.src = src;
		this.consumers = new Vector<ImageConsumer> ( );
	}

	/**
	 * Returns an Image object given an BlkImgDataSrc source. It will use a new
	 * J2KImageProducer object as the underlying image producer.
	 *
	 * <p>
	 * This method uses the JVM default Toolkit, which might not be what it is
	 * desired.
	 *
	 * @param src The source of image data.
	 * @return An image which has a J2KImageProducer object as the underlying
	 * image producer.
	 */
	public static Image createImage ( final BlkImgDataSrc src ) {
		// Use the system toolkit's createImage method
		return Toolkit.getDefaultToolkit ( ).createImage ( new BlkImgDataSrcImageProducer ( src ) );
	}

	/**
	 * Returns an Image object given an BlkImgDataSrc source. It will use a new
	 * J2KImageProducer object as the underlying image producer.
	 *
	 * <p>
	 * This method uses the component's toolkit. The toolkit of a component may
	 * change if it is moved from one frame to another one, since it is the
	 * frame that controls which toolkit is used.
	 *
	 * @param src The source of image data.
	 * @param c   The component to use to generate the 'Image' object from the
	 *            'ImageProducer'.
	 * @return An image which has a J2KImageProducer object as the underlying
	 * image producer.
	 */
	public static Image createImage ( final BlkImgDataSrc src , final Component c ) {
		// Use the component's toolkit createImage method
		return c.getToolkit ( ).createImage ( new BlkImgDataSrcImageProducer ( src ) );
	}

	/**
	 * Registers an image consumer with this image producer. The delivery of
	 * image data does not start immediately. It will only start after the next
	 * call to the startProduction() method.
	 *
	 * @param ic The image consumer to which image data has to be delivered.
	 * @see #startProduction
	 */
	@Override
	public final synchronized void addConsumer ( final ImageConsumer ic ) {
		if ( null != ic && ! this.consumers.contains ( ic ) ) {
			this.consumers.addElement ( ic );
		}
	}

	/**
	 * This method determines if the given image consumer, 'ic', is registered
	 * with this image producer.
	 *
	 * @param ic The image consumer to test.
	 * @return True if 'ic' is registered with this image producer, false
	 * otherwise.
	 */
	@Override
	public boolean isConsumer ( final ImageConsumer ic ) {
		return this.consumers.contains ( ic );
	}

	/**
	 * Removes the given image consumer 'ic' from the list of consumers
	 * registered with this producer. This image producer will stop sending
	 * pixel data to 'ic' as soon as it is feasible. The method call is ignored
	 * if 'ic' has not been registered with this image producer.
	 *
	 * @param ic The image consumer to be removed
	 */
	@Override
	public synchronized void removeConsumer ( final ImageConsumer ic ) {
		this.consumers.removeElement ( ic );
	}

	/**
	 * Registers the given ImageConsumer object as a consumer and starts an
	 * immediate reconstruction of the image data which will then be delivered
	 * to this consumer and any other consumer which may have already been
	 * registered with the producer.
	 *
	 * <p>
	 * Delivery is performed in "parallel" to all the registered image
	 * consumers. By "parallel" it is meant that each line of the image is
	 * delivered to all consumers before delivering the next line.
	 *
	 * <p>
	 * If the data returned by the BlkImgDataSrc source happens to be
	 * progressive (see BlkImgDataSrc and DataBlk) then the abort condition is
	 * sent to the image consumers and no further data is delivered.
	 *
	 * <p>
	 * Once all the data is sent to a consumer this one is automatically removed
	 * from the list of registered ones, unless an abort happens.
	 *
	 * <p>
	 * To start the BlkImgDataSrc is set to tile (0,0), and the tiles are
	 * produced in raster sacn order. Once the last tile is produced,
	 * setTile(0,0) is called again, which signals that we are done with the
	 * current tile, which might free up resources.
	 *
	 * @param ic The image consumer to register
	 */
	@Override
	public void startProduction ( final ImageConsumer ic ) {
		int i, k1, k2, k3, k4, l; // counters
		int tmp1, tmp2, tmp3, tmp4; // temporary storage for sample values
		final int mv1;  // max value for each component
		int mv2;
		int mv3;
		int mv4;
		final int ls1;  // level shift for each component
		int ls2;
		int ls3;
		int ls4;
		final int fb1;  // fractional bits for each component
		int fb2;
		int fb3;
		int fb4;
		int[] data1, data2, data3, data4; // references to data buffers
		ImageConsumer[] cons; // image consumers cache
		int hints; // hints to image consumers
		int height; // image height
		int width; // image width
		int[] pixbuf; // line buffer for pixel data
		DataBlkInt db1, db2, db3, db4; // data-blocks to request data from src
		int tOffx, tOffy; // Active tile offset
		boolean prog; // Flag for progressive data
		Coord nT = this.src.getNumTiles ( null );
		int tIdx = 0; // index of the current tile

		// Register ic
		if ( null != ic ) {
			this.addConsumer ( ic );
		}

		// Set the cache for the consumers
		synchronized ( this ) {
			// synchronized to avoid addition or deletion of consumers while
			// copying them to cache
			cons = new ImageConsumer[ this.consumers.size ( ) ];
			this.consumers.copyInto ( cons );
		}

		if ( null == src ) {
			// We cant't render with no source
			for ( i = cons.length - 1; 0 <= i ; i-- ) {
				cons[ i ].imageComplete ( ImageConsumer.IMAGEERROR );
			}
			return; // can not continue processing
		}

		// Initialize
		pixbuf = null; // to keep compiler happy
		ls2 = fb2 = mv2 = 0; // to keep compiler happy
		ls3 = fb3 = mv3 = 0; // to keep compiler happy
		ls4 = fb4 = mv4 = 0; // to keep compiler happy
		db1 = db2 = db3 = db4 = null; // to keep compiler happy
		switch ( this.type ) {
			case BlkImgDataSrcImageProducer.RGBA:
				db4 = new DataBlkInt ( ); // Alpha plane
				ls4 = 1 << ( this.src.getNomRangeBits ( 3 ) - 1 );
				mv4 = ( 1 << this.src.getNomRangeBits ( 3 ) ) - 1;
				fb4 = this.src.getFixedPoint ( 3 );
			case BlkImgDataSrcImageProducer.RGB:
				db3 = new DataBlkInt ( ); // Blue plane
				ls3 = 1 << ( this.src.getNomRangeBits ( 2 ) - 1 );
				mv3 = ( 1 << this.src.getNomRangeBits ( 2 ) ) - 1;
				fb3 = this.src.getFixedPoint ( 2 );
				db2 = new DataBlkInt ( ); // Green plane
				ls2 = 1 << ( this.src.getNomRangeBits ( 1 ) - 1 );
				mv2 = ( 1 << this.src.getNomRangeBits ( 1 ) ) - 1;
				fb2 = this.src.getFixedPoint ( 1 );
			case BlkImgDataSrcImageProducer.GRAY:
				db1 = new DataBlkInt ( ); // Gray or Red plane
				ls1 = 1 << ( this.src.getNomRangeBits ( 0 ) - 1 );
				mv1 = ( 1 << this.src.getNomRangeBits ( 0 ) ) - 1;
				fb1 = this.src.getFixedPoint ( 0 );
				break;
			default:
				throw new Error ( "Internal JJ2000 error" );
		}

		// Set the hints and info to the cached consumers
		nT = this.src.getNumTiles ( null );
		hints = ImageConsumer.SINGLEFRAME | ImageConsumer.SINGLEPASS;
		if ( 1 == nT.x ) {
			hints |= ImageConsumer.COMPLETESCANLINES | ImageConsumer.TOPDOWNLEFTRIGHT;
		}
		else {
			hints |= ImageConsumer.RANDOMPIXELORDER;
		}
		for ( i = cons.length - 1; 0 <= i ; i-- ) {
			cons[ i ].setColorModel ( BlkImgDataSrcImageProducer.cm );
			cons[ i ].setDimensions ( this.src.getCompImgWidth ( 0 ) , this.src.getCompImgHeight ( 0 ) );
			cons[ i ].setHints ( hints );
		}

		// Start the data delivery to the cached consumers tile by tile
		for ( int y = 0 ; y < nT.y ; y++ ) {
			// Loop on horizontal tiles
			for ( int x = 0 ; x < nT.x ; x++ , tIdx++ ) {
				this.src.setTile ( x , y );

				// Initialize tile
				height = this.src.getTileCompHeight ( tIdx , 0 );
				width = this.src.getTileCompWidth ( tIdx , 0 );

				if ( null == pixbuf || pixbuf.length < width ) {
					pixbuf = new int[ width ];
				}
				// The offset of the active tiles is the same for all
				// components, since we don't support different component
				// dimensions.
				tOffx = this.src.getCompULX ( 0 ) - ( int ) Math.ceil ( this.src.getImgULX ( ) / ( double ) this.src.getCompSubsX ( 0 ) );
				tOffy = this.src.getCompULY ( 0 ) - ( int ) Math.ceil ( this.src.getImgULY ( ) / ( double ) this.src.getCompSubsY ( 0 ) );

				// Deliver in lines to reduce memory usage
				for ( l = 0; l < height ; l++ ) {
					// Request line data
					prog = false;
					switch ( this.type ) {
						case BlkImgDataSrcImageProducer.RGBA:
							// Request alpha plane
							db4.ulx = 0;
							db4.uly = l;
							db4.w = width;
							db4.h = 1;
							this.src.getInternCompData ( db4 , 3 );
							prog = prog || db4.progressive;
						case BlkImgDataSrcImageProducer.RGB:
							// Request blue and green planes
							db2.ulx = db3.ulx = 0;
							db2.uly = db3.uly = l;
							db2.w = db3.w = width;
							db2.h = db3.h = 1;
							this.src.getInternCompData ( db3 , 2 );
							prog = prog || db3.progressive;
							this.src.getInternCompData ( db2 , 1 );
							prog = prog || db2.progressive;
						case BlkImgDataSrcImageProducer.GRAY:
							// Request
							db1.ulx = 0;
							db1.uly = l;
							db1.w = width;
							db1.h = 1;
							this.src.getInternCompData ( db1 , 0 );
							prog = prog || db1.progressive;
							break;
						default:
							throw new InvalidParameterException ( "Invalid color type: " + this.type );
					}
					if ( prog ) { // Progressive data not supported
						// We use abort since maybe at a later time
						// the data won't
						// be progressive anymore
						// (DSC: this need to be improved of course)
						for ( i = cons.length - 1; 0 <= i ; i-- ) {
							cons[ i ].imageComplete ( ImageConsumer.IMAGEABORTED );
						}
						return; // can not continue processing
					}
					// Put pixel data in line buffer
					switch ( this.type ) {
						case BlkImgDataSrcImageProducer.GRAY:
							data1 = db1.data;
							k1 = db1.offset + width - 1;
							for ( i = width - 1; 0 <= i ; i-- ) {
								tmp1 = ( data1[ k1 ] >> fb1 ) + ls1;
								k1--;
								tmp1 = ( 0 > tmp1 ) ? 0 : ( ( tmp1 > mv1 ) ? mv1 : tmp1 );
								pixbuf[ i ] = ( 0xFF << 24 ) | ( tmp1 << 16 ) | ( tmp1 << 8 ) | tmp1;
							}
							break;
						case BlkImgDataSrcImageProducer.RGB:
							data1 = db1.data; // red
							data2 = db2.data; // green
							data3 = db3.data; // blue
							k1 = db1.offset + width - 1;
							k2 = db2.offset + width - 1;
							k3 = db3.offset + width - 1;
							for ( i = width - 1; 0 <= i ; i-- ) {
								tmp1 = ( data1[ k1 ] >> fb1 ) + ls1;
								k1--;
								tmp1 = ( 0 > tmp1 ) ? 0 : ( ( tmp1 > mv1 ) ? mv1 : tmp1 );
								tmp2 = ( data2[ k2 ] >> fb2 ) + ls2;
								k2--;
								tmp2 = ( 0 > tmp2 ) ? 0 : ( ( tmp2 > mv2 ) ? mv2 : tmp2 );
								tmp3 = ( data3[ k3 ] >> fb3 ) + ls3;
								k3--;
								tmp3 = ( 0 > tmp3 ) ? 0 : ( ( tmp3 > mv3 ) ? mv3 : tmp3 );
								pixbuf[ i ] = ( 0xFF << 24 ) | ( tmp1 << 16 ) | ( tmp2 << 8 ) | tmp3;
							}
							break;

						case BlkImgDataSrcImageProducer.RGBA:
							data1 = db1.data; // red
							data2 = db2.data; // green
							data3 = db3.data; // blue
							data4 = db4.data; // alpha
							k1 = db1.offset + width - 1;
							k2 = db2.offset + width - 1;
							k3 = db3.offset + width - 1;
							k4 = db4.offset + width - 1;
							for ( i = width - 1; 0 <= i ; i-- ) {
								tmp1 = ( data1[ k1 ] >> fb1 ) + ls1;
								k1--;
								tmp1 = ( 0 > tmp1 ) ? 0 : ( ( tmp1 > mv1 ) ? mv1 : tmp1 );
								tmp2 = ( data2[ k2 ] >> fb2 ) + ls2;
								k2--;
								tmp2 = ( 0 > tmp2 ) ? 0 : ( ( tmp2 > mv2 ) ? mv2 : tmp2 );
								tmp3 = ( data3[ k3 ] >> fb3 ) + ls3;
								k3--;
								tmp3 = ( 0 > tmp3 ) ? 0 : ( ( tmp3 > mv3 ) ? mv3 : tmp3 );
								tmp4 = ( data4[ k4 ] >> fb4 ) + ls4;
								k4--;
								tmp4 = ( 0 > tmp4 ) ? 0 : ( ( tmp4 > mv4 ) ? mv4 : tmp4 );
								pixbuf[ i ] = ( tmp4 << 24 ) | ( tmp1 << 16 ) | ( tmp2 << 8 ) | tmp3;
							}
							break;
						default:
							throw new InvalidParameterException ( "Invalid color type: " + this.type );
					}
					// Send the line data to the consumers
					for ( i = cons.length - 1; 0 <= i ; i-- ) {
						cons[ i ].setPixels ( tOffx , tOffy + l , width , 1 , BlkImgDataSrcImageProducer.cm , pixbuf , 0 , width );
					}
				}
			} // End loop on horizontal tiles
		} // End loop on vertical tiles

		// Signal that this frame is complete. This is so that display of the
		// last tile occurs as soon as possible. When calling with
		// STATICIMAGEDONE the ImageConsumer might do some cleanup that will
		// take a considerable amount of time on large images, this is why we
		// first signal the frame as done (even though there is only one).
		for ( i = cons.length - 1; 0 <= i ; i-- ) {
			cons[ i ].imageComplete ( ImageConsumer.SINGLEFRAMEDONE );
		}
		// Signal that image is complete
		for ( i = cons.length - 1; 0 <= i ; i-- ) {
			cons[ i ].imageComplete ( ImageConsumer.STATICIMAGEDONE );
		}
		// Remove the consumers since all the data has been sent
		synchronized ( this ) {
			for ( i = cons.length - 1; 0 <= i ; i-- ) {
				this.consumers.removeElement ( cons[ i ] );
			}
		}
	}

	/**
	 * Starts the delivery of pixel data in the top-down letf-right order to the
	 * image consumer 'ic'. The TOPDOWNLEFTRIGHT hint is set in the image
	 * consumer on delivery.
	 *
	 * <p>
	 * Currently this call is ignored (which is perfectly legal according to the
	 * ImageProducer interface specification).
	 *
	 * @param ic The image consumer to which the data is sent in top-down,
	 *           left-right order.
	 */
	@Override
	public void requestTopDownLeftRightResend ( final ImageConsumer ic ) {
	}
}
