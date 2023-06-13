package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.List;
import java.util.Random;

/**
 * Contains the signature parameter data for the Persist datastore
 * <p>
 * This is utilized by the encryption system, and by the ID manipulation in Product.
 */
public class Signature {

	public static final String TAG_NAME = "sig";


	public long v1;
	public long v2;

	public Signature ( ) {
		this.v1 = 0;
		this.v2 = 0;
	}

	public Signature ( final Entry<List<Entry>> entry ) {
		this.v1 = EntryUtils.getLong ( Folder.getEntry ( entry , "a" ) );
		this.v2 = EntryUtils.getLong ( Folder.getEntry ( entry , "b" ) );
	}

	public static Signature makeNew ( ) {
		Random rng = new Random ( );
		rng = new Random ( rng.nextLong ( ) );
		final Signature sig = new Signature ( );
		sig.v1 = rng.nextLong ( );
		sig.v2 = rng.nextLong ( );

		return sig;
	}

	public Entry<?> save ( ) {
		final Entry<List<Entry>> e = Folder.getNew ( Signature.TAG_NAME );
		e.value.add ( EntryUtils.mkLong ( "a" , this.v1 ) );
		e.value.add ( EntryUtils.mkLong ( "b" , this.v2 ) );

		return e;

	}
}
