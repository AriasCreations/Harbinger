package dev.zontreck.harbinger.simulator.services.grid;

import com.mongodb.client.MongoCollection;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.events.GridFeatureQueryEvent;
import dev.zontreck.harbinger.simulator.services.ServiceRegistry;
import dev.zontreck.harbinger.simulator.types.Account;
import dev.zontreck.harbinger.simulator.types.inventory.InventoryFolder;
import dev.zontreck.harbinger.simulator.types.inventory.InventoryFolderTypes;
import dev.zontreck.harbinger.utils.DataUtils;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.conversions.Bson;

import java.nio.file.Path;
import java.util.*;

/**
 * Provides Services to Optional Queries
 * <p>
 * inventory-lib-owner
 * inventory-lib-root
 * inventory-skel-lib
 * inventory-root
 * inventory-skeleton
 */
public class GridInventoryService {
	private static List<String> VALID_OPTIONS = new ArrayList<> ( );

	static {
		VALID_OPTIONS.add ( "inventory-skeleton" );
		VALID_OPTIONS.add ( "inventory-skel-lib" );
		VALID_OPTIONS.add ( "inventory-lib-root" );
		VALID_OPTIONS.add ( "inventory-lib-owner" );
		VALID_OPTIONS.add ( "inventory-root" );
	}

	@Subscribe
	public static void onGridFeatureSeek ( GridFeatureQueryEvent ev ) throws Exception {

		Account aLibrarian;

		DBSession session = MongoDriver.makeSession();
		MongoCollection<Account> accounts = session.getTableFor(Account.TAG, new GenericClass<>(Account.class));

		var librarian = new BsonDocument();
		librarian.put("First", new BsonString("Librarian"));
		librarian.put("Last", new BsonString("Reaper"));
		boolean existed = false;

		aLibrarian = Account.getAccount("Librarian", "Reaper");
		if(aLibrarian == null)
		{

			aLibrarian = new Account("Librarian", "Reaper", "password-change-me");
			aLibrarian.PasswordHash = "";
			aLibrarian.UserLevel = 250;
			aLibrarian.UserTitle = "Librarian of the Reapers";
			aLibrarian.commit();

			existed=false;

			ServiceRegistry.LOGGER.info ( "\n/!\\ WARNING /!\\ \n\n** YOU NEED TO CHANGE THE 'Librarian Reaper' PASSWORD BEFORE YOU CAN USE THE ACCOUNT **\n\n" );
		} else existed=true;



		if ( ev.options.contains ( "inventory-lib-owner" ) ) {
			ev.setCancelled ( true );

			List<Map<String, String>> LibrarianOwner = new ArrayList<> ( );
			Map<String, String> tmp = new HashMap<> ( );
			tmp.put ( "agent_id" , aLibrarian.UserID );
			LibrarianOwner.add ( tmp );


			ev.reply.put ( "inventory-lib-owner" , LibrarianOwner );


		}


		List<InventoryFolder> libraryFolders = InventoryFolder.retrieveFolders(UUID.fromString(aLibrarian.UserID));

		InventoryFolder libraryRoot = new InventoryFolder();
		if(libraryFolders.size() == 0) {
			libraryRoot.folderName = "Library";
			libraryRoot.folderType = InventoryFolderTypes.Root;
			libraryRoot.folderOwner = aLibrarian.UserID;

			GenerateRequiredSystemFolders(libraryRoot, aLibrarian, libraryFolders);

			libraryRoot.commit();
			libraryFolders.add(libraryRoot);

		}else {
			libraryRoot = InventoryFolder.GetRootFolder(aLibrarian.UserID);
		}


		if ( ev.options.contains ( "inventory-skel-lib" ) ) {

			List<Map<String, Object>> folders = new ArrayList<> ( );

			for (InventoryFolder subfolder :
					libraryFolders) {
				Map<String,Object> entry = new HashMap<>();
				entry.put ( "name" , subfolder.folderName );
				entry.put ( "folder_id" , subfolder.folderID );
				entry.put ( "type_default" , subfolder.folderType.GetType ( ) );
				entry.put ( "version" , subfolder.folderRevision );
				entry.put ( "parent_id" , subfolder.parentFolderID );

				folders.add(entry);
			}

			ev.reply.put ( "inventory-skel-lib" , folders );
		}

		if ( ev.options.contains ( "inventory-lib-root" ) ) {

			List<Map<String, Object>> X = new ArrayList<> ( );
			Map<String, Object> V = new HashMap<> ( );
			V.put ( "folder_id" , libraryRoot.folderID );

			X.add ( V );
			ev.reply.put ( "inventory-lib-root" , X );
		}

		Account user = ev.userAccount;
		pUserInventory = pInventory.resolve ( user.UserID + ".json" );

		if ( pUserInventory.toFile ( ).exists ( ) ) {
			root = InventoryFolder.loadFrom ( pUserInventory );

		}
		else {
			root = new InventoryFolder ( user.UserID );
			root.folderName = "Inventory";
			root.originalPath = pUserInventory;

			GenerateRequiredSystemFolders ( root , user );

		}

		if ( root.needsReSave ) {
			root.commitFolders ();
		}

		if ( ev.options.contains ( "inventory-skeleton" ) ) {

			List<Map<String, Object>> folders = new ArrayList<> ( );

			root.serializeOutToFolders ( folders );

			ev.reply.put ( "inventory-skeleton" , folders );
		}

		if ( ev.options.contains ( "inventory-root" ) ) {


			List<Map<String, Object>> fold = new ArrayList<> ( );
			Map<String, Object> tmp = new HashMap<> ( );
			tmp.put ( "folder_id" , root.folderID );
			fold.add ( tmp );

			ev.reply.put ( "inventory-root" , fold );
		}

	}

	private static void GenerateRequiredSystemFolders ( InventoryFolder root , Account user, List<InventoryFolder> folders ) {

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Texture , "Textures" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Sound , "Sounds" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.CallingCard , "Calling Cards" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Landmark , "Landmarks" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Clothing , "Clothing" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Object , "Objects" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Notecard , "Notecards" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.LSLText , "Scripts" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.BodyPart , "Body Parts" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Trash , "Trash" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Snapshot , "Photo Album" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.LostAndFound , "Lost And Found" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Animation , "Animations" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Gesture , "Gestures" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.CurrentOutfit , "Current Outfit" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Outfit , "Outfit" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.MyOutfits , "My Outfits" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Mesh , "Mesh" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Outbox , "Outbox" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.MarketplaceListings , "Marketplace Listings" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.MarkplaceStock , "Marketplace Stock" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Settings , "Settings" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Material , "Materials" , user.UserID ) );

		root.AddFolder ( new InventoryFolder ( root , InventoryFolderTypes.Suitcase , "Suitcase" , user.UserID ) );


		root.needsReSave = true;
		root.AssertAllChildren ();
	}
}
