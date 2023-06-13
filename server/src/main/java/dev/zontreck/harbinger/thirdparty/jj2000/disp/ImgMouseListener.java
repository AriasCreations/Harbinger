/*
 * CVS identifier:
 *
 * $Id: ImgMouseListener.java,v 1.7 2000/09/21 16:12:42 dsanta Exp $
 *
 * Class:                   ImgMouseListener
 *
 * Description:             Handles the mouse events for scrolling an image
 *                          displayed in an ImgScrollPane.
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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class handles the dragging of an image displayed in an ImgScrollPane.
 * When the mouse is dragged the image scrolls accordingly.
 *
 * <p>
 * Objects of this class must be registerd as both mouse listener and mouse
 * motion listener.
 *
 * <p>
 * While the dragging is taking place the cursor is changed to the MOVE_CURSOR
 * type. The original cursor is restored when the mouse is released after the
 * drag.
 */
public class ImgMouseListener extends MouseAdapter {
	/**
	 * The component where the image is displayed
	 */
	ImgScrollPane isp;

	/**
	 * The horizontal coordinate where the drag starts
	 */
	int startMouseX;

	/**
	 * The vertical coordinate where the drag starts
	 */
	int startMouseY;

	/**
	 * The horizontal scroll position when the drag started
	 */
	int startScrollX;

	/**
	 * The vertical scroll position when the drag started
	 */
	int startScrollY;

	Cursor prevCursor;

	/**
	 * Instantiate a new ImgMouseListener that will work on the specified
	 * ImgScrollPane.
	 *
	 * @param isp The image scroll pane on which the actions should operate.
	 */
	public ImgMouseListener ( final ImgScrollPane isp ) {
		this.isp = isp;
	}

	@Override
	public void mousePressed ( final MouseEvent e ) {
		// Get the possibly start drag position
		this.startMouseX = e.getX ( );
		this.startMouseY = e.getY ( );
		// Get the start scroll position
		this.startScrollX = this.isp.getHAdjustable ( ).getValue ( );
		this.startScrollY = this.isp.getVAdjustable ( ).getValue ( );
	}

	@Override
	public void mouseReleased ( final MouseEvent e ) {
		// Restore the last cursor, if any
		if ( null != prevCursor ) {
			this.isp.setCursor ( this.prevCursor );
			this.prevCursor = null;
		}
	}

	@Override
	public void mouseDragged ( final MouseEvent evt ) {
		final int scrollX;
		final int scrollY;

		// Set the drag cursor
		if ( null == prevCursor ) {
			this.prevCursor = this.isp.getCursor ( );
			this.isp.setCursor ( Cursor.getPredefinedCursor ( Cursor.MOVE_CURSOR ) );
		}

		// Calculate new scroll position and set it
		scrollX = this.startScrollX + this.startMouseX - evt.getX ( );
		scrollY = this.startScrollY + this.startMouseY - evt.getY ( );
		this.isp.setScrollPosition ( scrollX , scrollY );
	}

	@Override
	public void mouseMoved ( final MouseEvent evt ) {
	}
}
