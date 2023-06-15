package dev.zontreck.harbinger.simulator.types;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Location {

	@Element
	public String RegionName = "Harbinger";

	@Element
	public float X = 128;

	@Element
	public float Y = 128;

	@Element
	public float Z = 50;



	public Location()
	{

	}
}
