/* 
 * CVS identifier:
 * 
 * $Id: ImgScrollPane.java,v 1.10 2000/12/04 17:19:27 grosbois Exp $
 * 
 * Class:                   ImgScrollPane
 * 
 * Description:             <short description of class>
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

import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;

/**
 * This class implements an image viewer that can display an image larger than
 * the actual display area, and presents scrollbars to scroll the viewable area.
 * This class also supports zooming in and out the image, with no extra memory
 * requirements.
 * 
 * <P>
 * The zoom factor by default is 1. It can be changed with the 'zoom()' and
 * 'setZoom()' methods. The maximum zoom factor is defined by MAX_ZOOM.
 * 
 * <P>
 * The zoom scaling is done directly by the AWT display system. In general it is
 * performed by dropping or repeating lines. It is just intended to provide a
 * display zoom and not for proper scaling of an image.
 * 
 * <P>
 * The scrolling can be performed by copying the actual displayed data to the
 * new scrolled position and redrawing the damaged parts, or by redrawing the
 * entire displayed image portion at the new scrolled position. Which is more
 * efficient depends on the JVM and working environment. By default it is done
 * by copying since it tends to provide less annoying visual artifacts while
 * scrolling, but that can be changed with 'setCopyScroll()'.
 * 
 * <P>
 * This class is very similar to the AWT ScrollPane one, but it is optimized for
 * display of large images and does not suffer from the problems of ScrollPane
 * when changing zoom. The Adjustable elements that represent the scrollbars are
 * made available as in ScrollPane, but the minimum, maximum, visible amount and
 * block increment should not be set (IllegalArgumentException is thrown if
 * attempted), since they are set internally by this class.
 * 
 * <P>
 * Focus and key event listeners that are registered are in fact registered with
 * the components that implement the three areas (the image display and the two
 * scrollbars) so that if any such event is fired in any of these areas it is
 * handled by the registered listener.
 * 
 * <P>
 * Mouse and mouse movement event listeners that are registered are in fact
 * registered with the image display component only. The mouse and mouse
 * movement events on the scrollbars are handled by the Scrollbar default
 * listeners only.
 * 
 * <P>
 * Although it is implemented as a container, it behaves like a component.
 * Specifically no components can be added or removed from objects of this
 * class. Furthermore, no layout manager can be set. It is internally set and it
 * can not be changed.
 * 
 * <P>
 * The implementation uses a lightweight container with an inner class to
 * display the image itself, and two scrollbars. The layout manager is a
 * BorderLayout.
 * 
 * <P>
 * This class should be really implemented as a Component, but it is implemented
 * as a Container for easyness. It should not be assumed it is a subclass of
 * Container since in the future it might be rewritten as a subclass of
 * Component only.
 * 
 * 
 * @see ScrollPane
 * 
 * @see Adjustable
 */
public class ImgScrollPane extends Container
{
	private static final long serialVersionUID = 1L;

	/** The ID for always visible scrollbars */
	public static final int SCROLLBARS_ALWAYS = ScrollPane.SCROLLBARS_ALWAYS;

	/** The ID for as needed visible scrollbars */
	public static final int SCROLLBARS_AS_NEEDED = ScrollPane.SCROLLBARS_AS_NEEDED;

	/** The ID for never visible scrollbars */
	public static final int SCROLLBARS_NEVER = ScrollPane.SCROLLBARS_NEVER;

	/** The maximum possible zoom factor: 32. */
	// This is used because factors too large cause problems with JVMs.
	public static final float MAX_ZOOM = 32.0f;

	/** The thickness of the scrollbars: 16 pixels */
	static final int SCROLLBAR_THICKNESS = 16;

	/** The internal gap between the elements, in pixels: 0 */
	static final int INTERNAL_GAP = 0;

	/**
	 * The proportion between the visible scrollbar length and the block
	 * increment amount: 0.8
	 */
	static final float BLOCK_INCREMENT_PROPORTION = 0.8f;

	/**
	 * The horizontal scrollbar.
	 * 
	 * @serial
	 */
	ISPScrollbar hsbar;

	/**
	 * The vertical scrollbar.
	 * 
	 * @serial
	 */
	ISPScrollbar vsbar;

	/**
	 * The image display
	 * 
	 * @serial
	 */
	private final ImageScrollDisplay imgDisplay;

	/**
	 * The scrollbar type (always, as needed, etc.)
	 * 
	 * @serial
	 */
	private final int sbType;

	/**
	 * The zoom to use in displaying the image. A factor larger than one
	 * produces a zoom in effect.
	 * 
	 * @serial
	 */
	private float zoom = 1.0f;

	/**
	 * The zoom used in the last scrollbar calculation.
	 * 
	 * @serial
	 */
	private float lastZoom;

	/**
	 * The viewable size used in the last scrollbar calculation.
	 * 
	 * @serial
	 */
	private Dimension lastSize;

	/**
	 * If scrolling is to be done by copying ot not. If not done by copying
	 * everything is redrawn.
	 * 
	 * @serial
	 */
	private boolean copyScroll = true;

	/**
	 * Creates a new ImgScrollPane with SCROLLBARS_AS_NEEDED scrollbars.
	 */
	public ImgScrollPane()
	{
		this(ImgScrollPane.SCROLLBARS_AS_NEEDED);
	}

	/**
	 * Creates a new ImgScrollPane with the specified type of scrollbar
	 * visibility.
	 * 
	 * @param svt
	 *            The scrollbar visibility type
	 */
	public ImgScrollPane(final int svt)
	{
		// Initialize
		super.setLayout(new BorderLayout(ImgScrollPane.INTERNAL_GAP, ImgScrollPane.INTERNAL_GAP));
		this.sbType = svt;
		this.hsbar = new ISPScrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 1);
		this.vsbar = new ISPScrollbar(Scrollbar.VERTICAL, 0, 1, 0, 1);
		this.imgDisplay = new ImageScrollDisplay();
		super.add(this.hsbar, BorderLayout.SOUTH);
		super.add(this.vsbar, BorderLayout.EAST);
		super.add(this.imgDisplay, BorderLayout.CENTER);

