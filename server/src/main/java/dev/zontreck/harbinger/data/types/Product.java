package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Product {
	public static AtomicLong SEQUENCE = new AtomicLong ( 0 );
	private static long Signature1;
	private static long Signature2;
	private static boolean init_done;
	public String productName;
	public Version versionNumber;
	public String productItem;
	public Server containingServer;
	public UUID productID;

	public static void loadSigs ( ) {
		if ( Product.init_done ) return;

		Product.Signature1 = Persist.SIGNATURE.v1;
		Product.Signature2 = Persist.SIGNATURE.v2;

		Product.init_done = true;
	}

	@Subscribe
	public static void onMemoryAltered ( final MemoryAlteredEvent ev ) {
		Product.init_done = false;
		Product.loadSigs ( );
	}

	/**
	 * Generates a product ID when fed two pieces of unchanging data.
	 *
	 * @param ID1 The product group ID.
	 * @param ID2 The product identity or number
	 * @return UUID with signature bit transforms applied.
	 */
	public static UUID makeProductID ( long ID1 , long ID2 ) {
		ID1 += Product.Signature1;
		ID2 -= Product.Signature2;

		return new UUID ( ID1 , ID2 );
	}

	public Product ( OSD entry ) {
		if ( entry instanceof OSDMap map ) {
			productName = map.get ( "product" ).AsString ( );
			versionNumber = new Version ( map.get ( "version" ) );
			productItem = map.get ( "item" ).AsString ( );
			productID = OSDID.loadUUID ( map.get ( "id" ) );
		}
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( "product" , OSD.FromString ( productName ) );
		map.put ( "version" , versionNumber.save ( ) );
		map.put ( "item" , OSD.FromString ( productItem ) );
		map.put ( "id" , OSDID.saveUUID ( productID ) );


		return map;
	}

}
