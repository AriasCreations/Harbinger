/*
 * CVS identifier:
 *
 * $Id: CodestreamManipulator.java,v 1.17 2001/05/16 13:58:09 qtxjoas Exp $
 *
 * Class:                   CodestreamManipulator
 *
 * Description:             Manipulates codestream to create tile-parts etc
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.util;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.Markers;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.io.RandomAccessIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * This class takes a legal JPEG 2000 codestream and performs some manipulation
 * on it. Currently the manipulations supported are: Tile-parts
 */
public class CodestreamManipulator {
	/**
	 * Flag indicating whether packed packet headers in main header is used
	 */
	private final boolean ppmUsed;

	/**
	 * Flag indicating whether packed packet headers in tile headers is used
	 */
	private final boolean pptUsed;

	/**
	 * Flag indicating whether SOP marker was only intended for parsing in This
	 * class and should be removed
	 */
	private final boolean tempSop;

	/**
	 * Flag indicating whether EPH marker was only intended for parsing in This
	 * class and should be removed
	 */
	private final boolean tempEph;

	/**
	 * The number of tiles in the image
	 */
	private int nt;

	/**
	 * The number of packets per tile-part
	 */
	private int pptp;

	/**
	 * The name of the outfile
	 */
	private final RandomAccessIO output;

	/**
	 * The length of a SOT plus a SOD marker
	 */
	private static final int TP_HEAD_LEN = 14;

	/** The maximum number of a tile part index (TPsot) */
//	private static int MAX_TPSOT = 16;

	/**
	 * The maximum number of tile parts in any tile
	 */
	private int maxtp;

	/**
	 * The number of packets per tile
	 */
	private int[] ppt = new int[this.nt];

	/**
	 * The positions of the SOT, SOP and EPH markers
	 */
	private Integer[] positions;

	/**
	 * The main header
	 */
	private byte[] mainHeader;

	/**
	 * Buffers containing the tile parts
	 */
	private byte[][][] tileParts;

	/**
	 * Buffers containing the original tile headers
	 */
	private byte[][] tileHeaders;

	/**
	 * Buffers contaning the packet headers
	 */
	private byte[][][] packetHeaders;

	/**
	 * Buffers containing the packet data
	 */
	private byte[][][] packetData;

	/**
	 * Buffers containing the SOP marker segments
	 */
	private byte[][][] sopMarkSeg;

	/**
	 * Instantiates a codestream manipulator..
	 *
	 * @param nt      The number of tiles in the image
	 * @param pptp    Packets per tile-part. If zero, no division into tileparts is performed
	 * @param ppm     Flag indicating that PPM marker is used
	 * @param ppt     Flag indicating that PPT marker is used
	 * @param tempSop Flag indicating whether SOP merker should be removed
	 * @param tempEph Flag indicating whether EPH merker should be removed
	 */
	public CodestreamManipulator(final RandomAccessIO output, final int nt, final int pptp, final boolean ppm, final boolean ppt, final boolean tempSop, final boolean tempEph) {
		this.output = output;
		this.nt = nt;
		this.pptp = pptp;
		ppmUsed = ppm;
		pptUsed = ppt;
		this.tempSop = tempSop;
		this.tempEph = tempEph;
	}

	/**
	 * This method performs the actual manipulation of the codestream which is
	 * the reparsing for tile parts and packed packet headers
	 *
	 * @return The number of bytes that the file has increased by
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	public int doCodestreamManipulation() throws IOException {
		int addedHeaderBytes = 0;
		this.ppt = new int[this.nt];
		this.tileParts = new byte[this.nt][][];
		this.tileHeaders = new byte[this.nt][];
		this.packetHeaders = new byte[this.nt][][];
		this.packetData = new byte[this.nt][][];
		this.sopMarkSeg = new byte[this.nt][][];

		// If neither packed packet header nor tile parts are used, return 0
		if (!this.ppmUsed && !this.pptUsed && 0 == pptp) return 0;

		addedHeaderBytes = -this.output.length();

		// Parse the codestream for SOT, SOP and EPH markers
		this.parseAndFind(this.output);

		// Read and buffer the tile headers, packet headers and packet data
		this.readAndBuffer(this.output);

		// Create tile-parts
		this.createTileParts();

		// Write new codestream
		this.writeNewCodestream(this.output);

		// Close file
		this.output.flush();
		addedHeaderBytes += this.output.length();

		return addedHeaderBytes;
	}

	/**
	 * This method parses the codestream for SOT, SOP and EPH markers and
	 * removes header bits signalling SOP and EPH markers if packed packet
	 * headers are used
	 *
	 * @param fi The file to parse the markers from
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void parseAndFind(final RandomAccessIO fi) throws IOException {
		int length, pos, i, t /*, sop = 0, eph = 0*/;
		short marker;
		int halfMarker;
		int tileEnd;
		final Vector<Integer> markPos = new Vector<Integer>();