		// Set the initial scrollbar visibility
		switch (svt)
		{
			case ImgScrollPane.SCROLLBARS_NEVER:
			case ImgScrollPane.SCROLLBARS_AS_NEEDED:
				this.hsbar.setVisible(false);
				this.vsbar.setVisible(false);
				break;
			case ImgScrollPane.SCROLLBARS_ALWAYS:
				this.hsbar.setVisible(true);
				this.vsbar.setVisible(true);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Sets the image to display in this component. If the image is not ready
	 * for display it will be prepared in the current thread. The current zoom
	 * factor applies.
	 * 
	 * <P>
	 * If the image is not ready for display (i.e. it has not been rendered at
	 * its natural size) it will be rendered in the current thread, if not being
	 * already rendered in another one. This means that the current thread can
	 * block until the image is ready.
	 * 
	 * <P>
	 * If the image is rendered incrementally (it depends on the underlying
	 * 'ImageProducer') it will be displayed in that way if the incremental
	 * display is set for the Component class. See the 'imageUpdate()' method of
	 * the 'Component' class.
	 * 
	 * <P>
	 * If the image is the same as the current one nothing is done.
	 * 
	 * @param img
	 *            The image to display.
	 * 
	 * @see Component#imageUpdate
	 */
	public void setImage(final Image img)
	{
		this.imgDisplay.setImage(img);
	}

	/**
	 * Returns the image that is displayed in this component.
	 * 
	 * @return The image displayed in this component, or null if none.
	 */
	public synchronized Image getImage()
	{
		return this.imgDisplay.img;
	}

	/**
	 * Sets the zoom factor to display the image. A zoom factor larger than 1
	 * corresponds to a zoom in. A factor of 1 corresponds to no scaling. After
	 * setting the zoom factor the component is invalidated and 'repaint()' is
	 * automatically called so that the image is redrawn at the new zoom factor.
	 * In order to revalidate the layout 'validate()' should be called on one of
	 * the parent containers. If the new zoom factor is larger than MAX_ZOOM,
	 * then MAX_ZOOM will be used.
	 * 
	 * @param zf
	 *            The zoom factor
	 */
	public synchronized void setZoom(final float zf)
	{
		if (zf == this.zoom || (MAX_ZOOM < zf && MAX_ZOOM == zoom))
		{
			// No change => do nothing
			return;
		}
		// Set the zoom factor and recalculate the component dimensions
		this.zoom = zf;
		if (MAX_ZOOM < zoom)
			this.zoom = ImgScrollPane.MAX_ZOOM;
		this.setScrollbars();
		// Check if we need to change the scrollbar display
		if (SCROLLBARS_AS_NEEDED == sbType)
			this.doLayout();
		// We need to erase previous scaled image
		this.imgDisplay.erase = true;
		// Redraw image
		this.imgDisplay.repaint();
	}

	/**
	 * Modifies the current zoom factor by the given multiplier. After setting
	 * the zoom factor the component is invalidated and 'repaint()' is
	 * automatically called so that the image is redrawn at the new zoom factor.
	 * In order to revalidate the layout 'validate()' should be called on one of
	 * the parent containers. If the resulting zoom factor is larger than
	 * MAX_ZOOM, then MAX_ZOOM will be used.
	 * 
	 * @param zm
	 *            The zoom multiplier to apply.
	 */
	public synchronized void zoom(final float zm)
	{
		this.setZoom(this.zoom * zm);
	}

	/**
	 * Returns the current zoom factor.
	 * 
	 * @return The current zoom factor
	 */
	public synchronized float getZoom()
	{
		return this.zoom;
	}

	/**
	 * Returns the Adjustable object which represents the state of the
	 * horizontal scrollbar.
	 */
	public Adjustable getHAdjustable()
	{
		return this.hsbar;
	}

	/**
	 * Returns the Adjustable object which represents the state of the vertical
	 * scrollbar.
	 */
	public Adjustable getVAdjustable()
	{
		return this.vsbar;
	}

	/**
	 * Returns the display policy for the scrollbars.
	 * 
	 * @return the display policy for the scrollbars
	 */
	public int getScrollbarDisplayPolicy()
	{
		return this.sbType;
	}

	/**
	 * Sets the display policy for the scrollbars.
	 * 
	 * @param v
	 *            the display policy for the scrollbars
	 */
	public void setScrollbarDisplayPolicy(final int v)
	{
		// If no change do nothing
		if (v == this.sbType)
			return;
		switch (this.sbType)
		{
			case ImgScrollPane.SCROLLBARS_NEVER:
			case ImgScrollPane.SCROLLBARS_AS_NEEDED:
				this.hsbar.setVisible(false);
				this.vsbar.setVisible(false);
				break;
			case ImgScrollPane.SCROLLBARS_ALWAYS:
				this.hsbar.setVisible(true);
				this.vsbar.setVisible(true);
				break;
			default:
				throw new IllegalArgumentException();
		}
		// Now redo the layout
		this.doLayout();
	}

	/**
	 * Scrolls to the specified position within the image. Specifying a position
	 * outside of the legal scrolling bounds of the image will scroll to the
	 * closest legal position. This is a convenience method which interfaces
	 * with the Adjustable objects which represent the state of the scrollbars.
	 * 
	 * @param x
	 *            the x position to scroll to
	 * 
	 * @param y
	 *            the y position to scroll to
	 */
	public synchronized void setScrollPosition(int x, int y)
	{
		this.hsbar.setValueI(x);
		this.vsbar.setValueI(y);
		// Check if we need to repaint
		x = this.hsbar.getValue(); // get the actual value for check
		y = this.vsbar.getValue(); // get the actual value for check
		if (null != imgDisplay.lastUpdateOffset && this.imgDisplay.lastUpdateOffset.x == x
				&& this.imgDisplay.lastUpdateOffset.y == y)
		{
			return; // No change
		}
		// New value changes from last drawn => repaint
		this.imgDisplay.repaint();
	}

	/**
	 * Scrolls to the specified position within the image. Specifying a position
	 * outside of the legal scrolling bounds of the image will scroll to the
	 * closest legal position. This is a convenience method which interfaces
	 * with the Adjustable objects which represent the state of the scrollbars.
	 * 
	 * @param p
	 *            the position to scroll to
	 */
	public synchronized void setScrollPosition(final Point p)
	{
		this.setScrollPosition(p.x, p.y);
	}

	/**
	 * Returns the current x,y position within the child which is displayed at
	 * the 0,0 location of the scrolled panel's view port. This is a convenience
	 * method which interfaces with the adjustable objects which represent the
	 * state of the scrollbars.
	 * 
	 * @return the coordinate position for the current scroll position
	 */
	public Point getScrollPosition()
	{
		return new Point(this.hsbar.getValue(), this.vsbar.getValue());
	}

	/**
	 * Returns the current size of the image scroll pane's view port. This is
	 * the size of the image display area. If this component has not been layed
	 * out yet the value is not defined.
	 * 
	 * @return The size of the image display area
	 */
	public Dimension getViewportSize()
	{
		return this.imgDisplay.getSize();
	}

	/**
	 * Sets if the scrolling is to be done by copying and redrawing of damaged
	 * parts of the displayed image. Otherwise it is done by redrawing the
	 * entire displayed image. In general copy scrolling is faster and produces
	 * less annoying effects. See the class description.
	 * 
	 * @param v
	 *            If true scrolling will be done by copying.
	 */
	public synchronized void setCopyScroll(final boolean v)
	{
		this.copyScroll = v;
	}

	/**
	 * Returns true if the scrolling is done by copying.
	 * 
	 * @return If the copy is done by scrolling
	 */
	public synchronized boolean getCopyScroll()
	{
		return this.copyScroll;
	}

	/**
	 * Causes this container to lay out its components. Most programs should not
	 * call this method directly, but should invoke the validate method instead.
	 */
	@Override
	public synchronized void doLayout()
	{
		// Let's see if we should include the scrollbars or not
		if (SCROLLBARS_AS_NEEDED == sbType && this.imgDisplay.calcDim())
		{
			final Dimension sz = this.getSize();
			final Dimension imsz = this.imgDisplay.getPreferredSize();

			if (sz.width >= imsz.width)
			{
				if (sz.height >= imsz.height)
				{
					// We don't need scrollbars
					this.hsbar.setVisible(false);
					this.vsbar.setVisible(false);
				}
				else
				{
					// We need at least the vertical one, check again for the
					// horizontal.
					this.vsbar.setVisible(true);
					this.hsbar.setVisible(sz.width < imsz.width + ImgScrollPane.SCROLLBAR_THICKNESS);
				}
			}
			else
			{
				// We need at least the horizontal, check for the vertical
				// one.
				this.hsbar.setVisible(true);
				this.vsbar.setVisible(sz.height < imsz.height + ImgScrollPane.SCROLLBAR_THICKNESS);
			}
		}
		// Indicate that we are erasing the image (the doLayout() will erase)
		this.imgDisplay.erase = true;
		// Now do the layout
		super.doLayout();
		// Trick the lower scrollbar: if both scrollbars are showing then
		// shorten the horizontal one so that the traditional empty square
		// appears at the lower right corner. This is probably not the best
		// solution but it works.
		if (this.hsbar.isVisible() && this.vsbar.isVisible())
		{
			final Rectangle b = this.hsbar.getBounds();
			if (SCROLLBAR_THICKNESS + ImgScrollPane.INTERNAL_GAP < b.width)
			{
				b.width -= ImgScrollPane.SCROLLBAR_THICKNESS + ImgScrollPane.INTERNAL_GAP;
			}
			this.hsbar.setBounds(b);
		}
		// We need to calculate the scrollbars with the possibly new size
		this.setScrollbars();
	}

	/**
	 * Adds the specified focus listener to receive focus events from this
	 * component. It is added to the image and scrollbar areas.
	 * 
	 * @param l
	 *            the focus listener
	 */
	@Override
	public synchronized void addFocusListener(final FocusListener l)
	{
		super.addFocusListener(l);
		this.imgDisplay.addFocusListener(l);
		this.hsbar.addFocusListener(l);
		this.vsbar.addFocusListener(l);
	}

	/**
	 * Removes the specified focus listener so that it no longer receives focus
	 * events from this component.
	 * 
	 * @param l
	 *            the focus listener
	 */
	@Override
	public synchronized void removeFocusListener(final FocusListener l)
	{
		super.removeFocusListener(l);
		this.imgDisplay.removeFocusListener(l);
		this.hsbar.removeFocusListener(l);
		this.vsbar.removeFocusListener(l);
	}

	/**
	 * Adds the specified key listener to receive key events from this
	 * component. It is added to the image and scrollbar areas.
	 * 
	 * @param l
	 *            the key listener
	 */
	@Override
	public synchronized void addKeyListener(final KeyListener l)
	{
		super.addKeyListener(l);
		this.imgDisplay.addKeyListener(l);
		this.hsbar.addKeyListener(l);
		this.vsbar.addKeyListener(l);
	}

	/**
	 * Removes the specified key listener so that it no longer receives key
	 * events from this component.
	 * 
	 * @param l
	 *            the key listener
	 */
	@Override
	public synchronized void removeKeyListener(final KeyListener l)
	{
		super.removeKeyListener(l);
		this.imgDisplay.removeKeyListener(l);
		this.hsbar.removeKeyListener(l);
		this.vsbar.removeKeyListener(l);
	}

	/**
	 * Adds the specified mouse listener to receive mouse events from this
	 * component. It is actually added to the image area only and not to the
	 * scrollbar areas.
	 * 
	 * @param l
	 *            the mouse listener
	 */
	@Override
	public synchronized void addMouseListener(final MouseListener l)
	{
		super.addMouseListener(l);
		this.imgDisplay.addMouseListener(l);
	}

	/**
	 * Removes the specified mouse listener so that it no longer receives mouse
	 * events from this component.
	 * 
	 * @param l
	 *            the mouse listener
	 */
	@Override
	public synchronized void removeMouseListener(final MouseListener l)
	{
		super.removeMouseListener(l);
		this.imgDisplay.removeMouseListener(l);
	}

	/**
	 * Adds the specified mouse motion listener to receive mouse motion events
	 * from this component. It is actually added to the image area only and not
	 * to the scrollbar areas.
	 * 
	 * @param l
	 *            the mouse motion listener
	 */
	@Override
	public synchronized void addMouseMotionListener(final MouseMotionListener l)
	{
		super.addMouseMotionListener(l);
		this.imgDisplay.addMouseMotionListener(l);
	}

	/**
	 * Removes the specified mouse motion listener so that it no longer receives
	 * mouse motion events from this component.
	 * 
	 * @param l
	 *            the mouse motion listener
	 */
	@Override
	public synchronized void removeMouseMotionListener(final MouseMotionListener l)
	{
		super.removeMouseMotionListener(l);
		this.imgDisplay.removeMouseMotionListener(l);
	}

	/**
	 * Sets the background color of this component. It sets the background of
	 * the 3 areas (image and scrollbars) plus the container itself.
	 * 
	 * @param c
	 *            The color to become background color for this component
	 */
	@Override
	public synchronized void setBackground(final Color c)
	{
		super.setBackground(c);
		this.imgDisplay.setBackground(c);
		this.hsbar.setBackground(c);
		this.vsbar.setBackground(c);
	}

	/**
	 * Set the cursor image to a predefined cursor. It sets the cursor of the
	 * image area and this container to the specified one. It does not set the
	 * cursor of the scrollbars.
	 * 
	 * @param cursor
	 *            One of the constants defined by the Cursor class.
	 */
	@Override
	public synchronized void setCursor(final Cursor cursor)
	{
		super.setCursor(cursor);
		this.imgDisplay.setCursor(cursor);
	}

	/**
	 * Enables or disables this component, depending on the value of the
	 * parameter b. An enabled component can respond to user input and generate
	 * events. Components are enabled initially by default.
	 * 
	 * @param b
	 *            If true, this component is enabled; otherwise this component
	 *            is disabled.
	 */
	@Override
	public synchronized void setEnabled(final boolean b)
	{
		super.setEnabled(b);
		this.imgDisplay.setEnabled(b);
		this.hsbar.setEnabled(b);
		this.vsbar.setEnabled(b);
	}

	/**
	 * Sets the foreground color of this component. It sets the foreground of
	 * the 3 areas (image display and scrollbars) plus this contaioner's
	 * foreground.
	 * 
	 * @param c
	 *            The color to become this component's foreground color.
	 */
	@Override
	public synchronized void setForeground(final Color c)
	{
		super.setForeground(c);
		this.imgDisplay.setForeground(c);
		this.hsbar.setForeground(c);
		this.vsbar.setForeground(c);
	}

	/**
	 * Throws an IllegalArgumentException since no components can be added to
	 * this container.
	 */
	@Override
	public Component add(final Component comp)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since no components can be added to
	 * this container.
	 */
	@Override
	public Component add(final String name, final Component comp)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since no components can be added to
	 * this container.
	 */
	@Override
	public Component add(final Component comp, final int index)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since no components can be added to
	 * this container.
	 */
	@Override
	public void add(final Component comp, final Object constraints)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since no components can be added to
	 * this container.
	 */
	@Override
	public void add(final Component comp, final Object constraints, final int index)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since the components should never be
	 * removed from this container.
	 */
	@Override
	public void remove(final int index)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since the components should never be
	 * removed from this container.
	 */
	@Override
	public void remove(final Component comp)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since the components should never be
	 * removed from this container.
	 */
	@Override
	public void removeAll()
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Throws an IllegalArgumentException since the layout manager is internally
	 * set and can not be changed.
	 */
	@Override
	public void setLayout(final LayoutManager mgr)
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the scrollbars values, according to the image display area and image
	 * size. The current scroll position is kept.
	 * 
	 */
	private void setScrollbars()
	{
		final Dimension asz; // actual image area size
		final Dimension psz; // preferred size
		int pos; // current scroll position
		final int szx;  // actual image display area size
		final int szy;

		if (!this.imgDisplay.calcDim())
		{
			// While the image dimensions are not known we can't really update
			// the scrollbar.
			return;
		}

		// Get the dimensions
		asz = this.imgDisplay.getSize();
		psz = this.imgDisplay.getPreferredSize();

		// Initialize lastZoom and lastSize if never done yet
		if (0.0f == lastZoom)
			this.lastZoom = this.zoom;
		if (null == lastSize)
			this.lastSize = new Dimension(asz.width, asz.height);

		// Get the actual display size
		szx = (asz.width < psz.width) ? asz.width : psz.width;
		szy = (asz.height < psz.height) ? asz.height : psz.height;

		// Set horizontal scrollbar
		pos = (int) ((this.hsbar.getValue() + this.lastSize.width / 2.0f) / this.lastZoom * this.zoom - szx / 2.0f);
		if (pos > (psz.width - asz.width))
			pos = psz.width - asz.width;
		if (0 > pos)
			pos = 0;
		if (0 >= asz.width)
			asz.width = 1;
		if (0 >= psz.width)
			psz.width = 1;
		this.hsbar.setValues(pos, asz.width, 0, psz.width);
		asz.width = (int) (asz.width * ImgScrollPane.BLOCK_INCREMENT_PROPORTION);
		if (0 >= asz.width)
			asz.width = 1;
		this.hsbar.setBlockIncrementI(asz.width);

		// Set vertical scrollbar
		pos = (int) ((this.vsbar.getValue() + this.lastSize.height / 2.0f) / this.lastZoom * this.zoom - szy / 2.0f);
		if (pos > (psz.height - asz.height))
			pos = psz.height - asz.height;
		if (0 > pos)
			pos = 0;
		if (0 >= asz.height)
			asz.height = 1;
		if (0 >= psz.height)
			psz.height = 1;
		this.vsbar.setValues(pos, asz.height, 0, psz.height);
		asz.height = (int) (asz.height * ImgScrollPane.BLOCK_INCREMENT_PROPORTION);
		if (0 >= asz.height)
			asz.height = 1;
		this.vsbar.setBlockIncrementI(asz.height);

		// Save the zoom and display size used in the scrollbar calculation
		this.lastZoom = this.zoom;
		this.lastSize.width = szx;
		this.lastSize.height = szy;
	}

	/**
	 * This class implements the component that displays the currently viewable
	 * image portion inside the ImgScrollPane. It handles the necessary erasing,
	 * zooming and panning.
	 * 
	 * <P>
	 * NOTE: extending 'Canvas' instead of 'Component' solves the flickering
	 * problem of lightweight components which are in heavyweight containers.
	 * 
	 */
	private class ImageScrollDisplay extends Canvas
	{
		private static final long serialVersionUID = 1L;

		/**
		 * The image to be displayed
		 * 
		 * @serial
		 */
		Image img;

		/**
		 * The preferred size for this component
		 * 
		 * @serial
		 */
		Dimension dim = new Dimension();

		/**
		 * If the current graphics context should be erased prior to drawing the
		 * image. Set when the image and/or zoom factor is changed.
		 * 
		 * @serial
		 */
		boolean erase;

		/**
		 * The image dimensions, without any scaling. Set as soon as they are
		 * known.
		 * 
		 * @serial
		 */
		Dimension imgDim = new Dimension();

		/**
		 * The image dimension flags, as in ImageObserver. The
		 * ImageObserver.WIDTH and ImageObserver.HEIGHT flags are set whenever
		 * the dimension is stored in imgDim. They are reset whenever the image
		 * changes.
		 * 
		 * @serial
		 */
		int dimFlags;

		/**
		 * The last offset used in update().
		 * 
		 * @serial
		 */
		Point lastUpdateOffset;

		/**
		 * Sets the image to display in this component. If the image is not
		 * ready for display it will be prepared in the current thread. The
		 * current zoom factor applies.
		 * 
		 * <P>
		 * If the image is not ready for display (i.e. it has not been rendered
		 * at its natural size) it will be rendered in the current thread, if
		 * not being already rendered in another one. This means that the
		 * current thread can block until the image is ready.
		 * 
		 * <P>
		 * If the image is rendered incrementally (it depends on the underlying
		 * 'ImageProducer') it will be displayed in that way if the incremental
		 * display is set for the Component class. See the 'imageUpdate()'
		 * method of the 'Component' class.
		 * 
		 * <P>
		 * If the image is the same as the current one nothing is done.
		 * 
		 * @param img
		 *            The image to display.
		 * 
		 * @see Component#imageUpdate
		 * 
		 */
		void setImage(final Image img)
		{
			// Update object state
			synchronized (ImgScrollPane.this)
			{
				if (null == img)
				{
					throw new IllegalArgumentException();
				}
				// If same image do nothing
				if (this.img == img)
				{
					return;
				}

				// (Re)initialize
				this.dimFlags = 0;
				this.img = img;
				ImgScrollPane.this.lastSize = null;
				ImgScrollPane.this.lastZoom = 0.0f;
				ImgScrollPane.this.setScrollbars();
				// Set to erase previous image
				this.erase = true;
			}
			// Start image production (if the image is already being prepared
			// the method does nothing)
			ImgScrollPane.this.prepareImage(img, this);
		}

		/**
		 * Returns the minimum size for this component, which is (0,0).
		 * 
		 * @return The minimum size
		 * 
		 */
		@Override
		public Dimension getMinimumSize()
		{
			return new Dimension(0, 0);
		}

		/**
		 * Returns the maximum size for this component, which is infinite.
		 * 
		 * @return The maximum size
		 * 
		 */
		@Override
		public Dimension getMaximumSize()
		{
			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		/**
		 * Returns the preferred size for this component, which is the image
		 * display size, if known, the previous image display size, if any, or
		 * the size specified at the constructor.
		 * 
		 * @return The preferred size for this component.
		 * 
		 */
		@Override
		public Dimension getPreferredSize()
		{
			return this.dim;
		}

		/**
		 * Monitors the image rendering for dimensions and calls the
		 * superclass' 'imageUpdate()' method. If the display size of the image
		 * is not yet known and the image dimensions are obtained, then the
		 * scrollbars' values are set. If 'img' is not the current image to
		 * display nothing is done and 'false' is returned indicating that
		 * nothing more is necessary for it.
		 * 
		 * @see ImageObserver#imageUpdate
		 * 
		 * @see Component#imageUpdate
		 * 
		 */
		@Override
		public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int w, final int h)
		{
			if (this.img != img)
			{
				// Not the image we want to display now (might be an old one)
				// => do nothing and no more info needed on that image
				return false;
			}
			// If got the image dimensions then store them and set component
			// size as appropriate.
			if (0 != (infoflags & (ImageObserver.WIDTH | ImageObserver.HEIGHT)))
			{
				// Got some image dimension
				synchronized (ImgScrollPane.this)
				{
					// Read the dimensions received
					if (0 != (infoflags & ImageObserver.WIDTH))
					{
						this.imgDim.width = w;
						this.dimFlags |= ImageObserver.WIDTH;
					}
					if (0 != (infoflags & ImageObserver.HEIGHT))
					{
						this.imgDim.height = h;
						this.dimFlags |= ImageObserver.HEIGHT;
					}
					// If we got to know the image dimensions force the layout
					// to be done (to see if it is necessary to show
					// scrollbars)
					if ((ImageObserver.WIDTH | ImageObserver.HEIGHT) == dimFlags)
					{
						ImgScrollPane.this.doLayout();
					}
				}
			}
			// Call the superclass' method to continue processing
			return super.imageUpdate(img, infoflags, x, y, w, h);
		}

		/**
		 * Paints the image, if any, on the graphics context by calling
		 * update().
		 * 
		 * @param g
		 *            The graphics context to paint on.
		 * 
		 */
		@Override
		public void paint(final Graphics g)
		{
			// Now call update as usual
			this.update(g);
		}

		/**
		 * Updates the component by drawing the relevant part of the image that
		 * fits within the Graphics clipping area of 'g'. If the image is not
		 * already being prepared for rendering or is not already rendered this
		 * method does not start it. This is to avoid blocking the AWT threads
		 * for rendering the image. The image rendering is started by the
		 * 'setImage()' method.
		 * 
		 * @param g
		 *            The graphics context where to draw
		 * 
		 * @see #setImage
		 * 
		 */
		@Override
		public void update(final Graphics g)
		{
			final Image img; // The image to display
			final float zoom; // The zoom factor
			boolean erase; // If the display area should be erased
			int dx1, dy1; // Scaling destionation upper-left corner
			int dx2, dy2; // Scaling destination down-right corner
			int sx1, sy1; // Scaling source upper-left corner
			int sx2, sy2; // Scaling source down-right corner
			int ox, oy; // Centering offset
			final Rectangle b; // Bounds of the display area
			final int offx;  // Offset of the upper-left corner
			final int offy;
			final int loffx;  // Offset of the upper-left corner of last update
			final int loffy;
			final boolean copyScroll;// If the scrolling should be done by copying
			final Rectangle clip; // The clipping area
			final int status; // The image fetching status

			// Copy to local variables in a synchronized block to avoid races
			synchronized (ImgScrollPane.this)
			{
				img = this.img;
				zoom = ImgScrollPane.this.zoom;
				erase = this.erase;
				copyScroll = ImgScrollPane.this.copyScroll;
				this.erase = false;

				// If no image or the image has not started preparation yet do
				// nothing. We do not want to start the image preparation in
				// this thread because it can be long.
				if (null == img || 0 == (status = this.checkImage(img, null)))
				{
					return;
				}
				// Get the display size and eventual centering offset for the
				// image.
				b = getBounds();
				ox = (b.width > this.dim.width) ? (b.width - this.dim.width) / 2 : 0;
				oy = (b.height > this.dim.height) ? (b.height - this.dim.height) / 2 : 0;
				// Get the display offset
				clip = g.getClipBounds();
				if (null != lastUpdateOffset && (clip.width < b.width || clip.height < b.height))
				{
					// The clip is smaller than the display area => we need to
					// paint with the last offset to avoid screwing up the
					// displayed image.
					offx = this.lastUpdateOffset.x;
					offy = this.lastUpdateOffset.y;
				}
				else
				{
					// The clipping area covers the whole display area => we
					// can use the current offset.
					offx = ImgScrollPane.this.hsbar.getValue();
					offy = ImgScrollPane.this.vsbar.getValue();
				}
				// Get and update the offset of last update
				if (null == lastUpdateOffset)
				{
					this.lastUpdateOffset = new Point();
				}
				loffx = this.lastUpdateOffset.x;
				loffy = this.lastUpdateOffset.y;
				this.lastUpdateOffset.x = offx;
				this.lastUpdateOffset.y = offy;
				// Set the display size according to zoom
				if (1.0f == zoom)
				{
					// Natural image size, no scaling
					// Displace the origin of the image according to offset
					ox -= offx;
					oy -= offy;
					// No zoom so no translation for scaling compensation needed
					sx1 = sy1 = 0; // to keep compiler happy
					sx2 = sy2 = 0; // to keep compiler happy
					dx1 = dy1 = 0; // to keep compiler happy
					dx2 = dy2 = 0; // to keep compiler happy
				}
				else
				{
					int sox, soy; // Scaling compensation offset
					// Calculate coordinates of lower right corner for scaling
					if ((ImageObserver.WIDTH | ImageObserver.HEIGHT) != dimFlags)
					{
						// Image dims not yet available we can't display
						return;
					}
					sx1 = sy1 = 0;
					sx2 = this.imgDim.width;
					sy2 = this.imgDim.height;
					dx1 = dy1 = 0;
					dx2 = this.dim.width;
					dy2 = this.dim.height;
					sox = soy = 0;
					// Limit the scaling area according to display size so
					// that scaling operates only on the area to be displayed
					if (dx2 > b.width)
					{
						// Calculate coordinates of displayed portion
						dx2 = b.width + ((1.0f < zoom) ? (int) Math.ceil(zoom) : 0);
						if ((int) zoom == zoom)
						{
							// For integer zoom make dx2 a multiple of zoom
							dx2 = (int) (Math.ceil(dx2 / zoom) * zoom);
						}
						sx1 = (int) (offx / zoom);
						sx2 = sx1 + (int) (dx2 / zoom);
						// Compensate the scaling on integer coordinates with
						// an offset
						sox = (int) (sx1 * zoom - offx);
					}
					if (dy2 > b.height)
					{
						// Calculate coordinates of displayed portion
						dy2 = b.height + ((1.0f < zoom) ? (int) Math.ceil(zoom) : 0);
						if ((int) zoom == zoom)
						{
							// For integer zoom make dy2 a multiple of zoom
							dy2 = (int) (Math.ceil(dy2 / zoom) * zoom);
						}
						sy1 = (int) (offy / zoom);
						sy2 = sy1 + (int) (dy2 / zoom);
						// Compensate the scaling on integer coordinates with
						// an extra offset
						soy = (int) (sy1 * zoom - offy);
					}
					// Apply centering offset and scaling compensation offset
					dx1 += ox + sox;
					dy1 += oy + soy;
					dx2 += ox + sox;
					dy2 += oy + soy;
				}
			}
			// If the image is not yet complete and we are scrolling set to
			// erase to avoid leftovers of previous scroll on parts of the
			// image which are not yet ready
			if (0 == (status & ImageObserver.ALLBITS) && (loffx != offx || loffy != offy))
			{
				erase = true;
			}
			// Now we have the necessary info for display. We do it outside
			// synchronized to avoid any potential deadlocks with imageUpdate().
			if (erase)
			{
				// We need to erase the current image. Make sure that we
				// redraw everything by setting the clipping area to the whole
				// display one.
				g.setClip(0, 0, b.width, b.height);
				g.setColor(getBackground());
				g.fillRect(0, 0, b.width, b.height);
			}

			// Use copy scrolling if the image has not been erased, we are
			// scrolling, the image is complete, and copy scrolling is enabled.
			if (copyScroll && !erase && (loffx != offx || loffy != offy) && 0 != (status & ImageObserver.ALLBITS))
			{
				// We might be able to move some part of the displayed area
				// instead of redrawing everything.

				// We are just trasnlating the current image, so we can reuse
				// a part of it.

				final int culx;  // Clipping area upper-left corner (inclusive)
				final int culy;
				final int cdrx;  // Clipping area down-right corner (exclusive)
				final int cdry;
				int vulx, vuly; // Valid area upper-left corner (inclusive)
				int vdrx, vdry; // Valid area down-right corner (exclusive)

				culx = clip.x;
				culy = clip.y;
				cdrx = clip.x + clip.width;
				cdry = clip.y + clip.height;

				// Initialize valid area as the current display area after the
				// translation.
				vulx = loffx - offx;
				vuly = loffy - offy;
				vdrx = vulx + b.width;
				vdry = vuly + b.height;

				// Make new valid area the intersection of the clipping area
				// and the valid area.
				if (culx > vulx)
					vulx = culx;
				if (culy > vuly)
					vuly = culy;
				if (cdrx < vdrx)
					vdrx = cdrx;
				if (cdry < vdry)
					vdry = cdry;

				// If the new valid area is non-empty then copy current image
				// data
				if (vulx < vdrx && vuly < vdry)
				{
					// Ok we can move a part instead of repainting
					g.copyArea(vulx + offx - loffx, vuly + offy - loffy, vdrx - vulx, vdry - vuly, loffx - offx, loffy
							- offy);
					// Now we need to redraw the other parts
					if (culx < vulx)
					{ // Need to draw at left
						g.setClip(culx, culy, vulx - culx, cdry - culy);
						if (1.0f == zoom)
						{ // No scaling
							g.drawImage(img, ox, oy, this);
						}
						else
						{ // Draw the image using on the fly scaling
							g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
						}
					}
					if (vdrx < cdrx)
					{ // Need to draw at right
						g.setClip(vdrx, culy, cdrx - vdrx, cdry - culy);
						if (1.0f == zoom)
						{ // No scaling
							g.drawImage(img, ox, oy, this);
						}
						else
						{ // Draw the image using on the fly scaling
							g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
						}
					}
					if (culy < vuly)
					{ // Need to draw at top
						g.setClip(vulx, culy, vdrx - vulx, vuly - culy);
						if (1.0f == zoom)
						{ // No scaling
							g.drawImage(img, ox, oy, this);
						}
						else
						{ // Draw the image using on the fly scaling
							g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
						}
					}
					if (vdry < cdry)
					{ // Need to draw at bottom
						g.setClip(vulx, vdry, vdrx - vulx, cdry - vdry);
						if (1.0f == zoom)
						{ // No scaling
							g.drawImage(img, ox, oy, this);
						}
						else
						{ // Draw the image using on the fly scaling
							g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
						}
					}
				}
				else
				{
					// New valid area is empty, we need to draw everything
					if (1.0f == zoom)
					{ // No scaling
						g.drawImage(img, ox, oy, this);
					}
					else
					{ // Draw the image using on the fly scaling
						g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
					}
				}
			}
			else
			{
				// We are not translating, so we can't copy
				if (1.0f == zoom)
				{ // No scaling
					g.drawImage(img, ox, oy, this);
				}
				else
				{ // Draw the image using on the fly scaling
					g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, this);
				}
			}
		}

		/**
		 * Calculates the image display dimensions according to the zoom and
		 * image size. The dimensions are stored in 'dim'.
		 * 
		 * @return True if the dimensions could be calculated, false if not
		 *         (i.e. not enough info is available).
		 * 
		 */
		boolean calcDim()
		{
			// We need the image dimensions
			if ((ImageObserver.WIDTH | ImageObserver.HEIGHT) != dimFlags)
			{
				// Image dims not yet available we can't do anything
				return false;
			}
			// Calculate dims
			if (1.0f == zoom)
			{
				// Natural image dimension
				this.dim.width = this.imgDim.width;
				this.dim.height = this.imgDim.height;
			}
			else
			{
				// Apply zoom
				this.dim.width = (int) (ImgScrollPane.this.zoom * this.imgDim.width);
				this.dim.height = (int) (ImgScrollPane.this.zoom * this.imgDim.height);
			}
			return true;
		}
	}

