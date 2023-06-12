package dev.zontreck.harbinger.simulator.types.structureddata.json;


import dev.zontreck.harbinger.simulator.types.enums.JsonType;

import java.util.List;

public interface IJsonWrapper extends List {
	boolean IsArray();

	boolean IsBoolean();

	boolean IsDouble();

	boolean IsInt();

	boolean IsLong();

	boolean IsObject();

	boolean IsString();

	boolean GetBoolean();

	double GetDouble();

	int GetInt();

	JsonType GetJsonType();

	long GetLong();

	String GetString();

	void SetBoolean(boolean val);

	void SetDouble(double val);

	void SetInt(int val);

	void SetJsonType(JsonType type);

	void SetLong(long val);

	void SetString(String val);

	String ToJson();

	void ToJson(JsonWriter writer);
}
