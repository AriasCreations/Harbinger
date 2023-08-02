package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.UUID;

public class Person {
	public UUID ID;
	public String Name;
	public PermissionLevel Permissions;

	public Person ( final UUID id , final String user , final PermissionLevel lvl ) {
		this.ID = id;
		this.Name = user;
		this.Permissions = lvl;
	}

	public String print ( final int indent ) {
		String ind = "";
		for ( int i = 0 ; i < indent ; i++ ) {
			ind += "\t";
		}
		String s = "";
		s += ind + "{\n" + ind + "\tID: ";
		s += this.ID.toString ( ) + "\n" + ind + "\tName: ";
		s += this.Name + "\n" + ind + "\tPermissions: ";
		s += this.Permissions.toString ( ) + "\n";
		s += ind + "}";

		return s;
	}

	public void commit()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(SupportReps.TAG, new GenericClass<>(Person.class));

		var filter = new BsonDocument();
		filter.put("ID", new BsonString(ID.toString()));

		table.replaceOne(filter, this);

		MongoDriver.closeSession(sess);
	}

	public void delete()
	{
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(SupportReps.TAG, new GenericClass<>(Person.class));

		var filter = new BsonDocument();
		filter.put("ID", new BsonString(ID.toString()));

		table.deleteOne(filter);

		MongoDriver.closeSession(sess);
	}
}
