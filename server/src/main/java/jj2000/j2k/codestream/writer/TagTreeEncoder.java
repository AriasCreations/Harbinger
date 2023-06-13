/*
 * CVS identifier:
 *
 * $Id: TagTreeEncoder.java,v 1.10 2001/08/17 16:02:06 grosbois Exp $
 *
 * Class:                   TagTreeEncoder
 *
 * Description:             Encoder of tag trees
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

import jj2000.j2k.util.*;

/**
 * This class implements the tag tree encoder. A tag tree codes a 2D matrix of
 * integer elements in an efficient way. The encoding procedure 'encode()' codes
 * information about a value of the matrix, given a threshold. The procedure
 * encodes the sufficient information to identify whether or not the value is
 * greater than or equal to the threshold.
 * 
 * <p>
 * The tag tree saves encoded information to a BitOutputBuffer.
 * 
 * <p>
 * A particular and useful property of tag trees is that it is possible to
 * change a value of the matrix, provided both new and old values of the element
 * are both greater than or equal to the largest threshold which has yet been
 * supplied to the coding procedure 'encode()'. This property can be exploited
 * through the 'setValue()' method.
 * 
 * <p>
 * This class allows saving the state of the tree at any point and restoring it
 * at a later time, by calling save() and restore().
 * 
 * <p>
 * A tag tree can also be reused, or restarted, if one of the reset() methods is
 * called.
 * 
 * <p>
 * The TagTreeDecoder class implements the tag tree decoder.
 * 
 * <p>
 * Tag trees that have one dimension, or both, as 0 are allowed for convenience.
 * Of course no values can be set or coded in such cases.
 * 
 * @see BitOutputBuffer
 * 
 * @see jj2000.j2k.codestream.reader.TagTreeDecoder
 */
public class TagTreeEncoder
{

	/** The horizontal dimension of the base level */
	protected int w;

	/** The vertical dimensions of the base level */
	protected int h;

	/** The number of levels in the tag tree */
	protected int lvls;

	/**
	 * The tag tree values. The first index is the level, starting at level 0
	 * (leafs). The second index is the element within the level, in
	 * lexicographical order.
	 */
	protected int[][] treeV;

	/**
	 * The tag tree state. The first index is the level, starting at level 0
	 * (leafs). The second index is the element within the level, in
	 * lexicographical order.
	 */
	protected int[][] treeS;

	/**
	 * The saved tag tree values. The first index is the level, starting at
	 * level 0 (leafs). The second index is the element within the level, in
	 * lexicographical order.
	 */
	protected int[][] treeVbak;

	/**
	 * The saved tag tree state. The first index is the level, starting at level
	 * 0 (leafs). The second index is the element within the level, in
	 * lexicographical order.
	 */
	protected int[][] treeSbak;

	/**
	 * The saved state. If true the values and states of the tree have been
	 * saved since the creation or last reset.
	 */
	protected boolean saved;

	/**
	 * Creates a tag tree encoder with 'w' elements along the horizontal
	 * dimension and 'h' elements along the vertical direction. The total number
	 * of elements is thus 'vdim' x 'hdim'.
	 * 
	 * <p>
	 * The values of all elements are initialized to Integer.MAX_VALUE.
	 * 
	 * @param h
	 *            The number of elements along the horizontal direction.
	 * 
	 * @param w
	 *            The number of elements along the vertical direction.
	 */
	public TagTreeEncoder(final int h, final int w)
	{
		int k;
		// Check arguments
		if (0 > w || 0 > h)
		{
			throw new IllegalArgumentException();
		}
		// Initialize elements
		this.init(w, h);
		// Set values to max
		for (k = this.treeV.length - 1; 0 <= k; k--)
		{
			ArrayUtil.intArraySet(this.treeV[k], Integer.MAX_VALUE);
		}
	}

