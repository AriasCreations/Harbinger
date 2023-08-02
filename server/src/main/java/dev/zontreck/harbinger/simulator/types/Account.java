package dev.zontreck.harbinger.simulator.types;

import com.mongodb.client.MongoCollection;
import dev.zontreck.ariaslib.json.DynSerial;
import dev.zontreck.ariaslib.json.DynamicDeserializer;
import dev.zontreck.ariaslib.json.DynamicSerializer;
import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.utils.DataUtils;
import dev.zontreck.harbinger.utils.DigestUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.UUID;

public class Account {

	@BsonIgnore
	public static final String TAG = "accounts";

	public String First;
	public String Last;
	public int UserLevel;
	public String PasswordHash;
	public String PasswordSalt;
	public String UserID;
	public String UserTitle = "Resident";

	public boolean PendingStipend = false;

	public boolean HasAgreedToTermsOfService = false;

	public long LastReadTOS = 0;

	public boolean HasReadCriticalInfo = false;

	/**
	 * MD5 hash of last critical info read.
	 */
	public String LastReadCritical;
	public Location LastLocation = new Location ( );
	public Location HomeLocation = new Location ( );

	public Account ( ) {

	}

	public Account ( String First , String Last , String Pwd ) {
		this.First = First;
		this.Last = Last;
		UserID = UUID.randomUUID ( ).toString ( );
		UserLevel = 0;
		/*
		 * Level Information
		 * 0 - Brand new User
		 * 1 - Agreed to TOS
		 * 2 - Read Critical information
		 * 3 - Resident
		 * 4 - Resident + Estate God
		 * 50 - Premium
		 * 100 - Premium Plus
		 * 150 - Support Staff
		 * 200 - God
		 * 	- At 150 and above, Unlimited Groups is activated.
		 */

		// Generate a salt
		this.PasswordSalt = DigestUtils.md5hex ( DataUtils.GenerateGarbage ( 255 ) );
		this.PasswordHash = DigestUtils.md5hex ( ( Pwd + ":" + DigestUtils.md5hex ( PasswordSalt.getBytes ( ) ) ).getBytes ( ) );

	}

	public boolean ValidatePassword ( String pass ) {
		String hash = DigestUtils.md5hex ( ( pass + ":" + DigestUtils.md5hex ( PasswordSalt.getBytes ( ) ) ).getBytes ( ) );
		if ( PasswordHash.equals ( hash ) )
			return true;
		else return false;
	}


	public void commit ( ) {

		DBSession session = MongoDriver.makeSession();
		MongoCollection<Account> accounts = session.getTableFor(TAG, new GenericClass<>(Account.class));

		var filter = new BsonDocument();
		filter.put("UserID", new BsonString(UserID));
		if(accounts.countDocuments(filter) == 1)
		{
			accounts.findOneAndReplace(filter, this);
		} else {
			accounts.insertOne(this);
		}

		MongoDriver.closeSession(session);
	}


	public static Account getAccount(String First, String Last)
	{
		DBSession session = MongoDriver.makeSession();
		MongoCollection<Account> accounts = session.getTableFor(TAG, getGenericClass());

		BsonDocument query = new BsonDocument();
		query.put("First", new BsonString(First));
		query.put("Last", new BsonString(Last));

		Account result;
		if(accounts.countDocuments(query)>0)
		{
			result = accounts.find(query).first();
		}else {
			result = null;
		}

		MongoDriver.closeSession(session);

		return result;
	}

	public static Account getAccount(String ID)
	{
		DBSession session = MongoDriver.makeSession();
		MongoCollection<Account> accounts = session.getTableFor(TAG, getGenericClass());

		BsonDocument query = new BsonDocument();
		query.put("UserID", new BsonString(ID));

		Account result;
		if(accounts.countDocuments(query)>0)
		{
			result = accounts.find(query).first();
		}else {
			result = null;
		}

		MongoDriver.closeSession(session);

		return result;
	}

	public static GenericClass<Account> getGenericClass()
	{
		return new GenericClass<>(Account.class);
	}


}
