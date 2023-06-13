/*
 * CVS identifier:
 *
 * $Id: ImgWriterPGX.java,v 1.14 2002/07/19 14:10:46 grosbois Exp $
 *
 * Class:                   ImgWriterPGX
 *
 * Description:             Image Writer for PGX files (custom file format
 *                          for VM3A)
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.output;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * This class extends the ImgWriter abstract class for writing PGX files. PGX is
 * a custom monochrome file format invented specifically to simplify the use of
 * VM3A with images of different bit-depths in the range 1 to 31 bits per pixel.
 * 
 * <p>
 * The file consists of a one line text header followed by the data.
 * 
 * <p>
 * <u>Header:</u> "PG"+ <i>ws</i> +&lt;<i>endianess</i>&gt;+ <i>ws</i>
 * +[<i>sign</i>]+<i>ws</i> + &lt;<i>bit-depth</i>&gt;+"
 * "+&lt;<i>width</i>&gt;+" "+&lt;<i>height</i>&gt;+'\n'
 * 
 * <p>
 * where:<br>
 * <ul>
 * <li><i>ws</i> (white-spaces) is any combination of characters ' ' and '\t'.</li>
 * <li><i>endianess</i> equals "LM" or "ML"(resp. little-endian or big-endian)</li>
 * <li><i>sign</i> equals "+" or "-" (resp. unsigned or signed). If omited,
 * values are supposed to be unsigned.</li>
 * <li><i>bit-depth</i> that can be any number between 1 and 31.</li>
 * <li><i>width</i> and <i>height</i> are the image dimensions (in pixels).</li>
 * </ul>
 * 
 * <u>Data:</u> The image binary values appear one after the other (in raster
 * order) immediately after the last header character ('\n') and are
 * byte-aligned (they are packed into 1,2 or 4 bytes per sample, depending upon
 * the bit-depth value).
 * 
 * <p>
 * If the data is unsigned, level shifting is applied adding 2^(bit depth - 1)
 * 
 * <p>
 * <u>NOTE</u>: This class is not thread safe, for reasons of internal
 * buffering.
 * 
 * @see ImgWriter
 * 
 * @see BlkImgDataSrc
 */
public class ImgWriterPGX extends ImgWriter
{

	/**
	 * Used during saturation (2^bitdepth-1 if unsigned, 2^(bitdepth-1)-1 if
	 * signed)
	 */
	int maxVal;

	/** Used during saturation (0 if unsigned, -2^(bitdepth-1) if signed) */
	int minVal;

	/** Used with level-shiting */
	int levShift;

	/**
	 * Whether the data must be signed when writing or not. In the latter case
	 * inverse level shifting must be applied
	 */
	boolean isSigned;

	/** The bit-depth of the input file (must be between 1 and 31) */
	private final int bitDepth;

	/** Where to write the data */
	private RandomAccessFile out;

	/** The offset of the raw pixel data in the PGX file */
	private final int offset;

	/**
	 * A DataBlk, just used to avoid allocating a new one each time it is needed
	 */
	private DataBlkInt db = new DataBlkInt();

	/** The number of fractional bits in the source data */
	private final int fb;

	/** The index of the component from where to get the data */
	private final int c;

	/**
	 * The pack length of one sample (in bytes, according to the output
	 * bit-depth
	 */
	private final int packBytes;

	/** The line buffer. */
	// This makes the class not thrad safe
	// (but it is not the only one making it so)
	private byte[] buf;

