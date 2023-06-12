package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.SimUtils;

public class OSDReal extends OSD {
	public final double value;

	public OSDReal(double value) {
		Type = OSDType.Real;
		this.value = value;
	}

	@Override
	public boolean AsBoolean() {
		return !Double.isNaN(value) && value != 0d;
	}

	@Override
	public OSD Copy() {
		return new OSDReal(value);
	}

	@Override
	public int AsInteger() {
		if (Double.isNaN(value))
			return 0;
		if (value > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		if (value < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		return (int) Math.round(value);
	}

	@Override
	public long AsLong() {
		if (Double.isNaN(value))
			return 0;
		if (value > Long.MAX_VALUE)
			return Long.MAX_VALUE;
		if (value < Long.MIN_VALUE)
			return Long.MIN_VALUE;
		return Math.round(value);
	}

	@Override
	public double AsReal() {
		return value;
	}

	// "r" ensures the value will correctly round-trip back through Double.TryParse
	@Override
	public String AsString() {
		return String.valueOf(value);
	}

	@Override
	public String toString() {
		return AsString();
	}
}
