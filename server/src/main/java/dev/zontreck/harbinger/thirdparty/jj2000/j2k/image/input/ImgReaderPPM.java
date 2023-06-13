/*
 *
 * Class:                   ImgWriterPPM
 *
 * Description:             Image writer for unsigned 8 bit data in
 *                          PPM files.
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.input;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.*;
import java.io.*;

/**
 * This class implements the ImgData interface for reading 8 bits unsigned data
 * from a binary PPM file
 * 
 * <P>
 * After being read the coefficients are level shifted by subtracting 2^(nominal
 * bit range - 1)
 * 
 * <P>
 * The transfer type (see ImgData) of this class is TYPE_INT.
 * 
 * <P>
 * This class is <i>buffered</i>: the 3 input components(R,G,B) are read when
 * the first one (R) is asked. The 2 others are stored until they are needed.
 * 
 * <P>
 * NOTE: This class is not thread safe, for reasons of internal buffering.
 * 
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.ImgData
 */
public class ImgReaderPPM extends ImgReader
{

	/** DC offset value used when reading image */
	public static int DC_OFFSET = 128;

	/** Where to read the data from */
	private RandomAccessFile in;

	/** The offset of the raw pixel data in the PPM file */
	private int offset;

	/** The number of bits that determine the nominal dynamic range */
	private final int rb;

	/** Buffer for the 3 components of each pixel(in the current block) */
	private final int[][] barr = new int[3][];

	/** Data block used only to store coordinates of the buffered blocks */
	private final DataBlkInt dbi = new DataBlkInt();

	/** The line buffer. */
	// This makes the class not thread safe (but it is not the only one making
	// it so)
	private byte[] buf;

	/**
	 * Temporary DataBlkInt object (needed when encoder uses floating-point
	 * filters). This avoid allocating new DataBlk at each time
	 */
	private DataBlkInt intBlk;

	/**
	 * Creates a new PPM file reader from the specified file.
	 * 
	 * @param file
	 *            The input file.
	 *
	 */
	public ImgReaderPPM(final File file) throws IOException
	{
		this(new RandomAccessFile(file, "r"));
	}

	/**
	 * Creates a new PPM file reader from the specified file name.
	 * 
	 * @param fname
	 *            The input file name.
	 *
	 */
	public ImgReaderPPM(final String fname) throws IOException
	{
		this(new RandomAccessFile(fname, "r"));
	}

	/**
	 * Creates a new PPM file reader from the specified RandomAccessFile object.
	 * The file header is read to acquire the image size.
	 * 
	 * @param in
	 *            From where to read the data
	 * 
	 * @exception EOFException
	 *                if an EOF is read
	 * @exception IOException
	 *                if an error occurs when opening the file
	 */
	private ImgReaderPPM(final RandomAccessFile in) throws IOException
	{
		this.in = in;

		this.confirmFileType();
		this.skipCommentAndWhiteSpace();
		this.w = this.readHeaderInt();
		this.skipCommentAndWhiteSpace();
		this.h = this.readHeaderInt();
		this.skipCommentAndWhiteSpace();
		/* Read the highest pixel value from header (not used) */
		this.readHeaderInt();
		this.nc = 3;
		this.rb = 8;
	}

	/**
	 * Closes the underlying file from where the image data is being read. No
	 * operations are possible after a call to this method.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public void close() throws IOException
	{
		this.in.close();
		this.in = null;
		// Free memory
		this.barr[0] = null;
		this.barr[1] = null;
		this.barr[2] = null;
		this.buf = null;
	}

	/**
	 * Returns the number of bits corresponding to the nominal range of the data
	 * in the specified component. This is the value rb (range bits) that was
	 * specified in the constructor, which normally is 8 for non bilevel data,
	 * and 1 for bilevel data.
	 * 
	 * <P>
	 * If this number is <i>b</b> then the nominal range is between -2^(b-1) and
	 * 2^(b-1)-1, since unsigned data is level shifted to have a nominal avergae
	 * of 0.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The number of bits corresponding to the nominal range of the
	 *         data. For floating-point data this value is not applicable and
	 *         the return value is undefined.
	 */
	@Override
	public int getNomRangeBits(final int c)
	{
		// Check component index
		if (0 > c || 2 < c)
			throw new IllegalArgumentException();

		return this.rb;
	}

	/**
	 * Returns the position of the fixed point in the specified component (i.e.
	 * the number of fractional bits), which is always 0 for this ImgReader.
	 * 
	 * @param c
	 *            The index of the component.
	 * 
	 * @return The position of the fixed-point (i.e. the number of fractional
	 *         bits). Always 0 for this ImgReader.
	 */
	@Override
	public int getFixedPoint(final int c)
	{
		// Check component index
		if (0 > c || 2 < c)
			throw new IllegalArgumentException();
		return 0;
	}

