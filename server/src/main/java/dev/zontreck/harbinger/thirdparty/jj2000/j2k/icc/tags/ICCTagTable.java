/*****************************************************************************
 *
 * $Id: ICCTagTable.java,v 1.1 2002/07/25 14:56:37 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags;

import dev.zontreck.harbinger.thirdparty.jj2000.j2k.colorspace.ColorSpace;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.ICCProfile;
import dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.types.ICCProfileHeader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class models an ICCTagTable as a HashTable which maps ICCTag signatures
 * (as Integers) to ICCTags.
 * <p>
 * On disk the tag table exists as a byte array conventionally aggragted into a
 * structured sequence of types (bytes, shorts, ints, and floats. The first four
 * bytes are the integer count of tags in the table. This is followed by an
 * array of triplets, one for each tag. The triplets each contain three
 * integers, which are the tag signature, the offset of the tag in the byte
 * array and the length of the tag in bytes. The tag data follows. Each tag
 * consists of an integer (4 bytes) tag type, a reserved integer and the tag
 * data, which varies depending on the tag.
 *
 * @author Bruce A. Kern
 * @version 1.0
 * @see dev.zontreck.harbinger.thirdparty.jj2000.j2k.icc.tags.ICCTag
 */
public class ICCTagTable extends Hashtable<Integer, ICCTag> {
	private static final long serialVersionUID = 1L;

	private static final String eol = System.getProperty ( "line.separator" );
	private static final int offTagCount = ICCProfileHeader.size;
	private static final int offTags = ICCTagTable.offTagCount + ICCProfile.int_size;

	private final Vector<Triplet> trios = new Vector<Triplet> ( );

	private final int tagCount;

	/**
	 * Ctor used by factory method.
	 *
	 * @param data raw tag data
	 */
	protected ICCTagTable ( final byte[] data ) {
		this.tagCount = ICCProfile.getInt ( data , ICCTagTable.offTagCount );

		int offset = ICCTagTable.offTags;
		for ( int i = 0 ; i < this.tagCount ; ++ i ) {
			final int signature = ICCProfile.getInt ( data , offset );
			final int tagOffset = ICCProfile.getInt ( data , offset + ICCProfile.int_size );
			final int length = ICCProfile.getInt ( data , offset + 2 * ICCProfile.int_size );
			this.trios.addElement ( new Triplet ( signature , tagOffset , length ) );
			offset += 3 * ICCProfile.int_size;
		}

		Iterator<Triplet> iterator = trios.iterator ( );
		while ( iterator.hasNext ( ) ) {
			final Triplet trio = iterator.next ( );
			final ICCTag tag = ICCTag.createInstance ( trio.signature , data , trio.offset , trio.count );
			this.put ( Integer.valueOf ( tag.signature ) , tag );
		}
	}

	/**
	 * Factory method for creating a tag table from raw input.
	 *
	 * @param data array of unstructured data representing a tag
	 * @return ICCTagTable
	 */
	public static ICCTagTable createInstance ( final byte[] data ) {
		final ICCTagTable tags = new ICCTagTable ( data );
		return tags;
	}

	/**
	 * Representation of a tag table
	 *
	 * @return String
	 */
	@Override
	public synchronized String toString ( ) {
		final StringBuffer rep = new StringBuffer ( "[ICCTagTable containing " + this.tagCount + " tags:" );
		final StringBuffer body = new StringBuffer ( "  " );
		final Enumeration<Integer> keys = this.keys ( );
		Iterator<Integer> iterator = keys.asIterator ( );
		while ( iterator.hasNext ( ) ) {
			final Integer key = iterator.next ( );
			final ICCTag tag = this.get ( key );
			body.append ( ICCTagTable.eol ).append ( tag.toString ( ) );
		}
		rep.append ( ColorSpace.indent ( "  " , body ) );
		return rep.append ( "]" ).toString ( );
	}

	/**
	 * Output the table to a disk
	 *
	 * @param raf RandomAccessFile which receives the table.
	 * @throws IOException
	 */
	public void write ( final RandomAccessFile raf ) throws IOException {

		final int ntags = this.trios.size ( );

		final int countOff = ICCProfileHeader.size;
		final int tagOff = countOff + ICCProfile.int_size;
		final int dataOff = tagOff + 3 * ntags * ICCProfile.int_size;

		raf.seek ( countOff );
		raf.writeInt ( ntags );

		int currentTagOff = tagOff;
		int currentDataOff = dataOff;

		Iterator<Triplet> iterator = trios.iterator ( );
		while ( iterator.hasNext ( ) ) {
			final Triplet trio = iterator.next ( );
			final ICCTag tag = this.get ( Integer.valueOf ( trio.signature ) );

			raf.seek ( currentTagOff );
			raf.writeInt ( tag.signature );
			raf.writeInt ( currentDataOff );
			raf.writeInt ( tag.count );
			currentTagOff += 3 * Triplet.size;

			raf.seek ( currentDataOff );
			raf.write ( tag.data , tag.offset , tag.count );
			currentDataOff += tag.count;
		}
	}

	private static class Triplet {
		/**
		 * size of an entry
		 */
		public static final int size = 3 * ICCProfile.int_size;
		/**
		 * Tag identifier
		 */
		private final int signature;
		/**
		 * absolute offset of tag data
		 */
		private final int offset;
		/**
		 * length of tag data
		 */
		private final int count;

		Triplet ( final int signature , final int offset , final int count ) {
			this.signature = signature;
			this.offset = offset;
			this.count = count;
		}
	}

	/* end class ICCTagTable */
}
