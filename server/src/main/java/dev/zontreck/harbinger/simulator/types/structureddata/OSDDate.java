package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.SimUtils;
import dev.zontreck.harbinger.utils.TimeUtils;

import java.time.Instant;
import java.util.Date;

public class OSDDate extends OSD {
	public final Instant value;

	public OSDDate(Instant value) {
		Type = OSDType.OSDate;
		this.value = value;
	}

	@Override
	public String AsString() {
		return TimeUtils.makeTimestamp(Date.from(value));
	}

	@Override
	public int AsInteger() {
		return (int)value.getEpochSecond()/1000;
	}

	@Override
	public long AsLong()
	{
		return value.getEpochSecond();
	}


	@Override
	public OSD Copy() {
		return new OSDDate(value);
	}

	@Override
	public Instant AsInstant() {
		return value;
	}

	@Override
	public String toString() {
		return AsString();
	}
}