	/**
	 * Creates a tag tree encoder with 'w' elements along the horizontal
	 * dimension and 'h' elements along the vertical direction. The total number
	 * of elements is thus 'vdim' x 'hdim'. The values of the leafs in the tag
	 * tree are initialized to the values of the 'val' array.
	 * 
	 * <p>
	 * The values in the 'val' array are supposed to appear in lexicographical
	 * order, starting at index 0.
	 * 
	 * @param h
	 *            The number of elements along the horizontal direction.
	 * 
	 * @param w
	 *            The number of elements along the vertical direction.
	 * 
	 * @param val
	 *            The values with which initialize the leafs of the tag tree.
	 */
	public TagTreeEncoder(final int h, final int w, final int[] val)
	{
		int k;
		// Check arguments
		if (0 > w || 0 > h || val.length < w * h)
		{
			throw new IllegalArgumentException();
		}
		// Initialize elements
		this.init(w, h);
		// Update leaf values
		for (k = w * h - 1; 0 <= k; k--)
		{
			this.treeV[0][k] = val[k];
		}
		// Calculate values at other levels
		this.recalcTreeV();
	}

	/**
	 * Returns the number of leafs along the horizontal direction.
	 * 
	 * @return The number of leafs along the horizontal direction.
	 */
	public final int getWidth()
	{
		return this.w;
	}

	/**
	 * Returns the number of leafs along the vertical direction.
	 * 
	 * @return The number of leafs along the vertical direction.
	 */
	public final int getHeight()
	{
		return this.h;
	}

	/**
	 * Initializes the variables of this class, given the dimensions at the base
	 * level (leaf level). All the state ('treeS' array) and values ('treeV'
	 * array) are intialized to 0. This method is called by the constructors.
	 * 
	 * @param w
	 *            The number of elements along the vertical direction.
	 * 
	 * @param h
	 *            The number of elements along the horizontal direction.
	 */
	private void init(int w, int h)
	{
		int i;
		// Initialize dimensions
		this.w = w;
		this.h = h;
		// Calculate the number of levels
		if (0 == w || 0 == h)
		{
			this.lvls = 0;
		}
		else
		{
			this.lvls = 1;
			while (1 != h || 1 != w)
			{ // Loop until we reach root
				w = (w + 1) >> 1;
				h = (h + 1) >> 1;
				this.lvls++;
			}
		}
		// Allocate tree values and states (no need to initialize to 0 since
		// it's the default)
		this.treeV = new int[this.lvls][];
		this.treeS = new int[this.lvls][];
		w = this.w;
		h = this.h;
		for (i = 0; i < this.lvls; i++)
		{
			this.treeV[i] = new int[h * w];
			this.treeS[i] = new int[h * w];
			w = (w + 1) >> 1;
			h = (h + 1) >> 1;
		}
	}

	/**
	 * Recalculates the values of the elements in the tag tree, in levels 1 and
	 * up, based on the values of the leafs (level 0).
	 */
	private void recalcTreeV()
	{
		int m, n, bi, lw, tm1, tm2, lh, k;
		// Loop on all other levels, updating minimum
		for (k = 0; k < this.lvls - 1; k++)
		{
			// Visit all elements in level
			lw = (this.w + (1 << k) - 1) >> k;
			lh = (this.h + (1 << k) - 1) >> k;
			for (m = ((lh >> 1) << 1) - 2; 0 <= m; m -= 2)
			{ // All quads with 2 lines
				for (n = ((lw >> 1) << 1) - 2; 0 <= n; n -= 2)
				{ // All quads with 2 columns
					// Take minimum of 4 elements and put it in higher
					// level
					bi = m * lw + n;
					tm1 = (this.treeV[k][bi] < this.treeV[k][bi + 1]) ? this.treeV[k][bi] : this.treeV[k][bi + 1];
					tm2 = (this.treeV[k][bi + lw] < this.treeV[k][bi + lw + 1]) ? this.treeV[k][bi + lw] : this.treeV[k][bi + lw + 1];
					this.treeV[k + 1][(m >> 1) * ((lw + 1) >> 1) + (n >> 1)] = tm1 < tm2 ? tm1 : tm2;
				}
				// Now we may have quad with 1 column, 2 lines
				if (0 != lw % 2)
				{
					n = ((lw >> 1) << 1);
					// Take minimum of 2 elements and put it in higher
					// level
					bi = m * lw + n;
					this.treeV[k + 1][(m >> 1) * ((lw + 1) >> 1) + (n >> 1)] = (this.treeV[k][bi] < this.treeV[k][bi + lw]) ? this.treeV[k][bi]
							: this.treeV[k][bi + lw];
				}
			}
			// Now we may have quads with 1 line, 2 or 1 columns
			if (0 != lh % 2)
			{
				m = ((lh >> 1) << 1);
				for (n = ((lw >> 1) << 1) - 2; 0 <= n; n -= 2)
				{ // All quads with 2 columns
					// Take minimum of 2 elements and put it in higher
					// level
					bi = m * lw + n;
					this.treeV[k + 1][(m >> 1) * ((lw + 1) >> 1) + (n >> 1)] = (this.treeV[k][bi] < this.treeV[k][bi + 1]) ? this.treeV[k][bi]
							: this.treeV[k][bi + 1];
				}
				// Now we may have quad with 1 column, 1 line
				if (0 != lw % 2)
				{
					// Just copy the value
					n = ((lw >> 1) << 1);
					this.treeV[k + 1][(m >> 1) * ((lw + 1) >> 1) + (n >> 1)] = this.treeV[k][m * lw + n];
				}
			}
		}
	}

