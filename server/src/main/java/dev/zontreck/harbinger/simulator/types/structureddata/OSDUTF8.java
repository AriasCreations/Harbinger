package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.simulator.types.osUTF8;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

public sealed class OSDUTF8 extends OSD {
	public final osUTF8 value;

	public OSDUTF8(osUTF8 value) {
		Type = OSDType.OSDUTF8;
		// Refuse to hold null pointers
		if (value != null)
			this.value = value;
		else
			this.value = new osUTF8();
	}

	public OSDUTF8(byte[] value) {
		Type = OSDType.OSDUTF8;
		// Refuse to hold null pointers
		if (value != null)
			this.value = new osUTF8(value);
		else
			this.value = new osUTF8();
	}

	public OSDUTF8(String value) {
		Type = OSDType.OSDUTF8;
		// Refuse to hold null pointers
		if (value != null)
			this.value = new osUTF8(value);
		else
			this.value = new osUTF8();
	}

	@Override
	public OSD Copy() {
		return new OSDUTF8(value.Clone());
	}

	@Override

	public boolean AsBoolean() {
		if (osUTF8.IsNullOrEmpty(value))
			return false;

		return !value.Equals('0') && !value.ACSIILowerEquals("false");
	}

	@Override
	public int AsInteger() {
		double dbl;
		if (Double.TryParse(value.ToString(), out dbl))
			return (int) Math.Floor(dbl);
		return 0;
	}

	@Override
	public long AsLong() {
		double dbl;
		if (double.TryParse(value.ToString(), out dbl))
			return (long) Math.Floor(dbl);
		return 0;
	}

	@Override
	public double AsReal() {
		double dbl;
		if (double.TryParse(value.ToString(), out dbl))
			return dbl;
		return 0d;
	}

	@Override
	public String AsString() {
		return value.ToString();
	}

	@Override
	public byte[] AsBinary() {
		return value.ToArray();
	}

	@Override
	public UUID AsUUID() {
		UUID uuid;
		if (UUID.TryParse(value.ToString().AsSpan(), out uuid))
			return uuid;
		return new UUID(0, 0);
	}

	@Override
	public Date AsDate() {
		Date dt;
		if (Date.TryParse(value.ToString(), out dt))
			return dt;
		return SimUtils.Epoch;
	}

	@Override
	public URI AsUri() {
		URI uri;
		if (URI.TryCreate(value.ToString(), UriKind.RelativeOrAbsolute, out uri))
			return uri;
		return null;
	}

	@Override
	public String toString() {
		return AsString();
	}
}
