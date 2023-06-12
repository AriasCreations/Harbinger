package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.simulator.types.osUTF8;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class OSDUTF8 extends OSD {
	public final osUTF8 value;

	public OSDUTF8(osUTF8 value) {
		Type = OSDType.OSOSDUTF8;
		// Refuse to hold null pointers
		if (value != null)
			this.value = value;
		else
			this.value = new osUTF8();
	}

	public OSDUTF8(byte[] value) {
		Type = OSDType.OSOSDUTF8;
		// Refuse to hold null pointers
		if (value != null)
			this.value = new osUTF8(value);
		else
			this.value = new osUTF8();
	}

	public OSDUTF8(String value) {
		Type = OSDType.OSOSDUTF8;
		// Refuse to hold null pointers
		if (value != null)
			this.value = new osUTF8(value);
		else
			this.value = new osUTF8();
	}

	@Override
	public int AsInteger() {
		return Integer.parseInt(value.toString());
	}

	@Override
	public long AsLong() {
		return Long.parseLong(value.toString());
	}

	@Override
	public double AsReal() {
		return Double.parseDouble(value.toString());
	}

	@Override
	public String AsString() {
		return value.toString();
	}

	@Override
	public byte[] AsBinary() {
		return value.ToArray();
	}

	@Override
	public UUID AsUUID() {
		return UUID.fromString(value.toString());
	}

	@Override
	public Instant AsInstant() {
		return Instant.parse(value.toString());
	}

	@Override
	public URI AsUri() {
		return URI.create(value.toString());
	}

	@Override
	public String toString() {
		return AsString();
	}
}
