package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.time.Instant;

public class SimulatorSettings {
	public static final String TAG = "simulator";
	public String BASE_URL = "http://localhost:7768";
	public String GRID_NAME = "Dark Space";
	public String GRID_NICK = "space";

	public Boolean GRID_ON = false;
	public Boolean SIM_ON = false;


	public Instant LAST_TOS_UPDATE;
	public Instant LAST_PATCHNOTES_UPDATE;


	public OSD serialize ( ) {
		OSDMap simsettings = new OSDMap ( );
		simsettings.put ( "base_url" , OSD.FromString ( BASE_URL ) );
		simsettings.put("name", OSD.FromString ( GRID_NAME ));
		simsettings.put("nick", OSD.FromString ( GRID_NICK ));
		simsettings.put("grid", OSD.FromBoolean ( GRID_ON ));
		simsettings.put("sim", OSD.FromBoolean ( SIM_ON ));

		simsettings.put("tos", OSD.FromLong ( LAST_TOS_UPDATE.getEpochSecond () ));
		simsettings.put("patch", OSD.FromLong(LAST_PATCHNOTES_UPDATE.getEpochSecond ()));


		return simsettings;
	}

	public SimulatorSettings ( ) {

	}

	public SimulatorSettings ( OSD val ) {
		OSDMap map = ( OSDMap ) val;
		try{

			BASE_URL = map.get ( "base_url" ).AsString ( );
			GRID_NAME = map.get("name").AsString ();
			GRID_NICK = map.get("nick").AsString ();
			GRID_ON = map.get ( "grid" ).AsBoolean ();
			SIM_ON = map.get ( "sim" ).AsBoolean ();
			LAST_TOS_UPDATE = Instant.ofEpochSecond ( map.get ( "tos" ).AsLong () );
			LAST_PATCHNOTES_UPDATE = Instant.ofEpochSecond ( map.get ( "patch" ).AsLong () );


		}catch(Exception e){

		}
	}
}