	/**
	 * Changes the value of a leaf in the tag tree. The new and old values of
	 * the element must be not smaller than the largest threshold which has yet
	 * been supplied to 'encode()'.
	 * 
	 * @param m
	 *            The vertical index of the element.
	 * 
	 * @param n
	 *            The horizontal index of the element.
	 * 
	 * @param v
	 *            The new value of the element.
	 */
	public void setValue(final int m, final int n, final int v)
	{
		int k, idx;
		// Check arguments
		if (0 == lvls || 0 > n || n >= this.w || v < this.treeS[this.lvls - 1][0] || this.treeV[0][m * this.w + n] < this.treeS[this.lvls - 1][0])
		{
			throw new IllegalArgumentException();
		}
		// Update the leaf value
		this.treeV[0][m * this.w + n] = v;
		// Update all parents
		for (k = 1; k < this.lvls; k++)
		{
			idx = (m >> k) * ((this.w + (1 << k) - 1) >> k) + (n >> k);
			if (v < this.treeV[k][idx])
			{
				// We need to update minimum and continue checking
				// in higher levels
				this.treeV[k][idx] = v;
			}
			else
			{
				// We are done: v is equal or less to minimum
				// in this level, no other minimums to update.
				break;
			}
		}
	}

	/**
	 * Sets the values of the leafs to the new set of values and updates the tag
	 * tree accordingly. No leaf can change its value if either the new or old
	 * value is smaller than largest threshold which has yet been supplied to
	 * 'encode()'. However such a leaf can keep its old value (i.e. new and old
	 * value must be identical.
	 * 
	 * <p>
	 * This method is more efficient than the setValue() method if a large
	 * proportion of the leafs change their value. Note that for leafs which
	 * don't have their value defined yet the value should be Integer.MAX_VALUE
	 * (which is the default initialization value).
	 * 
	 * @param val
	 *            The new values for the leafs, in lexicographical order.
	 * 
	 * @see #setValue
	 */
	public void setValues(final int[] val)
	{
		int i;
		final int maxt;
		if (0 == lvls)
		{ // Can't set values on empty tree
			throw new IllegalArgumentException();
		}
		// Check the values
		maxt = this.treeS[this.lvls - 1][0];
		for (i = this.w * this.h - 1; 0 <= i; i--)
		{
			if ((this.treeV[0][i] < maxt || val[i] < maxt) && this.treeV[0][i] != val[i])
			{
				throw new IllegalArgumentException();
			}
			// Update leaf value
			this.treeV[0][i] = val[i];
		}
		// Recalculate tree at other levels
		this.recalcTreeV();
	}

