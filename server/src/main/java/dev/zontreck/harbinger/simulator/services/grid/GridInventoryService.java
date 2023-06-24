package dev.zontreck.harbinger.simulator.services.grid;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.events.GridFeatureQueryEvent;
import dev.zontreck.harbinger.simulator.services.ServiceRegistry;
import dev.zontreck.harbinger.simulator.types.Account;
import dev.zontreck.harbinger.simulator.types.inventory.InventoryFolder;
import dev.zontreck.harbinger.simulator.types.inventory.InventoryFolderTypes;
import dev.zontreck.harbinger.utils.DataUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


		Path pAccounts = HarbingerServer.BASE_PATH.resolve ( "accounts" );
		Path pData = pAccounts.resolve ( "data" );
		Path pLibrarian = pAccounts.resolve ( "Librarian.Reaper.txt" );

		Account aLibrarian;

		if ( pLibrarian.toFile ( ).exists ( ) ) {
			String LibrarianID = DataUtils.StripNewLines ( DataUtils.ReadTextFile ( pLibrarian.toFile ( ) ) );
			pLibrarian = pData.resolve ( LibrarianID + ".json" );

			aLibrarian = Account.readFrom ( pLibrarian );


		}
		else {
			aLibrarian = new Account ( "Librarian" , "Reaper" , "password-change-me" );
			aLibrarian.PasswordHash = "";
			aLibrarian.UserLevel = 250;
			aLibrarian.UserTitle = "Librarian of the Reapers";


			aLibrarian.commit ( );

			ServiceRegistry.LOGGER.info ( "\n/!\\ WARNING /!\\ \n\n** YOU NEED TO CHANGE THE 'Librarian Reaper' PASSWORD BEFORE YOU CAN USE THE ACCOUNT **\n\n" );
		}

		if ( ev.options.contains ( "inventory-lib-owner" ) ) {
			ev.setCancelled ( true );

			List<Map<String, String>> LibrarianOwner = new ArrayList<> ( );
			Map<String, String> tmp = new HashMap<> ( );
			tmp.put ( "agent_id" , aLibrarian.UserID );
			LibrarianOwner.add ( tmp );


			ev.reply.put ( "inventory-lib-owner" , LibrarianOwner );


		}

		Path pInventory = pData.resolve ( "inventory" );
		if ( ! pInventory.toFile ( ).exists ( ) )
			pInventory.toFile ( ).mkdir ( );

		Path pUserInventory = pInventory.resolve ( aLibrarian.UserID + ".json" );
		InventoryFolder root;
		if ( pUserInventory.toFile ( ).exists ( ) ) {
			root = InventoryFolder.loadFrom ( pUserInventory );

		}
		else {
			root = new InventoryFolder ( aLibrarian.UserID );
			root.folderName = "Library";
			root.originalPath = pUserInventory;

			GenerateRequiredSystemFolders ( root , aLibrarian );

		}
		if ( root.needsReSave ) {
			root.commitFolders ();

		}

		if ( ev.options.contains ( "inventory-skel-lib" ) ) {

			List<Map<String, Object>> folders = new ArrayList<> ( );

			root.serializeOutToFolders ( folders );

			ev.reply.put ( "inventory-skel-lib" , folders );
		}

		if ( ev.options.contains ( "inventory-lib-root" ) ) {

			List<Map<String, Object>> X = new ArrayList<> ( );
			Map<String, Object> V = new HashMap<> ( );
			V.put ( "folder_id" , root.folderID );

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

	private static void GenerateRequiredSystemFolders ( InventoryFolder root , Account user ) {

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
