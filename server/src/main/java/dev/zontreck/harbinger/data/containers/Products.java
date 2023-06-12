package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.types.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Products {
	public List<Product> products = new ArrayList<>();


	public Entry<List<Entry>> write() {
		Entry<List<Entry>> tag = Folder.getNew("products");
		for (Product prod :
				products) {
			tag.value.add(prod.save());
		}
		tag.value.add(EntryUtils.mkLong("sequence", Product.SEQUENCE.get()));
		return tag;

	}

	public boolean hasProduct(UUID prodID) {
		return products.stream().filter(v -> v.productID.equals(prodID)).collect(Collectors.toList()).size() > 0;
	}

	public Products() {

	}

	public Products(Entry<List<Entry>> tag) {
		try {

			for (Entry<?> E : tag.value) {
				products.add(Product.deserialize((Entry<List<Entry>>) E));
			}
			Product.SEQUENCE = new AtomicLong(EntryUtils.getInt(Folder.getEntry(tag, "sequence")));
		} catch (Exception e) {
			products = new ArrayList<>();
		}
	}
}
