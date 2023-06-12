package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class OSDUri extends OSD {
	public final URI value;

	public OSDUri(URI value) {
		Type = OSDType.OSURI;
		this.value = value;
	}

	@Override
	public String AsString() {
		return value.toString();
	}

	@Override
	public OSD Copy() {
		return new OSDUri(value);
	}

	@Override
	public URI AsUri() {
		return value;
	}

	@Override
	public byte[] AsBinary() {
		return AsString().getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String toString() {
		return AsString();
	}
}