	/**
	 * Returns, in the blk argument, the block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a reference to the internal data, if any, instead of as a
	 * copy, therefore the returned data should not be modified.
	 * 
	 * <P>
	 * After being read the coefficients are level shifted by subtracting
	 * 2^(nominal bit range - 1)
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' and 'scanw' of the
	 * returned data can be arbitrary. See the 'DataBlk' class.
	 * 
	 * <P>
	 * If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one is
	 * created if necessary. The implementation of this interface may choose to
	 * return the same array or a new one, depending on what is more efficient.
	 * Therefore, the data array in <tt>blk</tt> prior to the method call should
	 * not be considered to contain the returned data, a new array may have been
	 * created. Instead, get the array from <tt>blk</tt> after the method has
	 * returned.
	 * 
	 * <P>
	 * The returned data always has its 'progressive' attribute unset (i.e.
	 * false).
	 * 
	 * <P>
	 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
	 * The exception is passed to its handleException method. The action that is
	 * taken depends on the action that has been registered in
	 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
	 * 
	 * <P>
	 * This method implements buffering for the 3 components: When the first one
	 * is asked, all the 3 components are read and stored until they are needed.
	 * 
	 * @param blk
	 *            Its coordinates and dimensions specify the area to return.
	 *            Some fields in this object are modified to return the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data. Only 0,
	 *            1 and 3 are valid.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getCompData
	 * 
	 * @see JJ2KExceptionHandler
	 */
	@Override
	public final DataBlk getInternCompData(DataBlk blk, final int c)
	{
		// Check component index
		if (0 > c || 2 < c)
			throw new IllegalArgumentException();

		// Check type of block provided as an argument
		if (DataBlk.TYPE_INT != blk.getDataType())
		{
			if (null == intBlk)
				this.intBlk = new DataBlkInt(blk.ulx, blk.uly, blk.w, blk.h);
			else
			{
				this.intBlk.ulx = blk.ulx;
				this.intBlk.uly = blk.uly;
				this.intBlk.w = blk.w;
				this.intBlk.h = blk.h;
			}
			blk = this.intBlk;
		}

		// If asking a component for the first time for this block, read the 3
		// components
		if ((null == barr[c]) || (this.dbi.ulx > blk.ulx) || (this.dbi.uly > blk.uly) || (this.dbi.ulx + this.dbi.w < blk.ulx + blk.w)
				|| (this.dbi.uly + this.dbi.h < blk.uly + blk.h))
		{
			int k;
			int j;
			int i;
			final int mi;
			final int[] red;
			int[] green;
			final int[] blue;

			// Reset data arrays if needed
			if (null == barr[c] || this.barr[c].length < blk.w * blk.h)
			{
				this.barr[c] = new int[blk.w * blk.h];
			}
			blk.setData(this.barr[c]);

			i = (c + 1) % 3;
			if (null == barr[i] || this.barr[i].length < blk.w * blk.h)
			{
				this.barr[i] = new int[blk.w * blk.h];
			}
			i = (c + 2) % 3;
			if (null == barr[i] || this.barr[i].length < blk.w * blk.h)
			{
				this.barr[i] = new int[blk.w * blk.h];
			}

			// set attributes of the DataBlk used for buffering
			this.dbi.ulx = blk.ulx;
			this.dbi.uly = blk.uly;
			this.dbi.w = blk.w;
			this.dbi.h = blk.h;

			// Check line buffer
			if (null == buf || this.buf.length < 3 * blk.w)
			{
				this.buf = new byte[3 * blk.w];
			}

			red = this.barr[0];
			green = this.barr[1];
			blue = this.barr[2];

			try
			{
				// Read line by line
				mi = blk.uly + blk.h;
				for (i = blk.uly; i < mi; i++)
				{
					// Reposition in input offset takes care of
					// header offset
					this.in.seek(this.offset + (long) i * 3 * this.w + 3L * blk.ulx);
					this.in.read(this.buf, 0, 3 * blk.w);

					for (k = (i - blk.uly) * blk.w + blk.w - 1, j = 3 * blk.w - 1; 0 <= j; k--)
					{
						// Read every third sample
						blue[k] = (this.buf[j] & 0xFF) - ImgReaderPPM.DC_OFFSET;
						j--;
						green[k] = (this.buf[j] & 0xFF) - ImgReaderPPM.DC_OFFSET;
						j--;
						red[k] = (this.buf[j] & 0xFF) - ImgReaderPPM.DC_OFFSET;
						j--;
					}
				}
			}
			catch (final IOException e)
			{
				JJ2KExceptionHandler.handleException(e);
			}
			this.barr[0] = red;
			this.barr[1] = green;
			this.barr[2] = blue;

			// Set buffer attributes
			blk.setData(this.barr[c]);
			blk.offset = 0;
			blk.scanw = blk.w;
		}
		else
		{ // Asking for the 2nd or 3rd block component
			blk.setData(this.barr[c]);
			blk.offset = (blk.ulx - this.dbi.ulx) * this.dbi.w + blk.ulx - this.dbi.ulx;
			blk.scanw = this.dbi.scanw;
		}

		// Turn off the progressive attribute
		blk.progressive = false;
		return blk;
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 * 
	 * <P>
	 * After being read the coefficients are level shifted by subtracting
	 * 2^(nominal bit range - 1)
	 * 
	 * <P>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 * 
	 * <P>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 * 
	 * <P>
	 * The returned data has its 'progressive' attribute unset (i.e. false).
	 * 
	 * <P>
	 * When an I/O exception is encountered the JJ2KExceptionHandler is used.
	 * The exception is passed to its handleException method. The action that is
	 * taken depends on the action that has been registered in
	 * JJ2KExceptionHandler. See JJ2KExceptionHandler for details.
	 * 
	 * @param blk
	 *            Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must have the
	 *            correct dimensions. If it contains a null data array a new one
	 *            is created. The fields in this object are modified to return
	 *            the data.
	 * 
	 * @param c
	 *            The index of the component from which to get the data. Only
	 *            0,1 and 2 are valid.
	 * 
	 * @return The requested DataBlk
	 * 
	 * @see #getInternCompData
	 * 
	 * @see JJ2KExceptionHandler
	 */
	@Override
	public final DataBlk getCompData(DataBlk blk, final int c)
	{
		// NOTE: can not directly call getInternCompData since that returns
		// internally buffered data.
		final int w;
		final int h;

		// Check type of block provided as an argument
		if (DataBlk.TYPE_INT != blk.getDataType())
		{
			final DataBlkInt tmp = new DataBlkInt(blk.ulx, blk.uly, blk.w, blk.h);
			blk = tmp;
		}

		int[] bakarr = (int[]) blk.getData();
		// Save requested block size
		w = blk.w;
		h = blk.h;
		// Force internal data buffer to be different from external
		blk.setData(null);
		this.getInternCompData(blk, c);
		// Copy the data
		if (null == bakarr)
		{
			bakarr = new int[w * h];
		}
		if (0 == blk.offset && blk.scanw == w)
		{
			// Requested and returned block buffer are the same size
			System.arraycopy(blk.getData(), 0, bakarr, 0, w * h);
		}
		else
		{ // Requested and returned block are different
			for (int i = h - 1; 0 <= i; i--)
			{ // copy line by line
				System.arraycopy(blk.getData(), blk.offset + i * blk.scanw, bakarr, i * w, w);
			}
		}
		blk.setData(bakarr);
		blk.offset = 0;
		blk.scanw = blk.w;
		return blk;
	}

	/**
	 * Returns a byte read from the RandomAccessFile. The number of read byted
	 * are counted to keep track of the offset of the pixel data in the PPM file
	 * 
	 * @return One byte read from the header of the PPM file.
	 */
	private byte countedByteRead() throws IOException {
		this.offset++;
		return this.in.readByte();
	}

	/**
	 * Checks that the file begins with 'P6'
	 */
	private void confirmFileType() throws IOException {
		final byte[] type = { 80, 54 };
		int i;
		byte b;

		for (i = 0; 2 > i; i++)
		{
			b = this.countedByteRead();
			if (b != type[i])
			{
				if (1 == i && 51 == b)
				{ // i.e 'P3'
					throw new IllegalArgumentException("JJ2000 does not support ascii-PPM files. Use "
							+ " raw-PPM file instead. ");
				}
				throw new IllegalArgumentException("Not a raw-PPM file");
			}
		}
	}

	/**
	 * Skips any line in the header starting with '#' and any space, tab, line
	 * feed or carriage return.
	 */
	private void skipCommentAndWhiteSpace() throws IOException {

		boolean done = false;
		byte b;

		while (!done)
		{
			b = this.countedByteRead();
			if (35 == b)
			{ // Comment start
				while (10 != b && 13 != b)
				{ // While not comment end (end-of-line)
					b = this.countedByteRead();
				}
			}
			else if (!(9 == b || 10 == b || 13 == b || 32 == b))
			{ // If not whitespace
				done = true;
			}
		}
		// Put back last valid byte
		this.offset--;
		this.in.seek(this.offset);
	}

	/**
	 * Returns an int read from the header of the PPM file.
	 * 
	 * @return One int read from the header of the PPM file.
	 */
	private int readHeaderInt() throws IOException {
		int res = 0;
		byte b = 0;

		b = this.countedByteRead();
		while (32 != b && 10 != b && 9 != b && 13 != b)
		{ // While not whitespace
			res = res * 10 + b - 48; // Convert from ASCII to decimal
			b = this.countedByteRead();
		}
		return res;
	}

	/**
	 * Returns true if the data read was originally signed in the specified
	 * component, false if not. This method always returns false since PPM data
	 * is always unsigned.
	 * 
	 * @param c
	 *            The index of the component, from 0 to N-1.
	 * 
	 * @return always false, since PPM data is always unsigned.
	 */
	@Override
	public boolean isOrigSigned(final int c)
	{
		// Check component index
		if (0 > c || 2 < c)
			throw new IllegalArgumentException();
		return false;
	}

	/**
	 * Returns a string of information about the object, more than 1 line long.
	 * The information string includes information from the underlying
	 * RandomAccessFile (its toString() method is called in turn).
	 * 
	 * @return A string of information about the object.
	 */
	@Override
	public String toString()
	{
		return "ImgReaderPPM: WxH = " + this.w + "x" + this.h + ", Component = 0,1,2\nUnderlying RandomAccessFile:\n"
				+ this.in.toString();
	}
}
