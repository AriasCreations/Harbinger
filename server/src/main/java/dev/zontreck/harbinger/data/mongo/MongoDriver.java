package dev.zontreck.harbinger.data.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.SocketSettings;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.concurrent.TimeUnit;

public class MongoDriver {

	public static boolean can_connect = false;
	public static MongoClientSettings client_settings;

	public static boolean tryConnect ( ) {
		String uri = "mongodb://" + ( DBSettings.instance.USER != "" ? DBSettings.instance.USER + ":" + DBSettings.instance.PASSWORD + "@" : "" ) + DBSettings.instance.HOST + ":" + DBSettings.instance.PORT;

		ServerApi api = ServerApi.builder ( ).version ( ServerApiVersion.V1 ).build ();

		MongoClientSettings settings = MongoClientSettings.builder ( ).applyConnectionString ( new ConnectionString ( uri ) ).serverApi ( api ).applicationName ( "Harbinger" ).applyToSocketSettings ( builder -> builder.connectTimeout ( 10, TimeUnit.SECONDS ).readTimeout ( 10, TimeUnit.SECONDS ) ).applyToConnectionPoolSettings ( builder -> builder.maxWaitTime ( 10, TimeUnit.SECONDS ) ).build ();


		try ( MongoClient client = MongoClients.create ( settings ) ) {
			MongoDatabase database = client.getDatabase ( DBSettings.instance.DATABASE );
			try {

				Bson cmd = new BsonDocument ( "ping" , new BsonInt64 ( 1 ) );
				Document test = database.runCommand ( cmd );
				can_connect = true;
				client_settings = settings;
				return true;
			} catch ( Exception e ) {
				can_connect = false;
				return false;
			}
		}


	}
}
