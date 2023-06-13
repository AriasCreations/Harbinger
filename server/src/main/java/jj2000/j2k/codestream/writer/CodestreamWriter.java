/*
 * CVS identifier:
 *
 * $Id: CodestreamWriter.java,v 1.11 2001/07/24 17:03:30 grosbois Exp $
 *
 * Class:                   CodestreamWriter
 *
 * Description:             Interface for writing bit streams
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
package jj2000.j2k.codestream.writer;

import java.io.IOException;
import java.io.OutputStream;

import jj2000.j2k.codestream.Markers;

/**
 * This is the class for writing to a codestream. A codestream corresponds to
 * headers (main and tile-parts) and packets. Each packet has a head and a body.
 * The codestream always has a maximum number of bytes that can be written to it.
 * After that many number of bytes no more data is written to the codestream but
 * the number of bytes is counted so that the value returned by getMaxAvailableBytes()
 * is negative. If the number of bytes is unlimited a ridicoulosly large value,
 * such as Integer.MAX_VALUE, is equivalent.
 * 
 * <p>
 * Data writing to the codestream can be simulated. In this case, no byte is
 * effectively written to the codestream but the resulting number of bytes is
 * calculated and returned (although it is not accounted in the bit stream).
 * This can be used in rate control loops.
 * 
 * <p>
 * Implementing classes should write the header of the bit stream before writing
 * any packets. The bit stream header can be written with the help of the
 * HeaderEncoder class.
 * 
 * @see HeaderEncoder
 */
public class CodestreamWriter
{
	/** The upper limit for the value of the Nsop field of the SOP marker */
	private static final int SOP_MARKER_LIMIT = 65535;

	/** The maximum number of bytes that can be written to the bit stream */
	protected int maxBytes;

	/** The output stream to write */
	protected OutputStream out;

	/**
	 * The number of bytes already written to the codestream, excluding the
	 * header length, magic number and header length info.
	 */
	int ndata;

	/** The default buffer length, 1024 bytes */
	public static int DEF_BUF_LEN = 1024;

	/** Array used to store the SOP markers values */
	byte[] sopMarker;

	/** Array used to store the EPH markers values */
	byte[] ephMarker;

	/**
	 * The packet index (when start of packet markers i.e. SOP markers) are
	 * used.
	 */
	int packetIdx;

	/** Offset of end of last packet containing ROI information */
	private int offLastROIPkt;

	/** Length of last packets containing no ROI information */
	private int lenLastNoROI;

	/**
	 * Allocates this object and initializes the maximum number of bytes.
	 * 
	 * @param mb
	 *            The maximum number of bytes that can be written to the
	 *            codestream.
	 */
	public CodestreamWriter(final OutputStream out, final int mb)
	{
		this.out = out;
		maxBytes = mb;
		this.initSOP_EPHArrays();
	}

	/**
	 * Returns the number of bytes remaining available in the codestream. This
	 * is the maximum allowed number of bytes minus the number of bytes that
	 * have already been written to the bit stream. If more bytes have been
	 * written to the bit stream than the maximum number of allowed bytes, then
	 * a negative value is returned.
	 * 
	 * @return The number of bytes remaining available in the bit stream.
	 */
	public final int getMaxAvailableBytes()
	{
		return this.maxBytes - this.ndata;
	}

	/**
	 * Returns the current length of the entire codestream.
	 * 
	 * @return the current length of the codestream
	 */
	public int getLength()
	{
		if (0 <= getMaxAvailableBytes())
		{
			return this.ndata;
		}
		return this.maxBytes;
	}

	/**
	 * Writes a packet head into the codestream and returns the number of bytes
	 * used by this header. If in simulation mode then no data is effectively
	 * written to the codestream but the number of bytes is calculated. This can
	 * be used for iterative rate allocation.
	 * 
	 * <p>
	 * If the number of bytes that has to be written to the codestream is more
	 * than the space left (as returned by getMaxAvailableBytes()), only the
	 * data that does not exceed the allowed length is effectively written and
	 * the rest is discarded. However the value returned by the method is the
	 * total length of the packet, as if all of it was written to the bit
	 * stream.
	 * 
	 * <p>
	 * If the codestream header has not been commited yet and if 'sim' is false,
	 * then the bit stream header is automatically commited (see
	 * commitBitstreamHeader() method) before writting the packet.
	 * 
	 * @param head
	 *            The packet head data.
	 * 
	 * @param hlen
	 *            The number of bytes in the packet head.
	 * 
	 * @param sim
	 *            Simulation mode flag. If true nothing is written to the bit
	 *            stream, but the number of bytes that would be written is
	 *            returned.
	 * 
	 * @param sop
	 *            Start of packet header marker flag. This flag indicates
	 *            whether or not SOP markers should be written. If true, SOP
	 *            markers should be written, if false, they should not.
	 * 
	 * @param eph
	 *            End of Packet Header marker flag. This flag indicates whether
	 *            or not EPH markers should be written. If true, EPH markers
	 *            should be written, if false, they should not.
	 * 
	 * @return The number of bytes spent by the packet head.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs while writing to the output stream.
	 * 
	 * @see #commitBitstreamHeader
	 */
	public int writePacketHead(final byte[] head, final int hlen, final boolean sim, final boolean sop, final boolean eph) throws IOException
	{
		int len = hlen + (sop ? Markers.SOP_LENGTH : 0) + (eph ? Markers.EPH_LENGTH : 0);

		// If not in simulation mode write the data
		if (!sim)
		{
			// Write the head bytes
			if (this.getMaxAvailableBytes() < len)
			{
				len = this.getMaxAvailableBytes();
			}

			if (0 < len)
			{
				// Write Start Of Packet header markers if necessary
				if (sop)
				{
					// The first 4 bytes of the array have been filled in the
					// classe's constructor.
					this.sopMarker[4] = (byte) (this.packetIdx >> 8);
					this.sopMarker[5] = (byte) (this.packetIdx);
					this.out.write(this.sopMarker, 0, Markers.SOP_LENGTH);
					this.packetIdx++;
					if (SOP_MARKER_LIMIT < packetIdx)
					{
						// Reset SOP value as we have reached its upper limit
						this.packetIdx = 0;
					}
				}
				this.out.write(head, 0, hlen);
				// Update data length
				this.ndata += len;

				// Write End of Packet Header markers if necessary
				if (eph)
				{
					this.out.write(this.ephMarker, 0, Markers.EPH_LENGTH);
				}

				// Deal with ROI Information
				this.lenLastNoROI += len;
			}
		}
		return len;
	}