	/**
	 * Encodes information for the specified element of the tree, given the
	 * threshold and sends it to the 'out' stream. The information that is coded
	 * is whether or not the value of the element is greater than or equal to
	 * the value of the threshold.
	 * 
	 * @param m
	 *            The vertical index of the element.
	 * 
	 * @param n
	 *            The horizontal index of the element.
	 * 
	 * @param t
	 *            The threshold to use for encoding. It must be non-negative.
	 * 
	 * @param out
	 *            The stream where to write the coded information.
	 */
	public void encode(final int m, final int n, final int t, final BitOutputBuffer out)
	{
		int k, ts, idx, tmin;

		// Check arguments
		if (m >= this.h || n >= this.w || 0 > t)
		{
			throw new IllegalArgumentException();
		}

		// Initialize
		k = this.lvls - 1;
		tmin = this.treeS[k][0];

		// Loop on levels
		while (true)
		{
			// Index of element in level 'k'
			idx = (m >> k) * ((this.w + (1 << k) - 1) >> k) + (n >> k);
			// Cache state
			ts = this.treeS[k][idx];
			if (ts < tmin)
			{
				ts = tmin;
			}
			while (t > ts)
			{
				if (this.treeV[k][idx] > ts)
				{
					out.writeBit(0); // Send '0' bit
				}
				else if (this.treeV[k][idx] == ts)
				{
					out.writeBit(1); // Send '1' bit
				}
				else
				{ // we are done: set ts and get out of this while
					ts = t;
					break;
				}
				// Increment of treeS[k][idx]
				ts++;
			}
			// Update state
			this.treeS[k][idx] = ts;
			// Update tmin or terminate
			if (0 < k)
			{
				tmin = ts < this.treeV[k][idx] ? ts : this.treeV[k][idx];
				k--;
			}
			else
			{
				// Terminate
				return;
			}
		}
	}

	/**
	 * Saves the current values and state of the tree. Calling restore()
	 * restores the tag tree the saved state.
	 * 
	 * @see #restore
	 */
	public void save()
	{
		int k;

		if (null == treeVbak)
		{ // Nothing saved yet
			// Allocate saved arrays
			// treeV and treeS have the same dimensions
			this.treeVbak = new int[this.lvls][];
			this.treeSbak = new int[this.lvls][];
			for (k = this.lvls - 1; 0 <= k; k--)
			{
				this.treeVbak[k] = new int[this.treeV[k].length];
				this.treeSbak[k] = new int[this.treeV[k].length];
			}
		}

		// Copy the arrays
		for (k = this.treeV.length - 1; 0 <= k; k--)
		{
			System.arraycopy(this.treeV[k], 0, this.treeVbak[k], 0, this.treeV[k].length);
			System.arraycopy(this.treeS[k], 0, this.treeSbak[k], 0, this.treeS[k].length);
		}

		// Set saved state
		this.saved = true;
	}

	/**
	 * Restores the saved values and state of the tree. An
	 * IllegalArgumentException is thrown if the tree values and state have not
	 * been saved yet.
	 * 
	 * @see #save
	 */
	public void restore()
	{
		int k;

		if (!this.saved)
		{ // Nothing saved yet
			throw new IllegalArgumentException();
		}

		// Copy the arrays
		for (k = this.lvls - 1; 0 <= k; k--)
		{
			System.arraycopy(this.treeVbak[k], 0, this.treeV[k], 0, this.treeV[k].length);
			System.arraycopy(this.treeSbak[k], 0, this.treeS[k], 0, this.treeS[k].length);
		}

	}

	/**
	 * Resets the tree values and state. All the values are set to
	 * Integer.MAX_VALUE and the states to 0.
	 */
	public void reset()
	{
		int k;
		// Set all values to Integer.MAX_VALUE
		// and states to 0
		for (k = this.lvls - 1; 0 <= k; k--)
		{
			ArrayUtil.intArraySet(this.treeV[k], Integer.MAX_VALUE);
			ArrayUtil.intArraySet(this.treeS[k], 0);
		}
		// Invalidate saved tree
		this.saved = false;
	}

	/**
	 * Resets the tree values and state. The values are set to the values in
	 * 'val'. The states are all set to 0.
	 * 
	 * @param val
	 *            The new values for the leafs, in lexicographical order.
	 */
	public void reset(final int[] val)
	{
		int k;
		// Set values for leaf level
		for (k = this.w * this.h - 1; 0 <= k; k--)
		{
			this.treeV[0][k] = val[k];
		}
		// Calculate values at other levels
		this.recalcTreeV();
		// Set all states to 0
		for (k = this.lvls - 1; 0 <= k; k--)
		{
			ArrayUtil.intArraySet(this.treeS[k], 0);
		}
		// Invalidate saved tree
		this.saved = false;
	}
}
