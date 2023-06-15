package dev.zontreck.harbinger.simulator.types.inventory;

public enum InventoryFolderTypes {
	/// <summary>None folder type</summary>
	None (-1),
	/// <summary>Texture folder type</summary>
	Texture (0),
	/// <summary>Sound folder type</summary>
	Sound (1),
	/// <summary>Calling card folder type</summary>
	CallingCard (2),
	/// <summary>Landmark folder type</summary>
	Landmark (3),
	/// <summary>Clothing folder type</summary>
	Clothing (5),
	/// <summary>Object folder type</summary>
	Object (6),
	/// <summary>Notecard folder type</summary>
	Notecard (7),
	/// <summary>The root folder type</summary>
	Root (8),
	/// <summary>LSLText folder</summary>
	LSLText (10),
	/// <summary>Bodyparts folder</summary>
	BodyPart (13),
	/// <summary>Trash folder</summary>
	Trash (14),
	/// <summary>Snapshot folder</summary>
	Snapshot (15),
	/// <summary>Lost And Found folder</summary>
	LostAndFound (16),
	/// <summary>Animation folder</summary>
	Animation (20),
	/// <summary>Gesture folder</summary>
	Gesture (21),
	/// <summary>Favorites folder</summary>
	Favorites (23),
	/// <summary>Ensemble beginning range</summary>
	//EnsembleStart (26),
	/// <summary>Ensemble ending range</summary>
	//EnsembleEnd (45),
	/// <summary>Current outfit folder</summary>
	CurrentOutfit (46),
	/// <summary>Outfit folder</summary>
	Outfit (47),
	/// <summary>My outfits folder</summary>
	MyOutfits (48),
	/// <summary>Mesh folder</summary>
	Mesh (49),
	/// <summary>Marketplace direct delivery inbox ("Received Items")</summary>
	Inbox (50),
	/// <summary>Marketplace direct delivery outbox</summary>
	Outbox (51),
	/// <summary>Basic root folder</summary>
	BasicRoot (52),
	/// <summary>Marketplace listings folder</summary>
	MarketplaceListings (53),
	/// <summary>Marketplace stock folder</summary>
	MarkplaceStock (54),
	/// <summary>Settings folder</summary>
	Settings (56),
	/// <summary>Render materials folder</summary>
	Material (57),
	/// <summary>Hypergrid Suitcase folder</summary>
	Suitcase (100);


	private byte _Type;

	InventoryFolderTypes(int id)
	{
		_Type = (byte)id;
	}

	public byte GetType()
	{
		return _Type;
	}
}
