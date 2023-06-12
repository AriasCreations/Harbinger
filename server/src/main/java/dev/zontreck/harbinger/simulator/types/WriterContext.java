package dev.zontreck.harbinger.simulator.types;

public class WriterContext {
	public int Count;
	public boolean ExpectingValue;
	public boolean InArray;
	public boolean InObject;
	public int Padding;
}
