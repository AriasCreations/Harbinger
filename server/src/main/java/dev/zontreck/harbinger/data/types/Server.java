package dev.zontreck.harbinger.data.types;

import com.mongodb.client.MongoCollection;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
	public static final String TAG = "servers";

	public String serverNick;
	public String serverURL;

	public Server ( ) {

	}

	public Server ( String nick , String url ) {
		this.serverNick = nick;
		this.serverURL = url;
	}

	public Server (String nick)
	{
		this(nick, "");
	}

	public static List<Server> loadServers()
	{
		DBSession sess = MongoDriver.makeSession();
		MongoCollection<Server> table = sess.getTableFor(TAG, new GenericClass<>(Server.class));
		List<Server> ret = new ArrayList<>();
		for (Server server :
				table.find()) {
			ret.add(server);
		}

		MongoDriver.closeSession(sess);

		return ret;
	}


	public void commit()
	{
		DBSession sess = MongoDriver.makeSession();
		MongoCollection<Server> servers = sess.getTableFor(TAG, new GenericClass<>(Server.class));

		BsonDocument filter = new BsonDocument();
		filter.put("serverNick", new BsonString(serverNick));
		servers.replaceOne(filter, this);

		MongoDriver.closeSession(sess);
	}



	public static void registerServerHandler ( ) {
		final Task watchdog = new Task ( "server_check_watchdog" , true ) {
			@Override
			public void run ( ) {

			}
		};

		DelayedExecutorService.scheduleRepeatingTask ( watchdog , 60 );
	}

	public static Server getOrCreate(String nick)
	{
		if(Persist.servers.stream().anyMatch(x->x.serverNick.equals(nick)))
		{
			return Persist.servers.stream().filter(x->x.serverNick.equals(nick)).collect(Collectors.toList()).get(0);
		}else return new Server(nick);
	}
}