	/**
	 * Scrollbars for the ImgScrollPane container. They are normal AWT
	 * Scrollbars, but with a thickness of ImgScrollPane.SCROLLBAR_THICKNESS.
	 * Also many of the set method of the Adjustable interface are overriden and
	 * throw IllegalArgumentException since they are not to be used externally.
	 * 
	 */
	class ISPScrollbar extends Scrollbar
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a new scroll bar with the specified orientation and
		 * values.
		 * 
		 * <P>
		 * The orientation argument must take one of the two values
		 * Scrollbar.HORIZONTAL, or Scrollbar.VERTICAL, indicating a horizontal
		 * or vertical scroll bar, respectively.
		 * 
		 * @param orientation
		 *            indicates the orientation of the scroll bar
		 * 
		 * @param value
		 *            the initial value of the scroll bar.
		 * 
		 * @param visible
		 *            the size of the scroll bar's bubble, representing the
		 *            visible portion; the scroll bar uses this value when
		 *            paging up or down by a page.
		 * 
		 * @param min
		 *            the minimum value of the scroll bar.
		 * 
		 * @param max
		 *            the maximum value of the scroll bar.
		 *
		 */
		ISPScrollbar(final int orientation, final int value, final int visible, final int min, final int max)
		{
			super(orientation, value, visible, min, max);
		}

