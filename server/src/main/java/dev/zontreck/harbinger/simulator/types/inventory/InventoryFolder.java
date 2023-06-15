package dev.zontreck.harbinger.simulator.types.inventory;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
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


	public List<InventoryFolder> subFolders;


	@ElementArray (required = false)
	public InventoryFolder[] ChildFolders;

	@Element (required = false)
	public String folderID;

	@Element
	public int folderRevision=1;


	@Persist
	public void persist ( ) {
		invFolderType = folderType.name ( );
		ChildFolders = subFolders.toArray (new InventoryFolder[]{});
	}

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


	@Commit
	public void finalize ( ) {
		folderType = InventoryFolderTypes.valueOf ( invFolderType );
		subFolders = List.of ( ChildFolders );
	}


	@Complete
	public void completed ( ) {
		invFolderType = "";
		ChildFolders = null;

		for (
				InventoryFolder folder :
				subFolders
		) {
			folder.parentFolder = this;
		}
	}


	public static InventoryFolder loadFrom ( Path path ) throws Exception {
		Serializer serial = new Persister ( );
		return serial.read ( InventoryFolder.class , path.toFile ( ) , false );
	}

	public void saveTo(Path path) throws Exception {
		Serializer serial = new Persister (  );
		serial.write ( this, path.toFile () );
	}

	/**
	 * Serializes the folder, and all subfolders as Map objects onto the list
	 */
	public void serializeOutToFolders ( List<Map<String, Object>> folders ) {
		Map<String,Object> self = new HashMap<> (  );
		self.put("name", folderName);
		self.put("folder_id", folderID);
		self.put("type_default", folderType.GetType ());
		self.put("version", folderRevision);
		self.put("parent_id", (parentFolder == null) ? new UUID(0,0).toString () : parentFolder.folderID);

		for (
				InventoryFolder folder :
				subFolders
		) {
			folder.serializeOutToFolders ( folders );
		}
	}
}
