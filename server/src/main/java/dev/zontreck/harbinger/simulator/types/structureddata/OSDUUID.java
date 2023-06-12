package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.util.UUID;

public class OSDUUID extends OSD {
	public final UUID value;

	public static final UUID NULL_ID = new UUID(0,0);

	public static final OSDUUID ZERO = new OSDUUID(NULL_ID);

	public OSDUUID(UUID value) {
		Type = OSDType.UUID;
		this.value = value;
	}

	@Override
	public OSD Copy() {
		return new OSDUUID(value);
	}

	@Override
	public boolean AsBoolean() {
		return !(value.getLeastSignificantBits() == 0 && value.getMostSignificantBits() == 0);
	}

	@Override
	public String AsString() {
		return value.toString();
	}

	@Override
	public UUID AsUUID() {
		return value;
	}

	@Override
	public String toString() {
		return AsString();
	}
}