		/**
		 * Returns the preferred size of the scrollbar. It is the same as the
		 * preferred size of a normal scrollbar but with a thickness of
		 * ImgScrollPane.SCROLLBAR_THICKNESS.
		 * 
		 * @return The Scrollbar preferred size
		 */
		@Override
		public Dimension getPreferredSize()
		{
			final Dimension psz = super.getPreferredSize();
			if (HORIZONTAL == getOrientation())
			{
				psz.height = SCROLLBAR_THICKNESS;
			}
			else
			{
				psz.width = SCROLLBAR_THICKNESS;
			}
			return psz;
		}

		/**
		 * Throws an IllegalArgumentException since the minimum value should
		 * never be set externally.
		 */
		@Override
		public void setMinimum(final int min)
		{
			throw new IllegalArgumentException();
		}

		/**
		 * Throws an IllegalArgumentException since the maximum value should
		 * never be set externally.
		 */
		@Override
		public void setMaximum(final int max)
		{
			throw new IllegalArgumentException();
		}

		/**
		 * Throws an IllegalArgumentException since the visible amount should
		 * never be set externally.
		 */
		@Override
		public void setVisibleAmount(final int v)
		{
			throw new IllegalArgumentException();
		}

		/**
		 * Throws an IllegalArgumentException since the block increment should
		 * never be set externally.
		 */
		@Override
		public void setBlockIncrement(final int b)
		{
			throw new IllegalArgumentException();
		}

