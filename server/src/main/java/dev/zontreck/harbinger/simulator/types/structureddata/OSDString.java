package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class OSDString extends OSD {
	public final String value;

	public OSDString(String value) {
		Type = OSDType.OSString;
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

		return true;
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
		return UUID.fromString(value);
	}

	@Override
	public Instant AsInstant() {
		return Instant.parse(value);
	}

	@Override
	public URI AsUri() {
		return URI.create(value);
	}

	@Override
	public String toString() {
		return AsString();
	}
}
