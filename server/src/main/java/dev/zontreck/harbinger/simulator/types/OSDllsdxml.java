package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.nio.charset.StandardCharsets;

public class OSDllsdxml extends OSD {
	public final String value;

	public OSDllsdxml(String value) {
		Type = OSDType.LLSDxml;
		// Refuse to hold null pointers
		if (value != null)
			this.value = value;
		else
			this.value = "";
	}

	@Override
	public OSD Copy() {
		return new OSDllsdxml(value);
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
	public String toString() {
		return AsString();
	}
}