	/**
	 * Creates a new writer to the specified File object, to write data from the
	 * specified component.
	 * 
	 * <p>
	 * The size of the image that is written to the file is the size of the
	 * component from which to get the data, specified by b, not the size of the
	 * source image (they differ if there is some sub-sampling).
	 * 
	 * <p>
	 * All the header informations are given by the BlkImgDataSrc source
	 * (component width, component height, bit-depth) and sign flag, which are
	 * provided to the constructor. The endianness is always big-endian (MSB
	 * first).
	 * 
	 * @param out
	 *            The file where to write the data
	 * 
	 * @param imgSrc
	 *            The source from where to get the image data to write.
	 * 
	 * @param c
	 *            The index of the component from where to get the data.
	 * 
	 * @param isSigned
	 *            Whether the datas are signed or not (needed only when writing
	 *            header).
	 * 
	 * @see DataBlk
	 */
	public ImgWriterPGX(final File out, final BlkImgDataSrc imgSrc, final int c, final boolean isSigned) throws IOException
	{
		// Initialize
		this.c = c;
		if (out.exists() && !out.delete())
		{
			throw new IOException("Could not reset file");
		}
		this.out = new RandomAccessFile(out, "rw");
		this.isSigned = isSigned;
		this.src = imgSrc;
		this.w = this.src.getImgWidth();
		this.h = this.src.getImgHeight();
		this.fb = imgSrc.getFixedPoint(c);

		this.bitDepth = this.src.getNomRangeBits(this.c);
		if ((0 >= bitDepth) || (31 < bitDepth))
		{
			throw new IOException("PGX supports only bit-depth between " + "1 and 31");
		}
		if (8 >= bitDepth)
		{
			this.packBytes = 1;
		}
		else if (16 >= bitDepth)
		{
			this.packBytes = 2;
		}
		else
		{ // <= 31
			this.packBytes = 4;
		}

		// Writes PGX header
		final String tmpString = "PG " + "ML " // Always writing big-endian
				+ ((this.isSigned) ? "- " : "+ ") // signed/unsigned
				+ this.bitDepth + " " // bit-depth
				+ this.w + " " // component width
				+ this.h + "\n"; // component height

		final byte[] tmpByte = tmpString.getBytes(StandardCharsets.UTF_8);
		for (int i = 0; i < tmpByte.length; i++)
		{
			this.out.write(tmpByte[i]);
		}

		this.offset = tmpByte.length;
		this.maxVal = this.isSigned ? ((1 << (this.src.getNomRangeBits(c) - 1)) - 1) : ((1 << this.src.getNomRangeBits(c)) - 1);
		this.minVal = this.isSigned ? (-1 * (1 << (this.src.getNomRangeBits(c) - 1))) : 0;

		this.levShift = (this.isSigned) ? 0 : 1 << (this.src.getNomRangeBits(c) - 1);
	}

	/**
	 * Creates a new writer to the specified file, to write data from the
	 * specified component.
	 * 
	 * <p>
	 * The size of the image that is written to the file is the size of the
	 * component from which to get the data, specified by b, not the size of the
	 * source image (they differ if there is some sub-sampling).
	 * 
	 * <p>
	 * All header information is given by the BlkImgDataSrc source (component
	 * width, component height, bit-depth) and sign flag, which are provided to
	 * the constructor. The endianness is always big-endian (MSB first).
	 * 
	 * @param fname
	 *            The name of the file where to write the data
	 * 
	 * @param imgSrc
	 *            The source from where to get the image data to write.
	 * 
	 * @param c
	 *            The index of the component from where to get the data.
	 * 
	 * @param isSigned
	 *            Whether the datas are signed or not (needed only when writing
	 *            header).
	 * 
	 * @see DataBlk
	 */
	public ImgWriterPGX(final String fname, final BlkImgDataSrc imgSrc, final int c, final boolean isSigned) throws IOException
	{
		this(new File(fname), imgSrc, c, isSigned);
	}

