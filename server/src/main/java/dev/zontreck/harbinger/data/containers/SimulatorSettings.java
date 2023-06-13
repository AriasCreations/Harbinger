package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

public class SimulatorSettings {
	public static final String TAG = "simulator";
	public String BASE_URL;


	public OSD serialize ( ) {
		OSDMap simsettings = new OSDMap ( );
		simsettings.put ( "base_url" , OSD.FromString ( BASE_URL ) );


		return simsettings;
	}

	public SimulatorSettings ( ) {

	}

	public SimulatorSettings ( OSD val ) {
		OSDMap map = ( OSDMap ) val;
		String url = map.get ( "base_url" ).AsString ( );


		BASE_URL = url;
	}
}