		// Find position of first SOT marker
		marker = (short) fi.readUnsignedShort(); // read SOC marker
		marker = (short) fi.readUnsignedShort();
		while (Markers.SOT != marker) {
			pos = fi.getPos();
			length = fi.readUnsignedShort();

			// If SOP and EPH markers were only used for parsing in this
			// class remove SOP and EPH markers from Scod field
			if (Markers.COD == marker) {
				int scod = fi.readUnsignedByte();
				if (this.tempSop)
					scod &= 0xfd; // Remove bits indicating SOP
				if (this.tempEph)
					scod &= 0xfb; // Remove bits indicating SOP
				fi.seek(pos + 2);
				fi.write(scod);
			}

			fi.seek(pos + length);
			marker = (short) fi.readUnsignedShort();
		}
		pos = fi.getPos();
		fi.seek(pos - 2);

		// Find all packet headers, packed data and tile headers
		for (t = 0; t < this.nt; t++) {
			// Read SOT marker
			fi.readUnsignedShort(); // Skip SOT
			pos = fi.getPos();
			markPos.addElement(Integer.valueOf(fi.getPos()));
			fi.readInt(); // Skip Lsot and Isot
			length = fi.readInt(); // Read Psot
			fi.readUnsignedShort(); // Skip TPsot & TNsot
			tileEnd = pos + length - 2; // Last byte of tile

			// Find position of SOD marker
			marker = (short) fi.readUnsignedShort();
			while (Markers.SOD != marker) {
				pos = fi.getPos();
				length = fi.readUnsignedShort();

				// If SOP and EPH markers were only used for parsing in this
				// class remove SOP and EPH markers from Scod field
				if (Markers.COD == marker) {
					int scod = fi.readUnsignedByte();
					if (this.tempSop)
						scod &= 0xfd; // Remove bits indicating SOP
					if (this.tempEph)
						scod &= 0xfb; // Remove bits indicating SOP
					fi.seek(pos + 2);
					fi.write(scod);
				}
				fi.seek(pos + length);
				marker = (short) fi.readUnsignedShort();
			}

			// Find all SOP and EPH markers in tile
//			sop = 0;
//			eph = 0;

			i = fi.getPos();
			while (i < tileEnd) {
				halfMarker = (short) fi.readUnsignedByte();
				if ((short) 0xff == halfMarker) {
					marker = (short) (((short) 0xff << 8) + fi.readUnsignedByte());
					i++;
					if (Markers.SOP == marker) {
						markPos.addElement(Integer.valueOf(fi.getPos()));
						this.ppt[t]++;
//						sop++;
						fi.skipBytes(4);
						i += 4;
					}

					if (Markers.EPH == marker) {
						markPos.addElement(Integer.valueOf(fi.getPos()));
//						eph++;
					}
				}
				i++;
			}
		}
		markPos.addElement(Integer.valueOf(fi.getPos() + 2));
		this.positions = new Integer[markPos.size()];
		markPos.copyInto(this.positions);
	}

	/**
	 * This method reads and buffers the tile headers, packet headers and packet
	 * data.
	 *
	 * @param fi The file to read the headers and data from
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void readAndBuffer(final RandomAccessIO fi) throws IOException {
		int p, prem, length, t, markIndex;

		// Buffer main header
		length = this.positions[0].intValue() - 2;
		this.mainHeader = new byte[length];
		fi.readFully(this.mainHeader, 0, length);
		markIndex = 0;

		for (t = 0; t < this.nt; t++) {
			prem = this.ppt[t];

			this.packetHeaders[t] = new byte[prem][];
			this.packetData[t] = new byte[prem][];
			this.sopMarkSeg[t] = new byte[prem][];

			// Read tile header
			length = this.positions[markIndex + 1].intValue() - this.positions[markIndex].intValue();
			this.tileHeaders[t] = new byte[length];
			fi.readFully(this.tileHeaders[t], 0, length);
			markIndex++;

			for (p = 0; p < prem; p++) {
				// Read packet header
				length = this.positions[markIndex + 1].intValue() - this.positions[markIndex].intValue();

				if (this.tempSop) { // SOP marker is skipped
					length -= Markers.SOP_LENGTH;
					fi.skipBytes(Markers.SOP_LENGTH);
				} else { // SOP marker is read and buffered
					length -= Markers.SOP_LENGTH;
					this.sopMarkSeg[t][p] = new byte[Markers.SOP_LENGTH];
					fi.readFully(this.sopMarkSeg[t][p], 0, Markers.SOP_LENGTH);
				}

				if (!this.tempEph) { // EPH marker is kept in header
					length += Markers.EPH_LENGTH;
				}
				this.packetHeaders[t][p] = new byte[length];
				fi.readFully(this.packetHeaders[t][p], 0, length);
				markIndex++;

				// Read packet data
				length = this.positions[markIndex + 1].intValue() - this.positions[markIndex].intValue();

				length -= Markers.EPH_LENGTH;
				if (this.tempEph) { // EPH marker is used and is skipped
					fi.skipBytes(Markers.EPH_LENGTH);
				}

				this.packetData[t][p] = new byte[length];
				fi.readFully(this.packetData[t][p], 0, length);
				markIndex++;
			}
		}
	}

	/**
	 * This method creates the tileparts from the buffered tile headers, packet
	 * headers and packet data
	 *
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void createTileParts() throws IOException {
		int i, prem, t, length;
		int pIndex;
		int tppStart;
		int tilePart;
		int p, np, nomnp;
		int numTileParts;
		final ByteArrayOutputStream temp = new ByteArrayOutputStream();
		byte[] tempByteArr;

		// Create tile parts
		this.tileParts = new byte[this.nt][][];
		this.maxtp = 0;

		for (t = 0; t < this.nt; t++) {
			// Calculate number of tile parts. If tileparts are not used,
			// put all packets in the first tilepart
			if (0 == pptp) this.pptp = this.ppt[t];
			prem = this.ppt[t];
			numTileParts = (int) Math.ceil(((double) prem) / this.pptp);
			// numPackets = packetHeaders[t].length;
			this.maxtp = (numTileParts > this.maxtp) ? numTileParts : this.maxtp;
			this.tileParts[t] = new byte[numTileParts][];

			// Create all the tile parts for tile t
			tppStart = 0;
			pIndex = 0;
			p = 0;

			for (tilePart = 0; tilePart < numTileParts; tilePart++) {

				// Calculate number of packets in this tilepart
				nomnp = (this.pptp > prem) ? prem : this.pptp;
				np = nomnp;

				// Write tile part header
				if (0 == tilePart) {
					// Write original tile part header up to SOD marker
					temp.write(this.tileHeaders[t], 0, this.tileHeaders[t].length - 2);
				} else {
					// Write empty header of length TP_HEAD_LEN-2
					temp.write(new byte[CodestreamManipulator.TP_HEAD_LEN - 2], 0, CodestreamManipulator.TP_HEAD_LEN - 2);
				}

				// Write PPT marker segments if PPT used
				if (this.pptUsed) {
					int pptLength = 3; // Zppt and Lppt
					int pptIndex = 0;
					int phLength;

					p = pIndex;
					while (0 < np) {
						phLength = this.packetHeaders[t][p].length;

						// If the total legth of the packet headers is greater
						// than MAX_LPPT, several PPT markers are needed
						if (Markers.MAX_LPPT < pptLength + phLength) {
							temp.write(Markers.PPT >>> 8);
							temp.write(Markers.PPT);
							temp.write(pptLength >>> 8);
							temp.write(pptLength);
							temp.write(pptIndex);
							pptIndex++;
							for (i = pIndex; i < p; i++) {
								temp.write(this.packetHeaders[t][i], 0, this.packetHeaders[t][i].length);
							}
							pptLength = 3; // Zppt and Lppt
							pIndex = p;
						}
						pptLength += phLength;
						p++;
						np--;
					}
					// Write last PPT marker
					temp.write(Markers.PPT >>> 8);
					temp.write(Markers.PPT);
					temp.write(pptLength >>> 8);
					temp.write(pptLength);
					temp.write(pptIndex);
					for (i = pIndex; i < p; i++) {

						temp.write(this.packetHeaders[t][i], 0, this.packetHeaders[t][i].length);
					}
				}
				pIndex = p;
				np = nomnp;

				// Write SOD marker
				temp.write(Markers.SOD >>> 8);
				temp.write(Markers.SOD);

				// Write packet data and packet headers if PPT and PPM not used
				for (p = tppStart; p < tppStart + np; p++) {
					if (!this.tempSop) {
						temp.write(this.sopMarkSeg[t][p], 0, Markers.SOP_LENGTH);
					}

					if (!(this.ppmUsed || this.pptUsed)) {
						temp.write(this.packetHeaders[t][p], 0, this.packetHeaders[t][p].length);
					}

					temp.write(this.packetData[t][p], 0, this.packetData[t][p].length);
				}
				tppStart += np;

				// Edit tile part header
				tempByteArr = temp.toByteArray();
				this.tileParts[t][tilePart] = tempByteArr;
				length = temp.size();

				if (0 == tilePart) {
					// Edit first tile part header
					tempByteArr[6] = (byte) (length >>> 24); // Psot
					tempByteArr[7] = (byte) (length >>> 16);
					tempByteArr[8] = (byte) (length >>> 8);
					tempByteArr[9] = (byte) (length);
					tempByteArr[10] = (0); // TPsot
					tempByteArr[11] = (byte) (numTileParts); // TNsot
				} else {
					// Edit tile part header
					tempByteArr[0] = (byte) (Markers.SOT >>> 8); // SOT
					tempByteArr[1] = (byte) (Markers.SOT);
					tempByteArr[2] = (0); // Lsot
					tempByteArr[3] = (10);
					tempByteArr[4] = (byte) (t >> 8); // Isot
					tempByteArr[5] = (byte) (t);
					tempByteArr[6] = (byte) (length >>> 24); // Psot
					tempByteArr[7] = (byte) (length >>> 16);
					tempByteArr[8] = (byte) (length >>> 8);
					tempByteArr[9] = (byte) (length);
					tempByteArr[10] = (byte) (tilePart); // TPsot
					tempByteArr[11] = (byte) (numTileParts); // TNsot
				}
				temp.reset();
				prem -= np;
			}
		}
		temp.close();
	}

	/**
	 * This method writes the new codestream to the file.
	 *
	 * @param fi The file to write the new codestream to
	 * @throws java.io.IOException If an I/O error occurred.
	 */
	private void writeNewCodestream(final RandomAccessIO fi) throws IOException {
		int t, p, tp;
		final int numTiles = this.tileParts.length;
		final int[][] packetHeaderLengths = new int[numTiles][this.maxtp];
		byte[] temp;
		int length;

		// Write main header up to SOT marker
		fi.write(this.mainHeader, 0, this.mainHeader.length);

		// If PPM used write all packet headers in PPM markers
		if (this.ppmUsed) {
			final ByteArrayOutputStream ppmMarkerSegment = new ByteArrayOutputStream();
			int numPackets;
			int totNumPackets;
			int ppmIndex = 0;
			int ppmLength;
			int pStart, pStop;
			final int[] prem = new int[numTiles];

			// Set number of remaining packets
			for (t = 0; t < numTiles; t++) {
				prem[t] = this.packetHeaders[t].length;
			}

			// Calculate Nppm values
			for (tp = 0; tp < this.maxtp; tp++) {
				for (t = 0; t < numTiles; t++) {
					if (this.tileParts[t].length > tp) {
						totNumPackets = this.packetHeaders[t].length;
						// Calculate number of packets in this tilepart
						numPackets = (tp == this.tileParts[t].length - 1) ? prem[t] : this.pptp;

						pStart = totNumPackets - prem[t];
						pStop = pStart + numPackets;

						// Calculate number of packet header bytes for this
						// tile part
						for (p = pStart; p < pStop; p++)
							packetHeaderLengths[t][tp] += this.packetHeaders[t][p].length;

						prem[t] -= numPackets;
					}
				}
			}

			// Write first PPM marker
			ppmMarkerSegment.write(Markers.PPM >>> 8);
			ppmMarkerSegment.write(Markers.PPM);
			ppmMarkerSegment.write(0); // Temporary Lppm value
			ppmMarkerSegment.write(0); // Temporary Lppm value
			ppmMarkerSegment.write(0); // zppm
			ppmLength = 3;
			ppmIndex++;

			// Set number of remaining packets
			for (t = 0; t < numTiles; t++)
				prem[t] = this.packetHeaders[t].length;

			// Write all PPM markers and information
			for (tp = 0; tp < this.maxtp; tp++) {
				for (t = 0; t < numTiles; t++) {

					if (this.tileParts[t].length > tp) {
						totNumPackets = this.packetHeaders[t].length;

						// Calculate number of packets in this tilepart
						numPackets = (tp == this.tileParts[t].length - 1) ? prem[t] : this.pptp;

						pStart = totNumPackets - prem[t];
						pStop = pStart + numPackets;

						// If Nppm value wont fit in current PPM marker segment
						// write current PPM marker segment and start new
						if (Markers.MAX_LPPM < ppmLength + 4) {
							// Write current PPM marker
							temp = ppmMarkerSegment.toByteArray();
							length = temp.length - 2;
							temp[2] = (byte) (length >>> 8);
							temp[3] = (byte) length;
							fi.write(temp, 0, length + 2);

							// Start new PPM marker segment
							ppmMarkerSegment.reset();
							ppmMarkerSegment.write(Markers.PPM >>> 8);
							ppmMarkerSegment.write(Markers.PPM);
							ppmMarkerSegment.write(0); // Temporary Lppm value
							ppmMarkerSegment.write(0); // Temporary Lppm value
							ppmMarkerSegment.write(ppmIndex); // zppm
							ppmIndex++;
							ppmLength = 3;
						}

						// Write Nppm value
						length = packetHeaderLengths[t][tp];
						ppmMarkerSegment.write(length >>> 24);
						ppmMarkerSegment.write(length >>> 16);
						ppmMarkerSegment.write(length >>> 8);
						ppmMarkerSegment.write(length);
						ppmLength += 4;

						// Write packet headers
						for (p = pStart; p < pStop; p++) {
							length = this.packetHeaders[t][p].length;

							// If next packet header value wont fit in
							// current PPM marker segment write current PPM
							// marker segment and start new
							if (Markers.MAX_LPPM < ppmLength + length) {
								// Write current PPM marker
								temp = ppmMarkerSegment.toByteArray();
								length = temp.length - 2;
								temp[2] = (byte) (length >>> 8);
								temp[3] = (byte) length;
								fi.write(temp, 0, length + 2);

								// Start new PPM marker segment
								ppmMarkerSegment.reset();
								ppmMarkerSegment.write(Markers.PPM >>> 8);
								ppmMarkerSegment.write(Markers.PPM);
								ppmMarkerSegment.write(0); // Temp Lppm value
								ppmMarkerSegment.write(0); // Temp Lppm value
								ppmMarkerSegment.write(ppmIndex); // zppm
								ppmIndex++;
								ppmLength = 3;
							}

							// write packet header
							ppmMarkerSegment.write(this.packetHeaders[t][p], 0, this.packetHeaders[t][p].length);
							ppmLength += this.packetHeaders[t][p].length;
						}
						prem[t] -= numPackets;
					}
				}
			}
			// Write last PPM marker segment
			temp = ppmMarkerSegment.toByteArray();
			length = temp.length - 2;
			temp[2] = (byte) (length >>> 8);
			temp[3] = (byte) length;
			fi.write(temp, 0, length + 2);
		}

		// Write tile parts interleaved
		for (tp = 0; tp < this.maxtp; tp++) {
			for (t = 0; t < this.nt; t++) {
				if (this.tileParts[t].length > tp) {
					temp = this.tileParts[t][tp];
					length = temp.length;
					fi.write(temp, 0, length);
				}
			}
		}
		fi.writeShort(Markers.EOC);
	}
}
