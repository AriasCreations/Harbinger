package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public sealed class OSDUri extends OSD {
	public final URI value;

	public OSDUri(URI value) {
		Type = OSDType.URI;
		this.value = value;
	}

	@Override
	public String AsString() {
		if (value != null) {
			if (value.IsAbsoluteUri)
				return value.AbsoluteUri;
			return value.toString();
		}

		return "";
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
