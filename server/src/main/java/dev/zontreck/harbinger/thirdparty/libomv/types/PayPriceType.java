package dev.zontreck.harbinger.thirdparty.libomv.types;


public enum PayPriceType
{
	// Indicates that this pay option should be hidden
	Hide(-1),

	// Indicates that this pay option should have the default value
	Default(-2);

	public static PayPriceType setValue(final int value)
	{
		if (0 <= value && value < PayPriceType.values().length)
			return PayPriceType.values()[value];
		return null;
	}

	public byte getValue()
	{
		return this._value;
	}

	private final byte _value;

	PayPriceType(final int value)
	{
		this._value = (byte) value;
	}
}