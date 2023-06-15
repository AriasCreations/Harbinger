package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

public class UDPSettings
{
	public int UDPPort = 7769;
	public boolean UDPServerEnabled = false;


	public UDPSettings( OSD item )
	{
		if(item instanceof OSDMap map )
		{
			UDPPort = map.get("port").AsInteger ();
			UDPServerEnabled = map.get("enable").AsBoolean ();
		}
	}

	public OSD save()
	{
		OSDMap map = new OSDMap (  );
		map.put ( "port", OSD.FromInteger ( UDPPort ) );
		map.put("enable", OSD.FromBoolean ( UDPServerEnabled ) );

		return map;
	}
}
