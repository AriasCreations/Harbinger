/*
 * CVS identifier:
 *
 * $Id: TagTreeDecoder.java,v 1.7 2001/08/23 08:04:48 grosbois Exp $
 *
 * Class:                   TagTreeDecoder
 *
 * Description:             Decoder of tag trees
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
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.reader;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.util.ArrayUtil;

import java.io.EOFException;
import java.io.IOException;

/**
 * This class implements the tag tree decoder. A tag tree codes a 2D matrix of
 * integer elements in an efficient way. The decoding procedure 'update()'
 * updates a value of the matrix from a stream of coded data, given a threshold.
 * This procedure decodes enough information to identify whether or not the
 * value is greater than or equal to the threshold, and updates the value
 * accordingly.
 *
 * <p>
 * In general the decoding procedure must follow the same sequence of elements
 * and thresholds as the encoding one. The encoder is implemented by the
 * TagTreeEncoder class.
 *
 * <p>
 * Tag trees that have one dimension, or both, as 0 are allowed for convenience.
 * Of course no values can be set or coded in such cases.
 *
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.codestream.writer.TagTreeEncoder
 */
public class TagTreeDecoder {

	/**
	 * The horizontal dimension of the base level
	 */
	protected int w;

	/**
	 * The vertical dimensions of the base level
	 */
	protected int h;

	/**
	 * The number of levels in the tag tree
	 */
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
	 * Creates a tag tree decoder with 'w' elements along the horizontal
	 * dimension and 'h' elements along the vertical direction. The total number
	 * of elements is thus 'vdim' x 'hdim'.
	 *
	 * <p>
	 * The values of all elements are initialized to Integer.MAX_VALUE (i.e. no
	 * information decoded so far). The states are initialized all to 0.
	 *
	 * @param h The number of elements along the vertical direction.
	 * @param w The number of elements along the horizontal direction.
	 */
	public TagTreeDecoder ( int h , int w ) {
		int i;

		// Check arguments
		if ( 0 > w || 0 > h ) {
			throw new IllegalArgumentException ( );
		}
		// Initialize dimensions
		this.w = w;
		this.h = h;
		// Calculate the number of levels
		if ( 0 == w || 0 == h ) {
			this.lvls = 0; // Empty tree
		}
		else {
			this.lvls = 1;
			while ( 1 != h || 1 != w ) { // Loop until we reach root
				w = ( w + 1 ) >> 1;
				h = ( h + 1 ) >> 1;
				this.lvls++;
			}
		}
		// Allocate tree values and states
		this.treeV = new int[ this.lvls ][];
		this.treeS = new int[ this.lvls ][];
		w = this.w;
		h = this.h;
		for ( i = 0; i < this.lvls ; i++ ) {
			this.treeV[ i ] = new int[ h * w ];
			// Initialize to infinite value
			ArrayUtil.intArraySet ( this.treeV[ i ] , Integer.MAX_VALUE );

			// (no need to initialize to 0 since it's the default)
			this.treeS[ i ] = new int[ h * w ];
			w = ( w + 1 ) >> 1;
			h = ( h + 1 ) >> 1;
		}
	}

	/**
	 * Returns the number of leafs along the horizontal direction.
	 *
	 * @return The number of leafs along the horizontal direction.
	 */
	public final int getWidth ( ) {
		return this.w;
	}

	/**
	 * Returns the number of leafs along the vertical direction.
	 *
	 * @return The number of leafs along the vertical direction.
	 */
	public final int getHeight ( ) {
		return this.h;
	}

	/**
	 * Decodes information for the specified element of the tree, given the
	 * threshold, and updates its value. The information that can be decoded is
	 * whether or not the value of the element is greater than, or equal to, the
	 * value of the threshold.
	 *
	 * @param m  The vertical index of the element.
	 * @param n  The horizontal index of the element.
	 * @param t  The threshold to use in decoding. It must be non-negative.
	 * @param in The stream from where to read the coded information.
	 * @return The updated value at position (m,n).
	 * @throws IOException  If an I/O error occurs while reading from 'in'.
	 * @throws EOFException If the ned of the 'in' stream is reached before getting
	 *                      all the necessary data.
	 */
	public int update ( final int m , final int n , final int t , final PktHeaderBitReader in ) throws IOException {
		int k, tmin;
		int idx, ts, tv;

		// Check arguments
		if ( m >= this.h || n >= this.w || 0 > t ) {
			throw new IllegalArgumentException ( );
		}

		// Initialize
		k = this.lvls - 1;
		tmin = this.treeS[ k ][ 0 ];

		// Loop on levels
		idx = ( m >> k ) * ( ( this.w + ( 1 << k ) - 1 ) >> k ) + ( n >> k );
		while ( true ) {
			// Cache state and value
			ts = this.treeS[ k ][ idx ];
			tv = this.treeV[ k ][ idx ];
			if ( ts < tmin ) {
				ts = tmin;
			}
			while ( t > ts ) {
				if ( tv >= ts ) { // We are not done yet
					if ( 0 == in.readBit ( ) ) { // '0' bit
						// We know that 'value' > treeS[k][idx]
						ts++;
					}
					else { // '1' bit
						// We know that 'value' = treeS[k][idx]
						tv = ts;
						ts++;
					}
					// Increment of treeS[k][idx] done above
				}
				else { // We are done, we can set ts and get out
					ts = t;
					break; // get out of this while
				}
			}
			// Update state and value
			this.treeS[ k ][ idx ] = ts;
			this.treeV[ k ][ idx ] = tv;
			// Update tmin or terminate
			if ( 0 < k ) {
				tmin = ts < tv ? ts : tv;
				k--;
				// Index of element for next iteration
				idx = ( m >> k ) * ( ( this.w + ( 1 << k ) - 1 ) >> k ) + ( n >> k );
			}
			else {
				// Return the updated value
				return tv;
			}
		}
	}

	/**
	 * Returns the current value of the specified element in the tag tree. This
	 * is the value as last updated by the update() method.
	 *
	 * @param m The vertical index of the element.
	 * @param n The horizontal index of the element.
	 * @return The current value of the element.
	 * @see #update
	 */
	public int getValue ( final int m , final int n ) {
		// Check arguments
		if ( m >= this.h || n >= this.w ) {
			throw new IllegalArgumentException ( );
		}
		// Return value
		return this.treeV[ 0 ][ m * this.w + n ];
	}
}
