package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.bson.BsonDocument;

import java.util.Random;

/**
 * Contains the signature parameter data for the Persist datastore
 * <p>
 * This is utilized by the encryption system, and by the ID manipulation in Product.
 */
public class Signature {

	public static final String TAG = "sig";


	public long v1=0;
	public long v2=0;

	public Signature ( ) {
		this.v1 = 0;
		this.v2 = 0;

		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(Signature.class));

		if(table.countDocuments()>0)
		{
			Signature sig = table.find().first();
			v1 = sig.v1;
			v2 = sig.v2;
		}

		MongoDriver.closeSession(sess);
	}

	public static Signature makeNew ( ) {
		Random rng = new Random ( );
		rng = new Random ( rng.nextLong ( ) );
		final Signature sig = new Signature ( );
		sig.v1 = rng.nextLong ( );
		sig.v2 = rng.nextLong ( );

		sig.commit();

		return sig;
	}

	public void commit()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(Signature.class));
		table.replaceOne(new BsonDocument(), this);

		MongoDriver.closeSession(sess);
	}

}
