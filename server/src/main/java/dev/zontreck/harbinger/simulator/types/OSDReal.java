package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.SimUtils;

public sealed class OSDReal extends OSD {
	public final double value;

	public OSDReal(double value) {
		Type = OSDType.Real;
		this.value = value;
	}

	@Override
	public boolean AsBoolean() {
		return !double.IsNaN(value) && value != 0d;
	}

	@Override
	public OSD Copy() {
		return new OSDReal(value);
	}

	@Override
	public int AsInteger() {
		if (double.IsNaN(value))
			return 0;
		if (value > int.MaxValue)
			return int.MaxValue;
		if (value < int.MinValue)
			return int.MinValue;
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
		return value.toString("r", SimUtils.EnUsCulture);
	}

	@Override
	public byte[] AsBinary() {
		return SimUtils.DoubleToBytesBig(value);
	}

	@Override
	public String toString() {
		return AsString();
	}
}
