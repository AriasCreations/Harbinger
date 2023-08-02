package dev.zontreck.harbinger.simulator.types.inventory;


import com.mongodb.client.MongoCollection;
import dev.zontreck.ariaslib.json.*;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.utils.DataUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class InventoryFolder {


	public InventoryFolderTypes folderType;
	public String folderName;

	public String parentFolderID;

	public String folderOwner;


	@BsonProperty("folder_id")
	public String folderID;

	public int folderRevision = 1;


	public void AddFolder ( InventoryFolder folder, boolean bumpDB ) {
		if(folder.folderID.isEmpty ())
		{
			folder.folderID = UUID.randomUUID ().toString ();
		}
		folder.parentFolderID = folderID;

		if(bumpDB)
		{

			bumpFolderVersion ( );

			folder.commit();
			commit();
		}
	}

	public void DeleteFolder ( InventoryFolder folder ) {
		bumpFolderVersion ( );

		BsonDocument query = new BsonDocument();
		query.put("folder_id", new BsonString(folder.folderID));
		query.put("folderOwner", new BsonString(folder.folderOwner));

		bumpFolderVersion();
		DBSession sess = MongoDriver.makeSession();
		sess.getTableFor(TAG, getGenericClass()).deleteOne(query);

		MongoDriver.closeSession(sess);
		commit();
	}

	@BsonIgnore
	public static final String TAG = "inventory_folders";


	public static GenericClass<InventoryFolder> getGenericClass()
	{
		return new GenericClass<>(InventoryFolder.class);
	}

	public void bumpFolderVersion ( ) {

		folderRevision++;
		commit();
	}

	public void commit()
	{
		UUID id = new UUID(0,0);

		// Commit folder
		BsonDocument filter = new BsonDocument();
		filter.put("folder_id", new BsonString(folderID));
		filter.put("folderOwner", new BsonString(folderOwner));

		DBSession sess = MongoDriver.makeSession();
		var tbl = sess.getTableFor(TAG, getGenericClass());
		tbl.findOneAndReplace(filter, this);

		MongoDriver.closeSession(sess);
	}

	public static List<InventoryFolder> retrieveFolders(UUID folderOwner)
	{
		DBSession sess = MongoDriver.makeSession();
		MongoCollection<InventoryFolder> table = sess.getTableFor(TAG, getGenericClass());

		BsonDocument query = new BsonDocument();
		query.put("folderOwner", new BsonString(folderOwner.toString()));

		var lst = table.find(query);
		InventoryFolder ret = new InventoryFolder();

		List<InventoryFolder> items = new ArrayList<>();
		for(InventoryFolder folder : lst)
		{
			items.add(folder);
		}
		MongoDriver.closeSession(sess);


		return items;
	}

	public InventoryFolder ( ) {
		folderID = UUID.randomUUID ( ).toString ( );
		folderType = InventoryFolderTypes.None;
		folderName = "New Folder";
	}

	public InventoryFolder ( InventoryFolder parent , InventoryFolderTypes type , String name , String folderOwner ) {

		folderType = type;
		folderName = name;
		this.folderOwner = folderOwner;

		folderID = UUID.randomUUID ( ).toString ( );
	}

	/**
	 * Should only be used to initialize a brand-new root inventory folder, as only a root folder can have no parent folder.
	 */
	public InventoryFolder ( String folderOwner ) {
		folderType = InventoryFolderTypes.Root;
		folderName = "Inventory";
		this.folderOwner = folderOwner;

		folderID = UUID.randomUUID ( ).toString ( );
	}

	public static InventoryFolder GetRootFolder ( String UserID ) {
		return getParentFolderByID(UserID, new UUID(0,0).toString());
	}

	public static InventoryFolder getFolderByID(String ID)
	{
		DBSession sess = MongoDriver.makeSession();
		var tbl = sess.getTableFor(TAG, getGenericClass());
		BsonDocument query = new BsonDocument();
		query.put("folder_id", new BsonString(new UUID(0,0).toString()));

		if(tbl.countDocuments(query) > 0)
		{
			return tbl.find(query).first();
		} else return null;
	}
	public static InventoryFolder getParentFolderByID(String UserID, String ID)
	{
		DBSession sess = MongoDriver.makeSession();
		var tbl = sess.getTableFor(TAG, getGenericClass());
		BsonDocument query = new BsonDocument();
		query.put("parentFolderID", new BsonString(new UUID(0,0).toString()));
		query.put("folderOwner", new BsonString(UserID));

		if(tbl.countDocuments(query) > 0)
		{
			return tbl.find(query).first();
		} else return null;
	}
}
