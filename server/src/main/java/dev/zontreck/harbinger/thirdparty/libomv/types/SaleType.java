package dev.zontreck.harbinger.thirdparty.libomv.types;

public enum SaleType
{
	/** Not for sale */
	Not,
	/** The original is for sale */
	Original,
	/** Copies are for sale */
	Copy,
	/** The contents of the object are for sale */
	Contents;

	private static final String[] _SaleTypeNames = { "not", "orig", "copy", "cntn" };

	/**
	 * Translate a string name of an SaleType into the proper Type
	 *
	 * @param value
	 *            A string containing the SaleType name
	 * @return The SaleType which matches the string name, or
	 *         SaleType.Unknown if no match was found
	 */
	public static SaleType setValue(final String value)
	{
		for (int i = 0; i < SaleType._SaleTypeNames.length; i++)
		{
			if (0 == value.compareToIgnoreCase(_SaleTypeNames[i]))
			{
				return SaleType.values()[i];
			}
		}
		return SaleType.Not;
	}

	public static SaleType setValue(final int value)
	{
		if (0 <= value && value < SaleType.values().length)
			return SaleType.values()[value];
		return null;
	}

	public byte getValue()
	{
		return (byte) this.ordinal();
	}

	@Override
	public String toString()
	{
		return SaleType._SaleTypeNames[this.ordinal()];
	}
}