	/**
	 * Closes the underlying file or netwrok connection to where the data is
	 * written. Any call to other methods of the class become illegal after a
	 * call to this one.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public void close() throws IOException
	{
		int i;
		// Finish writing the file, writing 0s at the end if the data at end
		// has not been written.
		if (this.out.length() != (long) this.w * this.h * this.packBytes + this.offset)
		{
			// Goto end of file
			this.out.seek(this.out.length());
			// Fill with 0s
			for (i = this.offset + this.w * this.h * this.packBytes - (int) this.out.length(); 0 < i; i--)
			{
				this.out.writeByte(0);
			}
		}
		this.out.close();
		this.src = null;
		this.out = null;
		this.db = null;
	}

	/**
	 * Writes all buffered data to the file or resource.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public void flush() throws IOException
	{
		// No flush is needed since we use RandomAccessFile
		// Get rid of line buffer (is this a good choice?)
		this.buf = null;
	}

	/**
	 * Writes the data of the specified area to the file, coordinates are
	 * relative to the current tile of the source. Before writing, the
	 * coefficients are limited to the nominal range and packed into 1,2 or 4
	 * bytes (according to the bit-depth).
	 * 
	 * <p>
	 * If the data is unisigned, level shifting is applied adding 2^(bit depth -
	 * 1)
	 * 
	 * <p>
	 * This method may not be called concurrently from different threads.
	 * 
	 * <p>
	 * If the data returned from the BlkImgDataSrc source is progressive, then
	 * it is requested over and over until it is not progressive anymore.
	 * 
	 * @param ulx
	 *            The horizontal coordinate of the upper-left corner of the area
	 *            to write, relative to the current tile.
	 * 
	 * @param uly
	 *            The vertical coordinate of the upper-left corner of the area
	 *            to write, relative to the current tile.
	 * 
	 * @param w
	 *            The width of the area to write.
	 * 
	 * @param h
	 *            The height of the area to write.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public void write(final int ulx, final int uly, final int w, final int h) throws IOException
	{
		int k, i, j;
		final int fracbits = this.fb; // In local variable for faster access
		final int tOffx;  // Active tile offset in the X and Y direction
		final int tOffy;

		// Initialize db
		this.db.ulx = ulx;
		this.db.uly = uly;
		this.db.w = w;
		this.db.h = h;
		// Get the current active tile offset
		tOffx = this.src.getCompULX(this.c) - (int) Math.ceil(this.src.getImgULX() / (double) this.src.getCompSubsX(this.c));
		tOffy = this.src.getCompULY(this.c) - (int) Math.ceil(this.src.getImgULY() / (double) this.src.getCompSubsY(this.c));
		// Check the array size
		if (null != db.data && this.db.data.length < w * h)
		{
			// A new one will be allocated by getInternCompData()
			this.db.data = null;
		}
		// Request the data and make sure it is not
		// progressive
		do
		{
			this.db = (DataBlkInt) this.src.getInternCompData(this.db, this.c);
		} while (this.db.progressive);

		int tmp;

		// Check line buffer
		if (null == buf || this.buf.length < this.packBytes * w)
		{
			this.buf = new byte[this.packBytes * w]; // Expand buffer
		}

		switch (this.packBytes)
		{

			case 1: // Samples packed into 1 byte
				// Write line by line
				for (i = 0; i < h; i++)
				{
					// Skip to beggining of line in file
					this.out.seek(this.offset + (long) this.w * (uly + tOffy + i) + ulx + tOffx);
					// Write all bytes in the line
					if (0 == fracbits)
					{
						for (k = this.db.offset + i * this.db.scanw + w - 1, j = w - 1; 0 <= j; k--)
						{
							tmp = this.db.data[k] + this.levShift;
							this.buf[j] = (byte) ((tmp < this.minVal) ? this.minVal : ((tmp > this.maxVal) ? this.maxVal : tmp));
							j--;
						}
					}
					else
					{
						for (k = this.db.offset + i * this.db.scanw + w - 1, j = w - 1; 0 <= j; k--)
						{
							tmp = (this.db.data[k] >>> fracbits) + this.levShift;
							this.buf[j] = (byte) ((tmp < this.minVal) ? this.minVal : ((tmp > this.maxVal) ? this.maxVal : tmp));
							j--;
						}
					}
					this.out.write(this.buf, 0, w);
				}
				break;

			case 2: // Samples packed in to 2 bytes (short)
				// Write line by line
				for (i = 0; i < h; i++)
				{

					// Skip to beggining of line in file
					this.out.seek(this.offset + 2 * ((long) this.w * (uly + tOffy + i) + ulx + tOffx));
					// Write all bytes in the line
					if (0 == fracbits)
					{
						for (k = this.db.offset + i * this.db.scanw + w - 1, j = (w << 1) - 1; 0 <= j; k--)
						{
							tmp = this.db.data[k] + this.levShift;
							tmp = (tmp < this.minVal) ? this.minVal : ((tmp > this.maxVal) ? this.maxVal : tmp);
							this.buf[j] = (byte) tmp; // no need for 0xFF mask
							j--;
							// since
													// truncation will do it
													// already
							this.buf[j] = (byte) (tmp >>> 8);
							j--;
						}
					}
					else
					{
						for (k = this.db.offset + i * this.db.scanw + w - 1, j = (w << 1) - 1; 0 <= j; k--)
						{
							tmp = (this.db.data[k] >>> fracbits) + this.levShift;
							tmp = (tmp < this.minVal) ? this.minVal : ((tmp > this.maxVal) ? this.maxVal : tmp);
							this.buf[j] = (byte) tmp; // no need for 0xFF mask
							j--;
							// since
													// truncation will do it
													// already
							this.buf[j] = (byte) (tmp >>> 8);
							j--;
						}
					}
					this.out.write(this.buf, 0, w << 1);
				}
				break;

			case 4:
				// Write line by line
				for (i = 0; i < h; i++)
				{
					// Skip to beggining of line in file
					this.out.seek(this.offset + 4 * ((long) this.w * (uly + tOffy + i) + ulx + tOffx));
					// Write all bytes in the line
					if (0 == fracbits)
					{
						for (k = this.db.offset + i * this.db.scanw + w - 1, j = (w << 2) - 1; 0 <= j; k--)
						{
							tmp = this.db.data[k] + this.levShift;
							tmp = (tmp < this.minVal) ? this.minVal : ((tmp > this.maxVal) ? this.maxVal : tmp);
							this.buf[j] = (byte) tmp; // No need to use 0xFF
							j--;
							this.buf[j] = (byte) (tmp >>> 8); // masks since
							j--;
							// truncation
							this.buf[j] = (byte) (tmp >>> 16); // will have already
							j--;
							// the
							this.buf[j] = (byte) (tmp >>> 24); // same effect
							j--;
						}
					}
					else
					{
						for (k = this.db.offset + i * this.db.scanw + w - 1, j = (w << 2) - 1; 0 <= j; k--)
						{
							tmp = (this.db.data[k] >>> fracbits) + this.levShift;
							tmp = (tmp < this.minVal) ? this.minVal : ((tmp > this.maxVal) ? this.maxVal : tmp);
							this.buf[j] = (byte) tmp; // No need to use 0xFF
							j--;
							this.buf[j] = (byte) (tmp >>> 8); // masks since
							j--;
							// truncation
							this.buf[j] = (byte) (tmp >>> 16); // will have already
							j--;
							// the
							this.buf[j] = (byte) (tmp >>> 24); // same effect
							j--;
						}
					}
					this.out.write(this.buf, 0, w << 2);
				}
				break;

			default:
				throw new IOException("PGX supports only bit-depth between " + "1 and 31");
		}

	}

	/**
	 * Writes the source's current tile to the output. The requests of data
	 * issued to the source BlkImgDataSrc object are done by strips, in order to
	 * reduce memory usage.
	 * 
	 * <p>
	 * If the data returned from the BlkImgDataSrc source is progressive, then
	 * it is requested over and over until it is not progressive anymore.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs.
	 * 
	 * @see DataBlk
	 */
	@Override
	public void write() throws IOException
	{
		int i;
		final int tIdx = this.src.getTileIdx();
		final int tw = this.src.getTileCompWidth(tIdx, this.c); // Tile width
		final int th = this.src.getTileCompHeight(tIdx, this.c); // Tile height
		// Write in strips
		for (i = 0; i < th; i += ImgWriter.DEF_STRIP_HEIGHT)
		{
			this.write(0, i, tw, (DEF_STRIP_HEIGHT > th - i) ? th - i : ImgWriter.DEF_STRIP_HEIGHT);
		}
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
		return "ImgWriterPGX: WxH = " + this.w + "x" + this.h + ", Component = " + this.c + ", Bit-depth = " + this.bitDepth
				+ ", signed = " + this.isSigned + "\nUnderlying RandomAccessFile:\n" + this.out.toString();
	}
}
