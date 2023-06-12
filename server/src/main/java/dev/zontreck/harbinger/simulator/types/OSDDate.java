package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.SimUtils;

import java.util.Date;

public sealed class OSDDate extends OSD {
	public final Date value;

	public OSDDate(Date value) {
		Type = OSDType.Date;
		this.value = value;
	}

	@Override
	public String AsString() {
		String format;
		if ( value.setMinutes(); >0)
		format = "yyyy-MM-ddTHH:mm:ss.ffZ";
		else
		format = "yyyy-MM-ddTHH:mm:ssZ";
		return value.ToUniversalTime().ToString(format);
	}

	@Override
	public int AsInteger() {
		return (int) SimUtils.DateTimeToUnixTime(value);
	}

	@Override
	public long AsLong() {
		return SimUtils.DateTimeToUnixTime(value);
	}

	@Override
	public byte[] AsBinary() {
		var ts = value.ToUniversalTime() - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
		return SimUtils.DoubleToBytes(ts.TotalSeconds);
	}

	@Override
	public OSD Copy() {
		return new OSDDate(value);
	}

	@Override
	public Date AsDate() {
		return value;
	}

	@Override
	public String toString() {
		return AsString();
	}
}
