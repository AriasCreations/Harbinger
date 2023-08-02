package dev.zontreck.harbinger.data.types;

import com.mongodb.client.MongoCollection;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.events.MemoryAlteredEvent;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Product {
	public static final String TAG = "products";
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


	public static List<Product> loadProducts()
	{
		DBSession sess = MongoDriver.makeSession();
		MongoCollection<Product> prods = sess.getTableFor(TAG, getGenericClass());

		List<Product> products = new ArrayList<>();
		for (Product prod :
				prods.find()) {
			products.add(prod);
		}
		MongoDriver.closeSession(sess);

		return products;
	}

	public static GenericClass<Product> getGenericClass()
	{
		return new GenericClass<>(Product.class);
	}

	/**
	 * Commits the current product to the database
	 */
	public void commit()
	{
		DBSession sess = MongoDriver.makeSession();
		MongoCollection<Product> prods = sess.getTableFor(TAG, getGenericClass());

		BsonDocument filter = new BsonDocument();
		filter.put("productID", new BsonString(productID.toString()));
		prods.replaceOne(filter, this);

		MongoDriver.closeSession(sess);
	}

	public static boolean exists(UUID ID)
	{
		return Persist.products.stream().anyMatch(x->x.productID.equals(ID));
	}


	public void delete()
	{
		DBSession sess = MongoDriver.makeSession();
		MongoCollection<Product> table = sess.getTableFor(TAG, new GenericClass<>(Product.class));

		BsonDocument filter = new BsonDocument();
		filter.put("productID", new BsonString(productID.toString()));
		table.deleteOne(filter);

		MongoDriver.closeSession(sess);
	}

}
