/*****************************************************************************
 *
 * $Id: ICCTagTable.java,v 1.1 2002/07/25 14:56:37 grosbois Exp $
 *
 * Copyright Eastman Kodak Company, 343 State Street, Rochester, NY 14650
 * $Date $
 ****************************************************************************/

package icc.tags;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import colorspace.ColorSpace;
import icc.ICCProfile;
import icc.types.ICCProfileHeader;

/**
 * This class models an ICCTagTable as a HashTable which maps ICCTag signatures
 * (as Integers) to ICCTags.
 * 
 * On disk the tag table exists as a byte array conventionally aggragted into a
 * structured sequence of types (bytes, shorts, ints, and floats. The first four
 * bytes are the integer count of tags in the table. This is followed by an
 * array of triplets, one for each tag. The triplets each contain three
 * integers, which are the tag signature, the offset of the tag in the byte
 * array and the length of the tag in bytes. The tag data follows. Each tag
 * consists of an integer (4 bytes) tag type, a reserved integer and the tag
 * data, which varies depending on the tag.
 * 
 * @see icc.tags.ICCTag
 * @version 1.0
 * @author Bruce A. Kern
 */
public class ICCTagTable extends Hashtable<Integer, ICCTag>
{
	private static final long serialVersionUID = 1L;

	private static final String eol = System.getProperty("line.separator");
	private static final int offTagCount = ICCProfileHeader.size;
	private static final int offTags = ICCTagTable.offTagCount + ICCProfile.int_size;

	private final Vector<Triplet> trios = new Vector<Triplet>();

	private final int tagCount;

	private static class Triplet
	{
		/** Tag identifier */
		private final int signature;
		/** absolute offset of tag data */
		private final int offset;
		/** length of tag data */
		private final int count;
		/** size of an entry */
		public static final int size = 3 * ICCProfile.int_size;

		Triplet(final int signature, final int offset, final int count)
		{
			this.signature = signature;
			this.offset = offset;
			this.count = count;
		}
	}

	/**
	 * Representation of a tag table
	 * 
	 * @return String
	 */
	@Override
	public synchronized String toString()
	{
		final StringBuffer rep = new StringBuffer("[ICCTagTable containing " + this.tagCount + " tags:");
		final StringBuffer body = new StringBuffer("  ");
		final Enumeration<Integer> keys = this.keys();
		Iterator<Integer> iterator = keys.asIterator();
		while (iterator.hasNext())
		{
			final Integer key = iterator.next();
			final ICCTag tag = this.get(key);
			body.append(ICCTagTable.eol).append(tag.toString());
		}
		rep.append(ColorSpace.indent("  ", body));
		return rep.append("]").toString();
	}

	/**
	 * Factory method for creating a tag table from raw input.
	 * 
	 * @param data array of unstructured data representing a tag
	 * @return ICCTagTable
	 */
	public static ICCTagTable createInstance(final byte[] data)
	{
		final ICCTagTable tags = new ICCTagTable(data);
		return tags;
	}

	/**
	 * Ctor used by factory method.
	 * 
	 * @param data raw tag data
	 */
	protected ICCTagTable(final byte[] data)
	{
		this.tagCount = ICCProfile.getInt(data, ICCTagTable.offTagCount);

		int offset = ICCTagTable.offTags;
		for (int i = 0; i < this.tagCount; ++i)
		{
			final int signature = ICCProfile.getInt(data, offset);
			final int tagOffset = ICCProfile.getInt(data, offset + ICCProfile.int_size);
			final int length = ICCProfile.getInt(data, offset + 2 * ICCProfile.int_size);
			this.trios.addElement(new Triplet(signature, tagOffset, length));
			offset += 3 * ICCProfile.int_size;
		}

		Iterator<Triplet> iterator = trios.iterator();
		while (iterator.hasNext())
		{
			final Triplet trio = iterator.next();
			final ICCTag tag = ICCTag.createInstance(trio.signature, data, trio.offset, trio.count);
			this.put(Integer.valueOf(tag.signature), tag);
		}
	}

	/**
	 * Output the table to a disk
	 * 
	 * @param raf
	 *            RandomAccessFile which receives the table.
	 * @exception IOException
	 */
	public void write(final RandomAccessFile raf) throws IOException
	{

		final int ntags = this.trios.size();

		final int countOff = ICCProfileHeader.size;
		final int tagOff = countOff + ICCProfile.int_size;
		final int dataOff = tagOff + 3 * ntags * ICCProfile.int_size;

		raf.seek(countOff);
		raf.writeInt(ntags);

		int currentTagOff = tagOff;
		int currentDataOff = dataOff;

		Iterator<Triplet> iterator = trios.iterator();
		while (iterator.hasNext())
		{
			final Triplet trio = iterator.next();
			final ICCTag tag = this.get(Integer.valueOf(trio.signature));

			raf.seek(currentTagOff);
			raf.writeInt(tag.signature);
			raf.writeInt(currentDataOff);
			raf.writeInt(tag.count);
			currentTagOff += 3 * Triplet.size;

			raf.seek(currentDataOff);
			raf.write(tag.data, tag.offset, tag.count);
			currentDataOff += tag.count;
		}
	}

	/* end class ICCTagTable */
}
