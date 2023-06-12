package dev.zontreck.harbinger.simulator.types.structureddata;

import dev.zontreck.harbinger.simulator.types.enums.OSDType;

import java.util.List;

public sealed class OSDArray extends OSD implements List<OSD> {
	public final List<OSD> value;

	public OSDArray() {
		Type = OSDType.Array;
		value = new List<OSD>();
	}

	public OSDArray(int capacity) {
		Type = OSDType.Array;
		value = new List<OSD>(capacity);
	}

	public OSDArray(List<OSD> value) {
		Type = OSDType.Array;
		if (value != null)
			this.value = value;
		else
			this.value = new List<OSD>();
	}

	@Override
	public byte[] AsBinary() {
		var binary = new byte[value.Count];

		for (var i = 0; i < value.Count; i++)
			binary[i] = (byte) value[i].AsInteger();

		return binary;
	}

	public override

	long AsLong() {
		if (value.Count < 8)
			return 0;
		var b = new byte[8];
		for (var i = 0; i < 8; i++)
			b[i] = (byte) value[i].AsInteger();
		return ((long) b[0] << 56) |
				((long) b[1] << 48) |
				((long) b[2] << 40) |
				((long) b[3] << 32) |
				((long) b[4] << 24) |
				((long) b[5] << 16) |
				((long) b[6] << 8) |
				b[7];
	}

	public override ulong

	AsULong() {
		if (value.Count < 8)
			return 0;
		var b = new byte[8];
		for (var i = 0; i < 8; i++)
			b[i] = (byte) value[i].AsInteger();
		return ((ulong) b[0] << 56) |
				((ulong) b[1] << 48) |
				((ulong) b[2] << 40) |
				((ulong) b[3] << 32) |
				((ulong) b[4] << 24) |
				((ulong) b[5] << 16) |
				((ulong) b[6] << 8) |
				b[7];
	}

	public override

	int AsInteger() {
		if (value.Count < 4)
			return 0;
		var by = new byte[4];
		for (var i = 0; i < 4; i++)
			by[i] = (byte) value[i].AsInteger();
		return (by[0] << 24) | (by[1] << 16) | (by[2] << 8) | by[3];
	}

	public override uint

	AsUInteger() {
		if (value.Count < 4)
			return 0;
		var by = new byte[4];
		for (var i = 0; i < 4; i++)
			by[i] = (byte) value[i].AsInteger();
		return (uint) ((by[0] << 24) | (by[1] << 16) | (by[2] << 8) | by[3]);
	}

	/*
	public override Vector2 AsVector2()
	{
		Vector2 vector = Vector2.Zero;

		if (this.Count == 2)
		{
			vector.X = (float)this[0].AsReal();
			vector.Y = (float)this[1].AsReal();
		}

		return vector;
	}

	public override Vector3 AsVector3()
	{
		Vector3 vector = Vector3.Zero;

		if (this.Count == 3)
		{
			vector.X = this[0].AsReal();
			vector.Y = this[1].AsReal();
			vector.Z = this[2].AsReal();
		}

		return vector;
	}

	public override Vector3d AsVector3d()
	{
		Vector3d vector = Vector3d.Zero;

		if (this.Count == 3)
		{
			vector.X = this[0].AsReal();
			vector.Y = this[1].AsReal();
			vector.Z = this[2].AsReal();
		}

		return vector;
	}

	public override Vector4 AsVector4()
	{
		Vector4 vector = Vector4.Zero;

		if (this.Count == 4)
		{
			vector.X = (float)this[0].AsReal();
			vector.Y = (float)this[1].AsReal();
			vector.Z = (float)this[2].AsReal();
			vector.W = (float)this[3].AsReal();
		}

		return vector;
	}

	public override Quaternion AsQuaternion()
	{
		Quaternion quaternion = Quaternion.Identity;

		if (this.Count == 4)
		{
			quaternion.X = (float)this[0].AsReal();
			quaternion.Y = (float)this[1].AsReal();
			quaternion.Z = (float)this[2].AsReal();
			quaternion.W = (float)this[3].AsReal();
		}

		return quaternion;
	}
	*/
	public override Color4

	AsColor4() {
		var color = Color4.Black;

		if (Count == 4) {
			color.R = (float) this[0].AsReal();
			color.G = (float) this[1].AsReal();
			color.B = (float) this[2].AsReal();
			color.A = (float) this[3].AsReal();
		}

		return color;
	}

	public override OSD

	Copy() {
		return new OSDArray(new List<OSD>(value));
	}

	public override string

	ToString() {
		return OSDParser.SerializeJsonString(this, true);
	}

		#
	region IList
	Implementation

	public int Count =>value.Count;
	public bool IsReadOnly =>false;

	public OSD this[
	int index]

	{
		get =>value[index];
		set =>this.value[index] = value;
	}

	public int IndexOf(OSD llsd) {
		return value.IndexOf(llsd);
	}

	public void Insert(int index, OSD llsd) {
		value.Insert(index, llsd);
	}

	public void RemoveAt(int index) {
		value.RemoveAt(index);
	}

	public void Add(OSD llsd) {
		value.Add(llsd);
	}

	public override

	void Clear() {
		value.Clear();
	}

	public bool Contains(OSD llsd) {
		return value.Contains(llsd);
	}

	public bool Contains(string element) {
		for (var i = 0; i < value.Count; i++)
			if (value[i].Type == OSDType.String && value[i].AsString() == element)
				return true;

		return false;
	}

	public void CopyTo(OSD[] array, int index) {
		throw new NotImplementedException();
	}

	public bool Remove(OSD llsd) {
		return value.Remove(llsd);
	}

	IEnumerator IEnumerable.

	GetEnumerator() {
		return value.GetEnumerator();
	}

	IEnumerator<OSD> IEnumerable<OSD>.

	GetEnumerator() {
		return value.GetEnumerator();
	}

}
