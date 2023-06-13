/*****************************************************************************
 *
 * $Id: ICCTextType.java,v 1.1 2002/07/25 14:56:37 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;

import java.nio.charset.StandardCharsets;

/**
 * A text based ICC tag
 *
 * @author Bruce A. Kern
 * @version 1.0
 */
public class ICCTextType extends ICCTag {

	/**
	 * Tag fields
	 */
	public final int type;
	/**
	 * Tag fields
	 */
	public final int reserved;
	/**
	 * Tag fields
	 */
	public final byte[] ascii;

	/**
	 * Construct this tag from its constituant parts
	 *
	 * @param signature tag id
	 * @param data      array of bytes
	 * @param offset    to data in the data array
	 * @param length    of data in the data array
	 */
	protected ICCTextType ( final int signature , final byte[] data , int offset , final int length ) {
		super ( signature , data , offset , length );
		this.type = ICCProfile.getInt ( data , offset );
		offset += ICCProfile.int_size;
		this.reserved = ICCProfile.getInt ( data , offset );
		offset += ICCProfile.int_size;
		int size = 0;
		while ( 0 != data[ offset + size ] )
			++ size;
		this.ascii = new byte[ size ];
		System.arraycopy ( data , offset , this.ascii , 0 , size );
	}

	/**
	 * Return the string rep of this tag.
	 */
	@Override
	public String toString ( ) {
		return "[" + super.toString ( ) + " \"" + new String ( this.ascii , StandardCharsets.UTF_8 ) + "\"]";
	}

	/* end class ICCTextType */
}