		/**
		 * Sets the block increment for this scroll bar.
		 * 
		 * <P>
		 * The block increment is the value that is added (subtracted) when the
		 * user activates the block increment area of the scroll bar, generally
		 * through a mouse or keyboard gesture that the scroll bar receives as
		 * an adjustment event.
		 * 
		 * <P>
		 * This is a version to be used by The ImgScrollPane class only.
		 * 
		 * @param v
		 *            the amount by which to increment or decrement the scroll
		 *            bar's value.
		 */
		void setBlockIncrementI(final int v)
		{
			super.setBlockIncrement(v);
		}

		/**
		 * Sets the value of this scroll bar to the specified value.
		 * 
		 * <P>
		 * If the value supplied is less than the current minimum or greater
		 * than the current maximum, then one of those values is substituted, as
		 * appropriate.
		 * 
		 * <P>
		 * This is a version to be used by The ImgScrollPane class only.
		 * 
		 * @param newValue
		 *            he new value of the scroll bar.
		 */
		void setValueI(final int newValue)
		{
			super.setValue(newValue);
		}

		/**
		 * Sets the value of this scroll bar to the specified value and requests
		 * a repaint of the image area.
		 * 
		 * <P>
		 * If the value supplied is less than the current minimum or greater
		 * than the current maximum, then one of those values is substituted, as
		 * appropriate.
		 * 
		 * @param newValue
		 *            he new value of the scroll bar.
		 */
		@Override
		public void setValue(int newValue)
		{
			// Set the value and check if we need to repaint
			synchronized (ImgScrollPane.this)
			{
				super.setValue(newValue);
				newValue = this.getValue(); // get the actual value for check
				if (null != imgDisplay.lastUpdateOffset)
				{
					if (HORIZONTAL == getOrientation())
					{
						if (ImgScrollPane.this.imgDisplay.lastUpdateOffset.x == newValue)
						{
							return; // No change
						}
					}
					else
					{
						if (ImgScrollPane.this.imgDisplay.lastUpdateOffset.y == newValue)
						{
							return; // No change
						}
					}
				}
			}
			// New value changes from last drawn => repaint
			ImgScrollPane.this.imgDisplay.repaint();
		}
	}
}
