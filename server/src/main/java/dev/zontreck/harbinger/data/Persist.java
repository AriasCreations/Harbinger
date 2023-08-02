package dev.zontreck.harbinger.data;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.data.containers.*;
import dev.zontreck.harbinger.data.types.Product;
import dev.zontreck.harbinger.data.types.Server;
import dev.zontreck.harbinger.data.types.Signature;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDJson;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Persist {

	public static final Logger LOGGER = LoggerFactory.getLogger ( Persist.class.

			getSimpleName ( ) );
	public static final String FILE_NAME = "Harbinger.json";

	public static OSDMap MEMORY = new OSDMap ( );
	public static List<Product> products = new ArrayList<>();
	public static List<Server> servers = new ArrayList<> ( );
	public static HTTPServerSettings serverSettings = new HTTPServerSettings ( );

	public static Signature SIGNATURE = new Signature ( );
	public static DiscordSettings discordSettings = new DiscordSettings ( );

	public static SimulatorSettings simulatorSettings = new SimulatorSettings ( );

	public static String HARBINGER_VERSION;

	public static String HARBINGER_EXTERNAL_IP;

	public static byte[] PATCH_NOTES;

	static {
		try {
			final BufferedInputStream BIS = new BufferedInputStream ( new FileInputStream ( HarbingerServer.BASE_PATH.resolve ( FILE_NAME ).toString ( ) ) );
			final byte[] data = BIS.readAllBytes ( );

			Persist.MEMORY = ( OSDMap ) OSDParser.deserialize ( data );

			products = Product.loadProducts();

			servers = Server.loadServers();

			SupportReps.loadSupportReps ( );

			serverSettings = new HTTPServerSettings ( MEMORY.get ( HTTPServerSettings.TAG ) );

			simulatorSettings = new SimulatorSettings ( MEMORY.get ( SimulatorSettings.TAG ) );

			LOGGER.info ( "Memory file loaded" );

			BIS.close ( );
		} catch ( Exception e ) {
			//e.printStackTrace();
		}

	}

	@Subscribe
	public static void onMemoryAltered ( MemoryAlteredEvent ev ) {
		save ( );
	}

	private static void save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( HTTPServerSettings.TAG , serverSettings.save ( ) );
		map.put ( SimulatorSettings.TAG , simulatorSettings.serialize ( ) );


		LOGGER.info ( "Memory file saved" );
		try {
			String json = LLSDJson.serializeToString ( map , OSD.OSDFormat.Json );
			FileWriter fw = new FileWriter ( HarbingerServer.BASE_PATH.resolve ( FILE_NAME ).toFile ( ) );
			fw.write ( json );
			fw.close ( );
		} catch ( FileNotFoundException e ) {
			throw new RuntimeException ( e );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}

	}
}
