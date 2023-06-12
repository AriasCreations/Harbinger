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

	public Signature() {
		v1 = 0;
		v2 = 0;
	}

	public static Signature makeNew() {
		Random rng = new Random();
		rng = new Random(rng.nextLong());
		Signature sig = new Signature();
		sig.v1 = rng.nextLong();
		sig.v2 = rng.nextLong();

		return sig;
	}

	public Signature(Entry<List<Entry>> entry) {
		v1 = EntryUtils.getLong(Folder.getEntry(entry, "a"));
		v2 = EntryUtils.getLong(Folder.getEntry(entry, "b"));
	}

	public Entry<?> save() {
		Entry<List<Entry>> e = Folder.getNew(TAG_NAME);
		e.value.add(EntryUtils.mkLong("a", v1));
		e.value.add(EntryUtils.mkLong("b", v2));

		return e;

	}
}
