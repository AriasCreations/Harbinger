/*
 * CVS identifier:
 *
 * $Id: StreamMsgLogger.java,v 1.11 2000/09/05 09:25:30 grosbois Exp $
 *
 * Class:                   StreamMsgLogger
 *
 * Description:             Implementation of MsgLogger for streams
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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * This class implements the MsgLogger interface for streams. Streams can be
 * simple files, terminals, stdout, stderr, etc. The messages or simple strings
 * are formatted using the linewidth given to the constructor.
 *
 * <p>
 * Messages are printed to the 'err' stream if they are of severity WARNING or
 * ERROR, otherwise they are printed to the 'out' stream. Simple strings are
 * always printed the 'out' stream.
 */
public class StreamMsgLogger implements MsgLogger {

	/**
	 * The 'out' stream
	 */
	private final PrintWriter out;

	/**
	 * The 'err' stream
	 */
	private final PrintWriter err;

	/**
	 * The printer that formats the text
	 */
	private final MsgPrinter mp;

	/**
	 * Constructs a StreamMsgLogger that uses 'outstr' as the 'out' stream, and
	 * 'errstr' as the 'err' stream. Note that 'outstr' and 'errstr' can be
	 * System.out and System.err.
	 *
	 * @param outstr Where to print simple strings and LOG and INFO messages.
	 * @param errstr Where to print WARNING and ERROR messages
	 * @param lw     The line width to use in formatting
	 */
	public StreamMsgLogger ( final OutputStream outstr , final OutputStream errstr , final int lw ) {
		this.out = new PrintWriter ( outstr , true , StandardCharsets.UTF_8 );
		this.err = new PrintWriter ( errstr , true , StandardCharsets.UTF_8 );
		this.mp = new MsgPrinter ( lw );
	}

	/**
	 * Constructs a StreamMsgLogger that uses 'outstr' as the 'out' stream, and
	 * 'errstr' as the 'err' stream. Note that 'outstr' and 'errstr' can be
	 * System.out and System.err.
	 *
	 * @param outstr Where to print simple strings and LOG and INFO messages.
	 * @param errstr Where to print WARNING and ERROR messages
	 * @param lw     The line width to use in formatting
	 */
	public StreamMsgLogger ( final Writer outstr , final Writer errstr , final int lw ) {
		this.out = new PrintWriter ( outstr , true );
		this.err = new PrintWriter ( errstr , true );
		this.mp = new MsgPrinter ( lw );
	}

	/**
	 * Constructs a StreamMsgLogger that uses 'outstr' as the 'out' stream, and
	 * 'errstr' as the 'err' stream. Note that 'outstr' and 'errstr' can be
	 * System.out and System.err.
	 *
	 * @param outstr Where to print simple strings and LOG and INFO messages.
	 * @param errstr Where to print WARNING and ERROR messages
	 * @param lw     The line width to use in formatting
	 */
	public StreamMsgLogger ( final PrintWriter outstr , final PrintWriter errstr , final int lw ) {
		this.out = outstr;
		this.err = errstr;
		this.mp = new MsgPrinter ( lw );
	}

	/**
	 * Prints the message 'msg' to the output device, appending a newline, with
	 * severity 'sev'. The severity of the message is prepended to the message.
	 *
	 * @param sev The message severity (LOG, INFO, etc.)
	 * @param msg The message to display
	 */
	@Override
	public void printmsg ( final int sev , final String msg ) {
		final PrintWriter lout;
		final String prefix;

		switch ( sev ) {
			case MsgLogger.LOG:
				prefix = "[LOG]: ";
				lout = this.out;
				break;
			case MsgLogger.INFO:
				prefix = "[INFO]: ";
				lout = this.out;
				break;
			case MsgLogger.WARNING:
				prefix = "[WARNING]: ";
				lout = this.err;
				break;
			case MsgLogger.ERROR:
				prefix = "[ERROR]: ";
				lout = this.err;
				break;
			default:
				throw new IllegalArgumentException ( "Severity " + sev + " not valid." );
		}
		this.mp.print ( lout , 0 , prefix.length ( ) , prefix + msg );
		lout.flush ( );
	}

	/**
	 * Prints the string 'str' to the 'out' stream, appending a newline. The
	 * message is reformatted to the line width given to the constructors and
	 * using 'flind' characters to indent the first line and 'ind' characters to
	 * indent the second line. However, any newlines appearing in 'str' are
	 * respected. The output device may or may not display the string until
	 * flush() is called, depending on the autoflush state of the PrintWriter,
	 * to be sure flush() should be called to write the string to the device.
	 * This method just prints the string, the string does not make part of a
	 * "message" in the sense that noe severity is associated to it.
	 *
	 * @param str   The string to print
	 * @param flind Indentation of the first line
	 * @param ind   Indentation of any other lines.
	 */
	@Override
	public void println ( final String str , final int flind , final int ind ) {
		this.mp.print ( this.out , flind , ind , str );
	}

	/**
	 * Writes any buffered data from the print() and println() methods to the
	 * device.
	 */
	@Override
	public void flush ( ) {
		this.out.flush ( );
	}
}
