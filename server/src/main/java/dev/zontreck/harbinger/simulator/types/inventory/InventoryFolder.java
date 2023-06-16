package dev.zontreck.harbinger.simulator.types.inventory;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Complete;
import org.simpleframework.xml.core.Persist;
import org.simpleframework.xml.core.Persister;

import java.nio.file.Path;
import java.util.*;

@Root (strict = false)
public class InventoryFolder {
	public InventoryFolderTypes folderType;

	@Element (required = false)
	public String invFolderType;

	@Element (required = false)
	public String folderName;

	public InventoryFolder parentFolder;

	@Element (required = false)
	public String folderOwner;



	@ElementList(required = false)
	public List<InventoryFolder> subFolders;


	@Element (required = false)
	public String folderID;

	@Element
	public int folderRevision = 1;


	@Persist
	public void persist ( ) {
		invFolderType = folderType.name ( );
	}


	public void AddFolder(InventoryFolder folder)
	{
		subFolders.add ( folder );
		bumpFolderVersion();
	}

	public void DeleteFolder(InventoryFolder folder)
	{
		subFolders.remove ( folder );
		bumpFolderVersion ();
	}

	public void bumpFolderVersion()
	{

		folderRevision++;
		ClimbTree ().commitFolders ();
	}

	public Path originalPath;

	public InventoryFolder ( ) {
		folderID = UUID.randomUUID ( ).toString ( );
		folderType = InventoryFolderTypes.None;
		folderName = "New Folder";
		subFolders = new ArrayList<> ( );
	}

	public InventoryFolder ( InventoryFolder parent , InventoryFolderTypes type , String name , String folderOwner ) {
		parentFolder = parent;
		parent.subFolders.add ( this );
		folderType = type;
		folderName = name;
		this.folderOwner = folderOwner;

		subFolders = new ArrayList<> ( );
		folderID = UUID.randomUUID ( ).toString ( );
	}

	/**
	 * Should only be used to initialize a brand-new root inventory folder, as only a root folder can have no parent folder.
	 */
	public InventoryFolder ( String folderOwner ) {
		folderType = InventoryFolderTypes.Root;
		folderName = "Inventory";
		this.folderOwner = folderOwner;

		subFolders = new ArrayList<> ( );
		folderID = UUID.randomUUID ( ).toString ( );
	}


	private void finishLoad ( ) {
		folderType = InventoryFolderTypes.valueOf ( invFolderType );
		InventoryFolder root = ClimbTree();

	}

	public InventoryFolder ClimbTree()
	{
		if(parentFolder != null)return parentFolder.ClimbTree ();

		return this;
	}

	public void AssertAllChildren()
	{
		// Climbs down the tree of folders and asserts all subfolders to have a parent value set.

		if(folderType != InventoryFolderTypes.Root) return;

		for (
				InventoryFolder folder :
				subFolders
		) {
			folder.parentFolder = this;
			folder.AssertAllChildren ();
		}
	}


	@Complete
	public void completed ( ) {
		finishLoad ();
	}

	public boolean needsReSave=false;
	/**
	 * WARNING: This is to be used only after de-serialization to ensure no duplicate folder entries exist
	 */
	public void repairFolderSubStructure()
	{
		if(folderType!=InventoryFolderTypes.Root)return;
		InventoryFolder root = ClimbTree ();
		// Scan root's subfolders
		while(root.scanAndRepair ()){
			root.needsReSave=true;
		}

	}

	private boolean scanAndRepair()
	{
		for(int i=0;i<subFolders.size ();i++)
		{
			if(destroyDuplicates(folderID)){
				return true;
			}
			if(subFolders.get ( i ).scanAndRepair ())
				return true;

		}
		return false;
	}

	private boolean destroyDuplicates(String ID)
	{
		for(int i=0;i<subFolders.size ();i++)
		{
			InventoryFolder sub = subFolders.get ( i );
			if(sub.folderID.equalsIgnoreCase ( ID ))
			{
				// Delete this folder and start this function over again
				subFolders.remove ( i );
				return true;

			}
			subFolders.get ( i ).destroyDuplicates ( ID );
		}

		return false;
	}


	public static InventoryFolder loadFrom ( Path path ) throws Exception {
		Serializer serial = new Persister ( );
		InventoryFolder orig =  serial.read ( InventoryFolder.class , path.toFile ( ) , false );
		orig.originalPath = path;
		return orig;
	}

	public void commitFolders()
	{
		InventoryFolder root = ClimbTree ();
		try {
			root.saveTo ( root.originalPath );
		} catch ( Exception e ) {
			throw new RuntimeException ( e );
		}
	}

	private void saveTo ( Path path ) throws Exception {
		Serializer serial = new Persister ( );
		serial.write ( this , path.toFile ( ) );
		needsReSave=false;
	}

	/**
	 * Serializes the folder, and all subfolders as Map objects onto the list
	 */
	public void serializeOutToFolders ( List<Map<String, Object>> folders ) {
		Map<String, Object> self = new HashMap<> ( );
		self.put ( "name" , folderName );
		self.put ( "folder_id" , folderID );
		self.put ( "type_default" , folderType.GetType ( ) );
		self.put ( "version" , folderRevision );
		self.put ( "parent_id" , ( parentFolder == null ) ? new UUID ( 0 , 0 ).toString ( ) : parentFolder.folderID );

		folders.add ( self );
		for (
				InventoryFolder folder :
				subFolders
		) {
			folder.serializeOutToFolders ( folders );
		}
	}
}
