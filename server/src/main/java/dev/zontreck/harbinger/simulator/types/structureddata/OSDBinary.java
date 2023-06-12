package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import dev.zontreck.harbinger.utils.DigestUtils;
import dev.zontreck.harbinger.utils.SimUtils;

public class OSDBinary extends OSD {
	public final byte[] value;

	public OSDBinary(byte[] value) {
		Type = OSDType.Binary;
		if (value != null)
			this.value = value;
		else
			this.value = new byte[0];
	}


	public OSDBinary(long value) {
		Type = OSDType.Binary;
		this.value = new byte[]
				{
						(byte) ((value >> 56) & 0xFF),
						(byte) ((value >> 48) & 0xFF),
						(byte) ((value >> 40) & 0xFF),
						(byte) ((value >> 32) & 0xFF),
						(byte) ((value >> 24) & 0xFF),
						(byte) ((value >> 16) & 0xFF),
						(byte) ((value >> 8) & 0xFF),
						(byte) (value & 0xFF)
				};
	}


	@Override
	public OSD Copy() {
		return new OSDBinary(value);
	}

	@Override
	public String AsString() {
		return DigestUtils.base64(value);
	}

	@Override
	public byte[] AsBinary() {
		return value;
	}

	@Override
	public int AsInteger() {
		return (value[0] << 24) | (value[1] << 16) | (value[2] << 8) | (value[3] << 0);
	}

	@Override
	public long AsLong() {
		return ((long) value[0] << 56) |
				((long) value[1] << 48) |
				((long) value[2] << 40) |
				((long) value[3] << 32) |
				((long) value[4] << 24) |
				((long) value[5] << 16) |
				((long) value[6] << 8) |
				((long) value[7] << 0);
	}

	@Override
	public String toString() {
		return SimUtils.BytesToHexString(value, null);
	}
}
