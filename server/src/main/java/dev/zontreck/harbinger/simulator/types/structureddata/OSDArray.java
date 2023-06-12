package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.llsd.OSDParser;
import dev.zontreck.harbinger.simulator.types.*;
import dev.zontreck.harbinger.simulator.types.enums.OSDType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OSDArray extends OSD implements List<OSD> {
	public final List<OSD> value;

	public OSDArray() {
		Type = OSDType.OSArray;
		value = new ArrayList<>();
	}

	public OSDArray(int capacity) {
		Type = OSDType.OSArray;
		value = new ArrayList<>();
	}

	public OSDArray(List<OSD> value) {
		Type = OSDType.OSArray;
		if (value != null)
			this.value = value;
		else
			this.value = new ArrayList<>();
	}

	@Override
	public byte[] AsBinary() {
		var binary = new byte[value.size()];

		for (var i = 0; i < value.size(); i++)
			binary[i] = (byte) value.get(i).AsInteger();

		return binary;
	}

	@Override
	public long AsLong() {
		if (value.size() < 8)
			return 0;
		var b = new byte[8];
		for (var i = 0; i < 8; i++)
			b[i] = (byte) value.get(i).AsInteger();
		return ((long) b[0] << 56) |
				((long) b[1] << 48) |
				((long) b[2] << 40) |
				((long) b[3] << 32) |
				((long) b[4] << 24) |
				((long) b[5] << 16) |
				((long) b[6] << 8) |
				b[7];
	}

	@Override
	public int AsInteger() {
		if (value.size() < 4)
			return 0;
		var by = new byte[4];
		for (var i = 0; i < 4; i++)
			by[i] = (byte) value.get(i).AsInteger();
		return (by[0] << 24) | (by[1] << 16) | (by[2] << 8) | by[3];
	}


	@Override
	public Vector2 AsVector2() {
		Vector2 vector = Vector2.Zero;

		if (this.Count() == 2) {
			vector.X = (float) this.get(0).AsReal();
			vector.Y = (float) this.get(1).AsReal();
		}

		return vector;
	}

	@Override
	public Vector3 AsVector3() {
		Vector3 vector = Vector3.Zero;

		if (this.Count() == 3) {
			vector.X = (float) this.get(0).AsReal();
			vector.Y = (float) this.get(1).AsReal();
			vector.Z = (float) this.get(2).AsReal();
		}

		return vector;
	}

	@Override
	public Vector3d AsVector3d() {
		Vector3d vector = Vector3d.Zero;

		if (this.Count() == 3) {
			vector.X = this.get(0).AsReal();
			vector.Y = this.get(0).AsReal();
			vector.Z = this.get(0).AsReal();
		}

		return vector;
	}

	@Override
	public Vector4 AsVector4() {
		Vector4 vector = Vector4.Zero;

		if (this.Count() == 4) {
			vector.X = (float) this.get(0).AsReal();
			vector.Y = (float) this.get(1).AsReal();
			vector.Z = (float) this.get(2).AsReal();
			vector.W = (float) this.get(3).AsReal();
		}

		return vector;
	}

	@Override
	public Quaternion AsQuaternion() {
		Quaternion quaternion = Quaternion.Identity;

		if (this.Count() == 4) {
			quaternion.X = (float) this.get(0).AsReal();
			quaternion.Y = (float) this.get(1).AsReal();
			quaternion.Z = (float) this.get(2).AsReal();
			quaternion.W = (float) this.get(3).AsReal();
		}

		return quaternion;
	}

	@Override
	public Color4 AsColor4() {
		var color = Color4.Black;

		if (Count() == 4) {
			color.R = (float) this.get(0).AsReal();
			color.G = (float) this.get(1).AsReal();
			color.B = (float) this.get(2).AsReal();
			color.A = (float) this.get(3).AsReal();
		}

		return color;
	}

	@Override
	public OSD Copy() {
		return new OSDArray(new ArrayList<OSD>(value));
	}

	@Override
	public String toString() {
		return OSDParser.SerializeJsonString(this, true);
	}


	public int Count() {
		return value.size();
	}

	public boolean IsReadOnly = false;

	public int IndexOf(OSD llsd) {
		return value.indexOf(llsd);
	}

	public void Insert(int index, OSD llsd) {
		value.add(index, llsd);
	}

	public void RemoveAt(int index) {
		value.remove(index);
	}

	public void Add(OSD llsd) {
		value.add(llsd);
	}

	@Override
	public void Clear() {
		value.clear();
	}

	public boolean Contains(OSD llsd) {
		return value.contains(llsd);
	}

	public boolean Contains(String element) {
		for (var i = 0; i < value.size(); i++)
			if (value.get(i).Type == OSDType.String && value.get(i).AsString() == element)
				return true;

		return false;
	}

	public boolean Remove(OSD llsd) {
		return value.remove(llsd);
	}

	@Override
	public int size() {
		return value.size();
	}

	@Override
	public boolean isEmpty() {
		return value.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return value.contains(o);
	}

	@NotNull
	@Override
	public Iterator<OSD> iterator() {
		return value.iterator();
	}

	@NotNull
	@Override
	public Object[] toArray() {
		return value.toArray();
	}

	@NotNull
	@Override
	public <T> T[] toArray(@NotNull T[] ts) {
		return value.toArray(ts);
	}

	@Override
	public boolean add(OSD osd) {
		return value.add(osd);
	}

	@Override
	public boolean remove(Object o) {
		return value.remove(o);
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> collection) {
		return value.containsAll(collection);
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends OSD> collection) {
		return value.addAll(collection);
	}

	@Override
	public boolean addAll(int i, @NotNull Collection<? extends OSD> collection) {
		return value.addAll(collection);
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		return value.removeAll(collection);
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		return value.retainAll(collection);
	}

	@Override
	public void clear() {
		Clear();
	}

	@Override
	public OSD get(int i) {
		return value.get(i);
	}

	@Override
	public OSD set(int i, OSD osd) {
		return value.set(i,osd);
	}

	@Override
	public void add(int i, OSD osd) {
		value.set(i,osd);
	}

	@Override
	public OSD remove(int i) {
		return value.remove(i);
	}

	@Override
	public int indexOf(Object o) {
		return value.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return value.lastIndexOf(o);
	}

	@NotNull
	@Override
	public ListIterator<OSD> listIterator() {
		return value.listIterator();
	}

	@NotNull
	@Override
	public ListIterator<OSD> listIterator(int i) {
		return value.listIterator(i);
	}

	@NotNull
	@Override
	public List<OSD> subList(int i, int i1) {
		return value.subList(i, i1);
	}
}
