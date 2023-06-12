package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public sealed class OSDString extends OSD {
	public final String value;

	public OSDString(String value) {
		Type = OSDType.String;
		// Refuse to hold null pointers
		if (value != null)
			this.value = value;
		else
			this.value = "";
	}

	@Override
	public OSD Copy() {
		return new OSDString(value);
	}

	@Override
	public boolean AsBoolean() {
		if (value.isEmpty())
			return false;

		return value != "0" && value.ToLower() != "false";
	}

	@Override
	public int AsInteger() {
		double dbl;
		if (double.TryParse(value, out dbl))
			return (int) Math.floor(dbl);
		return 0;
	}

	@Override
	public long AsLong() {
		double dbl;
		if (double.TryParse(value, out dbl))
			return (long) Math.Floor(dbl);
		return 0;
	}

	@Override
	public double AsReal() {
		double dbl;
		if (double.TryParse(value, out dbl))
			return dbl;
		return 0d;
	}

	@Override
	public String AsString() {
		return value;
	}

	@Override
	public byte[] AsBinary() {
		return value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public UUID AsUUID() {
		if (UUID.TryParse(value.AsSpan(), out var uuid))
			return uuid;
		return UUID.Zero;
	}

	@Override
	public Date AsDate() {
		Date dt;
		if (Date.TryParse(value, out dt))
			return dt;
		return SimUtils.Epoch;
	}

	@Override
	public URI AsUri() {
		URI uri;
		if (URI.TryCreate(value, UriKind.RelativeOrAbsolute, out uri))
			return uri;
		return null;
	}

	@Override
	public String toString() {
		return AsString();
	}
}
