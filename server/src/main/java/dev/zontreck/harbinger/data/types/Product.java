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

	private static boolean init_done;

	public static void loadSigs() {
		if (Product.init_done) return;

		Product.Signature1 = Persist.SIGNATURE.v1;
		Product.Signature2 = Persist.SIGNATURE.v2;

		Product.init_done = true;
	}

	@Subscribe
	public static void onMemoryAltered(final MemoryAlteredEvent ev) {
		Product.init_done = false;
		Product.loadSigs();
	}

	/**
	 * Generates a product ID when fed two pieces of unchanging data.
	 *
	 * @param ID1 The product group ID.
	 * @param ID2 The product identity or number
	 * @return UUID with signature bit transforms applied.
	 */
	public static UUID makeProductID(long ID1, long ID2) {
		ID1 += Product.Signature1;
		ID2 -= Product.Signature2;

		return new UUID(ID1, ID2);
	}

	public Entry<List<Entry>> save() {
		final Entry<List<Entry>> tag = Folder.getNew(this.productName);

		tag.value.add(EntryUtils.mkStr("product", this.productName));
		tag.value.add(this.versionNumber.save());
		tag.value.add(EntryUtils.mkStr("item", this.productItem));
		tag.value.add(EntryUtils.mkUUID("id", this.productID));

		return tag;
	}

	public static Product deserialize(final Entry<List<Entry>> tag) {
		try {

			final Product p = new Product();
			p.productName = EntryUtils.getStr(Folder.getEntry(tag, "product"));
			p.versionNumber = new Version((Entry<int[]>) Folder.getEntry(tag, "ver"));
			p.productItem = EntryUtils.getStr(Folder.getEntry(tag, "item"));
			p.productID = EntryUtils.getUUID(Folder.getEntry(tag, "id"));


			return p;
		} catch (final Exception e) {
			return null;
		}
	}

}
