package dev.zontreck.harbinger.data;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.file.AriaIO;
import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.containers.*;
import dev.zontreck.harbinger.data.types.Signature;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public enum Persist {
	;
	public static final Logger LOGGER = LoggerFactory.getLogger ( Persist.class.getSimpleName ( ) );
	public static final Path FILE_NAME = AriaIO.resolveDataFile ( "main" );

	public static Entry<List<Entry>> MEMORY = Folder.getNew ( "root" );
	public static Products products = new Products ( );
	public static Servers servers = new Servers ( );
	public static HTTPServerSettings serverSettings = new HTTPServerSettings ( );

	public static Signature SIGNATURE = new Signature ( );
	public static DiscordSettings discordSettings = new DiscordSettings ( );


	public static String HARBINGER_VERSION;

	static {
		try {

			Persist.MEMORY = ( Entry<List<Entry>> ) AriaIO.read ( Persist.FILE_NAME );

			Persist.products = new Products ( Folder.getEntry ( Persist.MEMORY , "products" ) );

			Persist.servers = Servers.deserialize ( Folder.getEntry ( Persist.MEMORY , "servers" ) );
			SupportReps.load ( Folder.getEntry ( Persist.MEMORY , "support" ) );

			Persist.serverSettings = new HTTPServerSettings ( Folder.getEntry ( Persist.MEMORY , HTTPServerSettings.TAG_NAME ) );

			Persist.SIGNATURE = new Signature ( Folder.getEntry ( Persist.MEMORY , "sig" ) );

			Persist.discordSettings = new DiscordSettings ( Folder.getEntry ( Persist.MEMORY , "discord" ) );

			Persist.LOGGER.info ( "Memory file loaded" );
		} catch ( final Exception e ) {
			//e.printStackTrace();
		}

	}

	@Subscribe
	public static void onMemoryAltered ( final MemoryAlteredEvent ev ) {
		Persist.save ( );
	}

	private static void save ( ) {
		Persist.MEMORY = Folder.getNew ( "root" );
		Persist.MEMORY.value.add ( Persist.products.write ( ) );
		Persist.MEMORY.value.add ( Persist.servers.save ( ) );
		Persist.MEMORY.value.add ( SupportReps.save ( ) );
		Persist.MEMORY.value.add ( Persist.serverSettings.save ( ) );
		Persist.MEMORY.value.add ( Persist.SIGNATURE.save ( ) );
		Persist.MEMORY.value.add ( Persist.discordSettings.save ( ) );


		Persist.LOGGER.info ( "Memory file saved" );

		AriaIO.write ( Persist.FILE_NAME , Persist.MEMORY );
	}
}
