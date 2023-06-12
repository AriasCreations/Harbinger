package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Product {
	public static AtomicLong SEQUENCE = new AtomicLong(0);
	public String productName;
	public Version versionNumber;
	public String productItem;
	public Server containingServer;

	public UUID productID;

	private static long Signature1;
	private static long Signature2;

	private static boolean init_done = false;

	public static void loadSigs() {
		if (init_done) return;

		Signature1 = Persist.SIGNATURE.v1;
		Signature2 = Persist.SIGNATURE.v2;

		init_done = true;
	}

	@Subscribe
	public static void onMemoryAltered(MemoryAlteredEvent ev) {
		init_done = false;
		loadSigs();
	}

	/**
	 * Generates a product ID when fed two pieces of unchanging data.
	 *
	 * @param ID1 The product group ID.
	 * @param ID2 The product identity or number
	 * @return UUID with signature bit transforms applied.
	 */
	public static UUID makeProductID(long ID1, long ID2) {
		ID1 += Signature1;
		ID2 -= Signature2;

		return new UUID(ID1, ID2);
	}

	public Entry<List<Entry>> save() {
		Entry<List<Entry>> tag = Folder.getNew(productName);

		tag.value.add(EntryUtils.mkStr("product", productName));
		tag.value.add(versionNumber.save());
		tag.value.add(EntryUtils.mkStr("item", productItem));
		tag.value.add(EntryUtils.mkUUID("id", productID));

		return tag;
	}

	public static Product deserialize(Entry<List<Entry>> tag) {
		try {

			Product p = new Product();
			p.productName = EntryUtils.getStr(Folder.getEntry(tag, "product"));
			p.versionNumber = new Version((Entry<int[]>) Folder.getEntry(tag, "ver"));
			p.productItem = EntryUtils.getStr(Folder.getEntry(tag, "item"));
			p.productID = EntryUtils.getUUID(Folder.getEntry(tag, "id"));


			return p;
		} catch (Exception e) {
			return null;
		}
	}

}
