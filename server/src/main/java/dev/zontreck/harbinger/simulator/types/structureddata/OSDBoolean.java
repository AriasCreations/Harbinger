package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

public class OSDBoolean extends OSD {
	public final boolean value;

	public OSDBoolean(boolean value) {
		Type = OSDType.OSBoolean;
		this.value = value;
	}

	@Override
	public boolean AsBoolean() {
		return value;
	}

	@Override
	public int AsInteger() {
		return value ? 1 : 0;
	}

	@Override
	public double AsReal() {
		return value ? 1d : 0d;
	}

	@Override
	public String AsString() {
		return value ? "1" : "0";
	}

	@Override
	public byte[] AsBinary() {
		return value ? trueBinary : falseBinary;
	}

	public OSD Copy() {
		return new OSDBoolean(value);
	}

	@Override
	public String toString() {
		return AsString();
	}
}
