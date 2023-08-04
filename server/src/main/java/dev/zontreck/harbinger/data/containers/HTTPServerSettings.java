package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryType;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.data.types.PresharedKey;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.utils.Key;
import org.bson.BsonDocument;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HTTPServerSettings {
	public static final String TAG = "http_server";

	public boolean enabled;
	public int port = 7768;

	public PresharedKey PSK;


	public int ExternalPortNumber; // If zero, the flag below will be set to false on serialize and deserialize.
	public boolean ExternalPortNumberSet = false; // Defaults then to the internal port number from HTTPServer settings

	public UDPSettings udp_settings;



	public HTTPServerSettings ( ) {
		try {
			PSK = new PresharedKey ( "changeme" );
			udp_settings = new UDPSettings ( );
		} catch ( Exception e ) {
			e.printStackTrace ( );
		}
	}

	public static HTTPServerSettings loadSettings()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(HTTPServerSettings.class));

		HTTPServerSettings ret = table.find().first();
		MongoDriver.closeSession(sess);

		return ret;
	}

	public void commit()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(HTTPServerSettings.class));

		table.replaceOne(new BsonDocument(), this);

		MongoDriver.closeSession(sess);
	}
}
