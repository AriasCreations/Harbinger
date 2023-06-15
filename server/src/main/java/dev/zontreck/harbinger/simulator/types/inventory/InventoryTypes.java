package dev.zontreck.harbinger.simulator.types.inventory;

public enum InventoryTypes {
	/// <summary>Unknown</summary>
	Unknown ( - 1 ),
	/// <summary>Texture</summary>
	Texture ( 0 ),
	/// <summary>Sound</summary>
	Sound ( 1 ),
	/// <summary>Calling Card</summary>
	CallingCard ( 2 ),
	/// <summary>Landmark</summary>
	Landmark ( 3 ),
	/*
	/// <summary>Script</summary>
	//[Obsolete("See LSL")] Script = 4,
	/// <summary>Clothing</summary>
	//[Obsolete("See Wearable")] Clothing = 5,
	/// <summary>Object, both single and coalesced</summary>
	 */
	Object ( 6 ),
	/// <summary>Notecard</summary>
	Notecard ( 7 ),
	/// <summary></summary>
	Category ( 8 ),
	/// <summary>Folder</summary>
	Folder ( 8 ),
	/// <summary></summary>
	RootCategory ( 9 ),
	/// <summary>an LSL Script</summary>
	LSL ( 10 ),
	/*
	/// <summary></summary>
	//[Obsolete("See LSL")] LSLBytecode = 11,
	/// <summary></summary>
	//[Obsolete("See Texture")] TextureTGA = 12,
	/// <summary></summary>
	//[Obsolete] Bodypart = 13,
	/// <summary></summary>
	//[Obsolete] Trash = 14,
	 */
	/// <summary></summary>
	Snapshot ( 15 ),
	/*
	/// <summary></summary>
	//[Obsolete] LostAndFound = 16,
	 */
	/// <summary></summary>
	Attachment ( 17 ),
	/// <summary></summary>
	Wearable ( 18 ),
	/// <summary></summary>
	Animation ( 19 ),
	/// <summary></summary>
	Gesture ( 20 ),

	/// <summary></summary>
	Mesh ( 22 ),

	Settings ( 25 ),

	Material ( 26 );


	private byte _Type;

	InventoryTypes ( int id ) {
		_Type = ( byte ) id;
	}

	public byte GetType ( ) {
		return _Type;
	}
}
