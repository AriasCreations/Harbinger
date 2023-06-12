package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.llsd.OSDParser;
import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public sealed class OSDMap extends OSD implements Map<String, OSD> {
	public final Map<String, OSD> dicvalue;

	public OSDMap() {
		Type = OSDType.Map;
		dicvalue = new HashMap<>();
	}


	public OSDMap(int capacity) {
		Type = OSDType.Map;
		dicvalue = new HashMap<>(capacity);
	}

	public OSDMap(Map<String, OSD> value) {
		Type = OSDType.Map;
		if (value != null)
			dicvalue = value;
		else
			dicvalue = new HashMap<>();
	}

	@Override
	public String toString() {
		return OSDParser.SerializeJsonString(this, true);
	}

	@Override
	public OSD Copy() {
		return new OSDMap(new HashMap<String, OSD>(dicvalue));
	}


	public int Count() {
		return dicvalue.size();
	}

	public boolean IsReadOnly = false;

	public void Add(String key, OSD llsd) {
		dicvalue.put(key, llsd);
	}

	public boolean Remove(String key) {
		return dicvalue.remove(key) != null;
	}

	@Override
	public void Clear() {
		dicvalue.clear();
	}

	@Override
	public int size() {
		return dicvalue.size();
	}

	@Override
	public boolean isEmpty() {
		return dicvalue.isEmpty();
	}

	@Override
	public boolean containsKey(Object o) {
		return dicvalue.containsKey(o);
	}

	@Override
	public boolean containsValue(Object o) {
		return dicvalue.containsValue(o);
	}

	@Override
	public OSD get(Object o) {
		return dicvalue.get(o);
	}

	@Nullable
	@Override
	public OSD put(String s, OSD osd) {
		return dicvalue.put(s, osd);
	}

	@Override
	public OSD remove(Object o) {
		return dicvalue.remove(o);
	}

	@Override
	public void putAll(@NotNull Map<? extends String, ? extends OSD> map) {
		dicvalue.putAll(map);
	}

	@Override
	public void clear() {
		dicvalue.clear();
	}

	@NotNull
	@Override
	public Set<String> keySet() {
		return dicvalue.keySet();
	}

	@NotNull
	@Override
	public Collection<OSD> values() {
		return dicvalue.values();
	}

	@NotNull
	@Override
	public Set<Entry<String, OSD>> entrySet() {
		return dicvalue.entrySet();
	}
}
