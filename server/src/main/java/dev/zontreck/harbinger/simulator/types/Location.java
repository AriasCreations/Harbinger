package dev.zontreck.harbinger.simulator.types;


import dev.zontreck.ariaslib.json.DynSerial;

@DynSerial
public class Location {

	public String RegionName = "Harbinger";
	public float X = 128;
	public float Y = 128;
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
