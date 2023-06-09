/*
 * CVS identifier:
 *
 * $Id: ImgKeyListener.java,v 1.8 2000/09/21 16:12:41 dsanta Exp $
 *
 * Class:                   ImgKeyListener
 *
 * Description:             Handles the key events for zooming and scrolling
 *                          an image displayed in an ImgScrollPane.
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
 *
 *
 *
 */
package dev.zontreck.harbinger.thirdparty.jj2000.disp;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.decoder.Decoder;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class implements the listener for keyboard events for the JJ2000 decoder
 * that displays in a ImgScrollPane.
 *
 * <p>
 * The following key sequences are defined: <br>
 * - <up arrow>: scroll image up<br>
 * - <down arrow>: scroll image down<br>
 * - <left arrow>: scroll image left<br>
 * - <right arrow>: scroll image right<br>
 * - <page up>: scroll image up by a vertical block increment<br>
 * - <page down>: scroll image down by a vertical block increment<br>
 * - 'Q' or 'q': exit the application<br>
 * - '-': zoom out by 2<br>
 * - '=' or '+': zoom in by 2<br>
 * - '1': display at a zoom of 1<br>
 * - 'Ctrl-C': exit the application<br>
 *
 * <p>
 * The amount by which the arrows scroll the image is determined by the modifier
 * keys. If the Ctrl key is held down, the block increment will be used,
 * otherwise the unit increment will. If the Shift key is held down then the
 * increment is multiplied by ACCEL_FACTOR. That is the Ctrl key selects between
 * unit and block increments, while the Shift key selects between normal and
 * accelerated mode.
 */
public class ImgKeyListener extends KeyAdapter {

	/**
	 * The acceleration factor when the shift key is pressed: 10
	 */
	public static final int ACCEL_FACTOR = 10;
	/**
	 * Frame used to display tools
	 */
	Frame helpFrame;
	/**
	 * The component where the image is displayed
	 */
	ImgScrollPane isp;
	/**
	 * Decoder instance
	 */
	Decoder dec;

	/**
	 * Instantiate a new ImgKeyListener that will work on the specified
	 * ImgScrollPane.
	 *
	 * @param isp The image scroll pane on which the actions should operate.
	 * @param dec The decoder instance
	 */
	public ImgKeyListener ( final ImgScrollPane isp , final Decoder dec ) {
		this.isp = isp;
		this.dec = dec;
	}

	/**
	 * Create a help TextArea concerning the tools associated with keys.
	 *
	 * @return The help TextArea
	 */
	private static TextArea getHelp ( ) {
		final TextArea ta = new TextArea ( "" , 17 , 61 , TextArea.SCROLLBARS_NONE );
		ta.setEditable ( false );
		ta.setFont ( new Font ( "Monospaced" , Font.PLAIN , 10 ) );

		ta.append ( "The following key sequences are recognized in the \n" + "image display window:\n\n" );
		ta.append ( "'-'           : zoom out by a factor of 2.\n" );
		ta.append ( "'+' or '='    : zoom in by a factor of 2.\n" );
		ta.append ( "'1'           : set the zoom factor to 1 (i.e. no zoom).\n" );
		ta.append ( "<up arrow>    : scroll the image up by one pixel.\n" );
		ta.append ( "<down arrow>  : scroll the image down by one pixel.\n" );
		ta.append ( "<left arrow>  : scroll the image left by one pixel.\n" );
		ta.append ( "<right arrow> : scroll the image right by one pixel.\n" );
		ta.append ( "<page up>     : scroll the image up by a whole page.\n" );
		ta.append ( "<page down>   : scroll the image down by a whole page.\n" );
		ta.append ( "Ctrl+<arrow>  : scroll in the direction of the arrow a \n"
				+ "                page at a time instead of a pixel at a time.\n" );
		ta.append ( "Shift+<arrow> : accelerate the scroll speed by 10.\n" );
		ta.append ( "'Q' or 'q'    : exit the application.\n" );
		ta.append ( "'Ctrl-C'      : exit the application.\n" );

		return ta;
	}