	/**
	 * Writes a packet body to the codestream and returns the number of bytes
	 * used by this body. If in simulation mode then no data is written to the
	 * bit stream but the number of bytes is calculated. This can be used for
	 * iterative rate allocation.
	 * 
	 * <p>
	 * If the number of bytes that has to be written to the codestream is more
	 * than the space left (as returned by getMaxAvailableBytes()), only the
	 * data that does not exceed the allowed length is effectively written and
	 * the rest is discarded. However the value returned by the method is the
	 * total length of the packet, as if all of it was written to the bit
	 * stream.
	 * 
	 * @param body
	 *            The packet body data.
	 * 
	 * @param blen
	 *            The number of bytes in the packet body.
	 * 
	 * @param sim
	 *            Simulation mode flag. If true nothing is written to the bit
	 *            stream, but the number of bytes that would be written is
	 *            returned.
	 * 
	 * @param roiInPkt
	 *            Whether or not there is ROI information in this packet
	 * 
	 * @param roiLen
	 *            Number of byte to read in packet body to get all the ROI
	 *            information
	 * 
	 * @return The number of bytes spent by the packet body.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs while writing to the output stream.
	 * 
	 * @see #commitBitstreamHeader
	 */
	public int writePacketBody(final byte[] body, final int blen, final boolean sim, final boolean roiInPkt, final int roiLen) throws IOException
	{
		int len = blen;

		// If not in simulation mode write the data
		if (!sim)
		{
			// Write the body bytes
			if (this.getMaxAvailableBytes() < len)
			{
				len = this.getMaxAvailableBytes();
			}
			if (0 < len)
			{
				this.out.write(body, 0, len);
			}
			// Update data length
			this.ndata += blen;

			// Deal with ROI information
			if (roiInPkt)
			{
				this.offLastROIPkt += this.lenLastNoROI + roiLen;
				this.lenLastNoROI = len - roiLen;
			}
			else
			{
				this.lenLastNoROI += len;
			}
		}
		return len;
	}

	/**
	 * Terminates the codestream, writing the EOC marker. Does not close the underlaying stream.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs while writing to the resource.
	 */
	public void terminate() throws IOException
	{
		if (2 < getMaxAvailableBytes())
		{
			// Write the EOC marker.
			this.out.write(Markers.EOC >> 8);
			this.out.write(Markers.EOC);
		}
		this.ndata += 2; // Add two to length of codestream for EOC marker
	}

	/**
	 * Writes the header data to the bit stream, if it has not been already
	 * done. In some implementations this method can be called only once, and an
	 * IllegalArgumentException is thrown if called more than once.
	 * 
	 * @exception IOException
	 *                If an I/O error occurs while writing the data.
	 * 
	 * @exception IllegalArgumentException
	 *                If this method has already been called.
	 */
	public void commitBitstreamHeader(final HeaderEncoder he) throws IOException
	{
		final int len = he.getLength();
		// Actualize ndata

		he.writeTo(this.out); // Write the header


		this.ndata += len;
		// Reset packet index used for SOP markers
		this.packetIdx = 0;

		// Deal with ROI information
		this.lenLastNoROI += he.getLength();
	}

	/**
	 * Gives the offset of the end of last packet containing ROI information
	 * 
	 * @return End of last ROI packet
	 */
	public int getOffLastROIPkt()
	{
		return this.offLastROIPkt;
	}

	/**
	 * Performs the initialisation of the arrays that are used to store the
	 * values used to write SOP and EPH markers
	 */
	private void initSOP_EPHArrays()
	{

		// Allocate and set first values of SOP marker as they will not be
		// modified
		this.sopMarker = new byte[Markers.SOP_LENGTH];
		this.sopMarker[0] = (Markers.SOP >> 8);
		this.sopMarker[1] = (byte) Markers.SOP;
		this.sopMarker[2] = 0x00;
		this.sopMarker[3] = 0x04;

		// Allocate and set values of EPH marker as they will not be
		// modified
		this.ephMarker = new byte[Markers.EPH_LENGTH];
		this.ephMarker[0] = (Markers.EPH >> 8);
		this.ephMarker[1] = (byte) Markers.EPH;
	}
}
