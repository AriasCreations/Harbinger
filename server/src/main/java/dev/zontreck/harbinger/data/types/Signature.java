package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

import java.util.Random;

/**
 * Contains the signature parameter data for the Persist datastore
 * <p>
 * This is utilized by the encryption system, and by the ID manipulation in Product.
 */
public class Signature {

	public static final String TAG = "sig";


	public long v1;
	public long v2;

	public Signature ( ) {
		this.v1 = 0;
		this.v2 = 0;
	}

	public Signature ( final OSD entry ) {
		if ( entry instanceof OSDMap map ) {
			v1 = map.get ( "a" ).AsLong ( );
			v2 = map.get ( "b" ).AsLong ( );
		}
	}

	public static Signature makeNew ( ) {
		Random rng = new Random ( );
		rng = new Random ( rng.nextLong ( ) );
		final Signature sig = new Signature ( );
		sig.v1 = rng.nextLong ( );
		sig.v2 = rng.nextLong ( );

		return sig;
	}

	public OSD save ( ) {
		OSDMap map = new OSDMap ( );
		map.put ( "a" , OSD.FromLong ( v1 ) );
		map.put ( "b" , OSD.FromLong ( v2 ) );
		return map;
	}
}
