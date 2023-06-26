package dev.zontreck.harbinger.data.mongo;

import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDBinary;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDParser;
import dev.zontreck.harbinger.utils.DataUtils;

import java.io.IOException;
import java.text.ParseException;

public class DBSettings {
	public String HOST = "127.0.0.1";
	public int PORT = 27017;
	public String USER = "";
	public String PASSWORD = "";
	public String DATABASE = "harbinger";


	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( "host" , OSD.FromString ( HOST ) );
		map.put ( "port" , OSD.FromInteger ( PORT ) );
		map.put ( "user" , OSD.FromString ( USER ) );
		map.put ( "pwd" , OSD.FromString ( PASSWORD ) );
		map.put ( "db" , OSD.FromString ( DATABASE ) );

		return map;
	}

	public DBSettings ( ) { }

	public DBSettings ( final OSD mp ) {
		if ( mp instanceof final OSDMap map ) {
			HOST = map.get ( "host" ).AsString ( );
			PORT = map.get ( "port" ).AsInteger ( );
			USER = map.get ( "user" ).AsString ( );
			PASSWORD = map.get ( "pwd" ).AsString ( );
			DATABASE = map.get ( "db" ).AsString ( );
		}
	}


	public static DBSettings instance = new DBSettings ( );

	static {
		LOAD ( );
	}


	public static void LOAD ( ) {
		try {
			byte[] arr = DataUtils.ReadAllBytes ( HarbingerServer.BASE_PATH.resolve ( "db.bin" ) );
			final OSD deserialize = OSDParser.deserialize ( arr );
			DBSettings.instance = new DBSettings ( deserialize );
		} catch ( Exception e)
		{
			return;
		}
	}

	public static void SAVE ( ) {
		try {
			byte[] arr = LLSDBinary.serializeToBytes ( instance.save ( ) , OSD.OSDFormat.Binary , true );

			DataUtils.WriteFileBytes ( HarbingerServer.BASE_PATH.resolve ( "db.bin" ) , arr );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}
	}
}
