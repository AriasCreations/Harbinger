package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.SimUtils;

public class OSDInteger extends OSD {
	public final int value;

	public OSDInteger(int value) {
		Type = OSDType.Integer;
		this.value = value;
	}

	@Override
	public boolean AsBoolean() {
		return value != 0;
	}

	@Override
	public int AsInteger() {
		return value;
	}


	@Override
	public long AsLong() {
		return value;
	}

	@Override
	public double AsReal() {
		return value;
	}

	@Override
	public String AsString() {
		return value.toString();
	}

	@Override
	public byte[] AsBinary() {
		return SimUtils.IntToBytesBig(value);
	}

	@Override
	public OSD Copy() {
		return new OSDInteger(value);
	}

	@Override
	public String toString() {
		return AsString();
	}
}
