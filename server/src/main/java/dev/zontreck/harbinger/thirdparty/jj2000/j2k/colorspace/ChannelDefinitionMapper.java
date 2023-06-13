/*****************************************************************************
 *
 * $Id: ChannelDefinitionMapper.java,v 1.2 2002/08/08 14:06:53 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/
package dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.BlkImgDataSrc;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.image.DataBlk;

/**
 * This class is responsible for the mapping between requested components and
 * image channels.
 *
 * @author Bruce A. Kern
 * @version 1.0
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace
 */
public class ChannelDefinitionMapper extends ColorSpaceMapper {
	/**
	 * Ctor which creates an ICCProfile for the image and initializes all data
	 * objects (input, working, and output).
	 *
	 * @param src   -- Source of image data
	 * @param csMap -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 */
	protected ChannelDefinitionMapper ( final BlkImgDataSrc src , final ColorSpace csMap ) {
		super ( src , csMap );
		/* end ChannelDefinitionMapper ctor */
	}

	/**
	 * Factory method for creating instances of this class.
	 *
	 * @param src   -- source of image data
	 * @param csMap -- provides dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace info
	 * @return ChannelDefinitionMapper instance
	 * @throws ColorSpaceException
	 */
	public static BlkImgDataSrc createInstance ( final BlkImgDataSrc src , final ColorSpace csMap ) throws ColorSpaceException {

		return new ChannelDefinitionMapper ( src , csMap );
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 *
	 * <p>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 *
	 * <p>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 *
	 * <p>
	 * The returned data has its 'progressive' attribute set to that of the
	 * input data.
	 *
	 * @param out Its coordinates and dimensions specify the area to return. If
	 *            it contains a non-null data array, then it must have the
	 *            correct dimensions. If it contains a null data array a new one
	 *            is created. The fields in this object are modified to return
	 *            the data.
	 * @param c   The index of the component from which to get the data. Only 0
	 *            and 3 are valid.
	 * @return The requested DataBlk
	 * @see #getInternCompData
	 */
	@Override
	public DataBlk getCompData ( final DataBlk out , final int c ) {
		return this.src.getCompData ( out , this.csMap.getChannelDefinition ( c ) );
	}

	/**
	 * Returns, in the blk argument, a block of image data containing the
	 * specifed rectangular area, in the specified component. The data is
	 * returned, as a copy of the internal data, therefore the returned data can
	 * be modified "in place".
	 *
	 * <p>
	 * The rectangular area to return is specified by the 'ulx', 'uly', 'w' and
	 * 'h' members of the 'blk' argument, relative to the current tile. These
	 * members are not modified by this method. The 'offset' of the returned
	 * data is 0, and the 'scanw' is the same as the block's width. See the
	 * 'DataBlk' class.
	 *
	 * <p>
	 * This method, in general, is less efficient than the 'getInternCompData()'
	 * method since, in general, it copies the data. However if the array of
	 * returned data is to be modified by the caller then this method is
	 * preferable.
	 *
	 * <p>
	 * If the data array in 'blk' is 'null', then a new one is created. If the
	 * data array is not 'null' then it is reused, and it must be large enough
	 * to contain the block's data. Otherwise an 'ArrayStoreException' or an
	 * 'IndexOutOfBoundsException' is thrown by the Java system.
	 *
	 * <p>
	 * The returned data may have its 'progressive' attribute set. In this case
	 * the returned data is only an approximation of the "final" data.
	 *
	 * @param out Its coordinates and dimensions specify the area to return,
	 *            relative to the current tile. If it contains a non-null data
	 *            array, then it must be large enough. If it contains a null
	 *            data array a new one is created. Some fields in this object
	 *            are modified to return the data.
	 * @param c   The index of the component from which to get the data.
	 * @see #getCompData
	 */
	@Override
	public DataBlk getInternCompData ( final DataBlk out , final int c ) {
		return this.src.getInternCompData ( out , this.csMap.getChannelDefinition ( c ) );
	}

	/**
	 * Returns the number of bits, referred to as the "range bits",
	 * corresponding to the nominal range of the data in the specified
	 * component. If this number is <i>b</b> then for unsigned data the nominal
	 * range is between 0 and 2^b-1, and for signed data it is between -2^(b-1)
	 * and 2^(b-1)-1. For floating point data this value is not applicable.
	 *
	 * @param c The index of the component.
	 * @return The number of bits corresponding to the nominal range of the
	 * data. Fro floating-point data this value is not applicable and
	 * the return value is undefined.
	 */
	@Override
	public int getFixedPoint ( final int c ) {
		return this.src.getFixedPoint ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getNomRangeBits ( final int c ) {
		return this.src.getNomRangeBits ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getCompImgHeight ( final int c ) {
		return this.src.getCompImgHeight ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getCompImgWidth ( final int c ) {
		return this.src.getCompImgWidth ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getCompSubsX ( final int c ) {
		return this.src.getCompSubsX ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getCompSubsY ( final int c ) {
		return this.src.getCompSubsY ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getCompULX ( final int c ) {
		return this.src.getCompULX ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getCompULY ( final int c ) {
		return this.src.getCompULY ( this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getTileCompHeight ( final int t , final int c ) {
		return this.src.getTileCompHeight ( t , this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public int getTileCompWidth ( final int t , final int c ) {
		return this.src.getTileCompWidth ( t , this.csMap.getChannelDefinition ( c ) );
	}

	@Override
	public String toString ( ) {
		int i;
		final StringBuffer rep = new StringBuffer ( "[ChannelDefinitionMapper nchannels= " ).append ( this.ncomps );

		for ( i = 0; i < this.ncomps ; ++ i ) {
			rep.append ( ColorSpaceMapper.eol ).append ( "  component[" ).append ( i ).append ( "] mapped to channel[" )
					.append ( this.csMap.getChannelDefinition ( i ) ).append ( "]" );
		}

		return rep.append ( "]" ).toString ( );
	}

	/* end class ChannelDefinitionMapper */
}
