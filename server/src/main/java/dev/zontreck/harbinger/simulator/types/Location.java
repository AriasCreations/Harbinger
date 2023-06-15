package dev.zontreck.harbinger.simulator.types;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root (strict = false)
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

	public Location(String last)
	{
		// TODO
	}


	public String interpret(Account act)
	{
		if(act.LastLocation.RegionName == RegionName) return "last";
		if(RegionName.equals ( act.HomeLocation.RegionName )) return "home";
		else return RegionName+"/"+X+"/"+Y+"/"+Z;
	}
}
