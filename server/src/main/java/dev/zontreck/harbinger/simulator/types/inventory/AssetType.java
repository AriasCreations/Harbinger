package dev.zontreck.harbinger.simulator.types.inventory;


public enum AssetType {
	/// <summary>Unknown asset type</summary>
	Unknown ( - 1 ),
	/// <summary>Texture asset, stores in JPEG2000 J2C stream format</summary>
	Texture ( 0 ),
	/// <summary>Sound asset</summary>
	Sound ( 1 ),
	/// <summary>Calling card for another avatar</summary>
	CallingCard ( 2 ),
	/// <summary>Link to a location in world</summary>
	Landmark ( 3 ),
	// <summary>Legacy script asset, you should never see one of these</summary>
	//[Obsolete]
	//Script = 4,
	/// <summary>Collection of textures and parameters that can be worn by an avatar</summary>
	Clothing ( 5 ),
	/// <summary>Primitive that can contain textures, sounds,
	/// scripts and more</summary>
	Object ( 6 ),
	/// <summary>Notecard asset</summary>
	Notecard ( 7 ),
	/// <summary>Holds a collection of inventory items. "Category" in the Linden viewer</summary>
	Folder ( 8 ),
	/// <summary>Linden scripting language script</summary>
	LSLText ( 10 ),
	/// <summary>LSO bytecode for a script</summary>
	LSLBytecode ( 11 ),
	/// <summary>Uncompressed TGA texture</summary>
	TextureTGA ( 12 ),
	/// <summary>Collection of textures and shape parameters that can be worn</summary>
	Bodypart ( 13 ),
	/// <summary>Uncompressed sound</summary>
	SoundWAV ( 17 ),
	/// <summary>Uncompressed TGA non-square image, not to be used as a
	/// texture</summary>
	ImageTGA ( 18 ),
	/// <summary>Compressed JPEG non-square image, not to be used as a
	/// texture</summary>
	ImageJPEG ( 19 ),
	/// <summary>Animation</summary>
	Animation ( 20 ),
	/// <summary>Sequence of animations, sounds, chat, and pauses</summary>
	Gesture ( 21 ),
	/// <summary>Simstate file</summary>
	Simstate ( 22 ),
	/// <summary>Asset is a link to another inventory item</summary>
	Link ( 24 ),
	/// <summary>Asset is a link to another inventory folder</summary>
	LinkFolder ( 25 ),
	/// <summary>Marketplace Folder. Same as an Category but different display methods.</summary>
	MarketplaceFolder ( 26 ),
	/// <summary>Linden mesh format</summary>
	Mesh ( 49 ),

	Settings ( 56 ),
	/// <summary>Render material</summary>
	Material ( 57 );

	private byte _Type;

	AssetType ( int id ) {
		_Type = ( byte ) id;
	}

	public byte GetType ( ) {
		return _Type;
	}
}
