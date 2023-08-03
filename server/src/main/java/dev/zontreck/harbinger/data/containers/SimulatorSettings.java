package dev.zontreck.harbinger.data.containers;

import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.DBSettings;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.bson.BsonDocument;

import java.time.Instant;

public class SimulatorSettings {
	public static final String TAG = "simulator";
	public String BASE_URL = "http://localhost:7768";
	public String GRID_NAME = "Dark Space";
	public String GRID_NICK = "space";

	public Boolean GRID_ON = false;
	public Boolean SIM_ON = false;


	public Instant LAST_TOS_UPDATE;


	public void commit()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(SimulatorSettings.class));

		table.replaceOne(new BsonDocument(), this);

		MongoDriver.closeSession(sess);
	}

	public SimulatorSettings ( ) {

	}

	public static SimulatorSettings loadSettings()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(SimulatorSettings.class));

		SimulatorSettings ret = table.find().first();
		MongoDriver.closeSession(sess);

		return ret;
	}
}
