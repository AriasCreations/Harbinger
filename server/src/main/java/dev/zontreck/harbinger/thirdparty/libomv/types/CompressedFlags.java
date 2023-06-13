package dev.zontreck.harbinger.thirdparty.libomv.types;


public enum CompressedFlags {
	;
	public static final short None = 0x00;
	// Unknown
	public static final short ScratchPad = 0x01;
	// Whether the object has a TreeSpecies
	public static final short Tree = 0x02;
	// Whether the object has floating text ala llSetText
	public static final short HasText = 0x04;
	// Whether the object has an active particle system
	public static final short HasParticles = 0x08;
	// Whether the object has sound attached to it
	public static final short HasSound = 0x10;
	// Whether the object is attached to a root object or not
	public static final short HasParent = 0x20;
	// Whether the object has texture animation settings
	public static final short TextureAnimation = 0x40;
	// Whether the object has an angular velocity
	public static final short HasAngularVelocity = 0x80;
	// Whether the object has a name value pairs string
	public static final short HasNameValues = 0x100;
	// Whether the object has a Media URL set
	public static final short MediaURL = 0x200;
	private static final short _mask = 0x3FF;

	public static short setValue ( final short value ) {
		return ( short ) ( value & CompressedFlags._mask );
	}

	public static short getValue ( final int value ) {
		return ( short ) ( value & CompressedFlags._mask );
	}
}