	/**
	 * Hanldes the keyPressed events. Normal keys are not treated here because
	 * some don't have a defined keycode (as incredible as it might seem!).
	 *
	 * @param evt The key event to process.
	 */
	@Override
	public void keyPressed ( final KeyEvent evt ) {
		final Adjustable adj;

		// Do nothing if consumed
		if ( evt.isConsumed ( ) )
			return;

		// Perform action based on key
		switch ( evt.getKeyCode ( ) ) {
			case KeyEvent.VK_LEFT: // Move left
				adj = this.isp.getHAdjustable ( );
				adj.setValue ( adj.getValue ( ) - this.calcIncrement ( evt , adj ) );
				break;
			case KeyEvent.VK_RIGHT: // Move right
				adj = this.isp.getHAdjustable ( );
				adj.setValue ( adj.getValue ( ) + this.calcIncrement ( evt , adj ) );
				break;
			case KeyEvent.VK_UP: // Move up
				adj = this.isp.getVAdjustable ( );
				adj.setValue ( adj.getValue ( ) - this.calcIncrement ( evt , adj ) );
				break;
			case KeyEvent.VK_DOWN: // Move down
				adj = this.isp.getVAdjustable ( );
				adj.setValue ( adj.getValue ( ) + this.calcIncrement ( evt , adj ) );
				break;
			case KeyEvent.VK_PAGE_UP: // Move up by a page
				adj = this.isp.getVAdjustable ( );
				adj.setValue ( adj.getValue ( ) - adj.getBlockIncrement ( ) );
				break;
			case KeyEvent.VK_PAGE_DOWN: // Move down by a page
				adj = this.isp.getVAdjustable ( );
				adj.setValue ( adj.getValue ( ) + adj.getBlockIncrement ( ) );
				break;
			case KeyEvent.VK_C:
				// Exit if ctrl is pressed
				if ( evt.isControlDown ( ) ) {
					this.dec.exit ( );
				}
				break;
			default:
				return;
		}

		// Consume the event so nothing else is done
		evt.consume ( );
	}

	/**
	 * Handles the key typed event. Normal (i.e. "text") keys are handled here.
	 *
	 * @param evt The key event to process.
	 */
	@Override
	public void keyTyped ( final KeyEvent evt ) {

		// Do nothing if consumed
		if ( evt.isConsumed ( ) )
			return;

		// Perform action based on key
		switch ( evt.getKeyChar ( ) ) {
			case '+':
			case '=':
				// Zoom in
				this.isp.zoom ( 2.0f );
				break;
			case '-':
				// Zoom out
				this.isp.zoom ( 0.5f );
				break;
			case '1':
				// Set zoom to 1
				this.isp.setZoom ( 1.0f );
				break;
			case 'q':
			case 'Q':
				// Exit
				this.dec.exit ( );
				break;
			case 'h':
			case 'H':
				// Help display
				if ( null == helpFrame ) {
					this.helpFrame = new Frame ( "Tools" );
					this.helpFrame.add ( ImgKeyListener.getHelp ( ) );
					this.helpFrame.pack ( );
					this.helpFrame.setResizable ( false );
					this.helpFrame.addWindowListener ( new WindowAdapter ( ) {
						@Override
						public void windowClosing ( final WindowEvent e ) {
							ImgKeyListener.this.helpFrame.setVisible ( false );
						}
					} );
				}
				this.helpFrame.setVisible ( ! this.helpFrame.isVisible ( ) );
				break;
			default:
				return;
		}

		// Consume the event so nothing else is done
		evt.consume ( );
	}

	/**
	 * Returns the increment based on the modifier keys of the KeyEvent. If
	 * control is down then the Adjustable block increment is used, otherwise
	 * the unit one is used. If the shift key is down the increment is
	 * multiplied by ACCEL_FACTOR.
	 *
	 * @param evt The KeyEvent fom where to obtain the modifiers
	 * @param adj The Adjustable from where to obtain the increments
	 */
	private int calcIncrement ( final KeyEvent evt , final Adjustable adj ) {
		int incr;
		// Control selects block instead of unit
		if ( evt.isControlDown ( ) ) {
			incr = adj.getBlockIncrement ( );
		}
		else {
			incr = adj.getUnitIncrement ( );
		}
		// Shift accelerates by ACCEL_FACTOR
		if ( evt.isShiftDown ( ) ) {
			incr *= ImgKeyListener.ACCEL_FACTOR;
		}
		return incr;
	}
}
