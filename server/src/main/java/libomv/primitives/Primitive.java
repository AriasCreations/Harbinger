/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Copyright (c) 2006, Lateral Arts Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.primitives;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.primitives.TextureEntry.TextureAnimMode;
import libomv.types.Color4;
import libomv.types.NameValue;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.utils.Helpers;

public class Primitive
{
	protected static float CUT_QUANTA = 0.00002f;
	protected static float SCALE_QUANTA = 0.01f;
	protected static float SHEAR_QUANTA = 0.01f;
	protected static float TAPER_QUANTA = 0.01f;
	protected static float REV_QUANTA = 0.015f;
	protected static float HOLLOW_QUANTA = 0.00002f;

	/** Identifier code for primitive types */
	public enum PCode
	{
		/* None */
		None(0),
		/* A Primitive */
		Prim(9),
		/* A Avatar */
		Avatar(47),
		/* Linden grass */
		Grass(95),
		/* Linden tree */
		NewTree(111),
		/* A primitive that acts as the source for a particle stream */
		ParticleSystem(143),
		/* A Linden true */
		Tree(255);

		public static PCode setValue(final int value)
		{
			for (final PCode e : PCode.values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		PCode(final int value)
		{
			this._value = (byte) value;
		}
	}

	// Primary parameters for primitives such as Physics Enabled or Phantom
	// [Flags]
	public enum PrimFlags
	{
		;
		// Deprecated
		public static final int None = 0;
		// Whether physics are enabled for this object
		public static final int Physics = 0x00000001;
		public static final int CreateSelected = 0x00000002;
		public static final int ObjectModify = 0x00000004;
		public static final int ObjectCopy = 0x00000008;
		public static final int ObjectAnyOwner = 0x00000010;
		public static final int ObjectYouOwner = 0x00000020;
		public static final int Scripted = 0x00000040;
		public static final int Touch = 0x00000080;
		public static final int ObjectMove = 0x00000100;
		public static final int Money = 0x00000200;
		public static final int Phantom = 0x00000400;
		public static final int InventoryEmpty = 0x00000800;
		public static final int JointHinge = 0x00001000;
		public static final int JointP2P = 0x00002000;
		public static final int JointLP2P = 0x00004000;
		// Deprecated */
		public static final int JointWheel = 0x00008000;
		public static final int AllowInventoryDrop = 0x00010000;
		public static final int ObjectTransfer = 0x00020000;
		public static final int ObjectGroupOwned = 0x00040000;
		// Deprecated */
		public static final int ObjectYouOfficer = 0x00080000;
		public static final int CameraDecoupled = 0x00100000;
		public static final int AnimSource = 0x00200000;
		public static final int CameraSource = 0x00400000;
		public static final int CastShadows = 0x00800000;
		// Server flag, will not be sent to clients. Specifies that the object
		// is
		// destroyed when it touches a simulator edge
		public static final int DieAtEdge = 0x01000000;
		// Server flag, will not be sent to clients. Specifies that the object
		// will
		// be returned to the owner's inventory when it touches a simulator edge
		public static final int ReturnAtEdge = 0x02000000;
		// Server flag, will not be sent to clients.
		public static final int Sandbox = 0x04000000;
		// Server flag, will not be sent to client. Specifies that the object is
		// hovering/flying
		public static final int Flying = 0x08000000;
		public static final int ObjectOwnerModify = 0x10000000;
		public static final int TemporaryOnRez = 0x20000000;
		public static final int Temporary = 0x40000000;
		public static final int ZlibCompressed = 0x80000000;

		public static int setValue(final int value)
		{
			return value & PrimFlags._mask;
		}

		public static int getValue(final int value)
		{
			return value & PrimFlags._mask;
		}

		private static final int _mask = 0xFFFFFFFF;
	}

	// Sound flags for sounds attached to primitives
	// [Flags]
	public enum SoundFlags
	{
		;
		public static final byte None = 0;
		public static final byte Loop = 0x01;
		public static final byte SyncMaster = 0x02;
		public static final byte SyncSlave = 0x04;
		public static final byte SyncPending = 0x08;
		public static final byte LinkedList = 0x10;
		public static final byte Stop = 0x20;

		public static byte setValue(final int value)
		{
			return (byte) (value & SoundFlags._mask);
		}

		public static byte getValue(final byte value)
		{
			return (byte) (value & SoundFlags._mask);
		}

		private static final byte _mask = (byte)0xFF;
	}

	public enum ProfileCurve
	{
//		Circle, Square, IsoTriangle, EqualTriangle, RightTriangle, HalfCircle;
		Circle, Square, IsometricTriangle, EquilateralTriangle, RightTriangle, HalfCircle;

		public static ProfileCurve setValue(final int value)
		{
			if (0 <= value && value < ProfileCurve.values().length)
				return ProfileCurve.values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	public enum HoleType
	{
		Same(0x00), Circle(0x10), Square(0x20), Triangle(0x30);

		public static HoleType setValue(final int value)
		{
			for (final HoleType e : HoleType.values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		HoleType(final int value)
		{
			this._value = (byte) value;
		}
	}

	public enum PathCurve
	{
		Line(0x10), Circle(0x20), Circle2(0x30), Test(0x40), Flexible(0x80);

		public static PathCurve setValue(final int value)
		{
			for (final PathCurve e : PathCurve.values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		PathCurve(final int value)
		{
			this._value = (byte) value;
		}
	}

	// Material type for a primitive
	public enum Material
	{
		Stone, Metal, Glass, Wood, Flesh, Plastic, Rubber, Light;

		public static Material setValue(final int value)
		{
			if (0 <= value && value < Material.values().length)
				return Material.values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	// Used in a helper function to roughly determine prim shape
	public enum PrimType
	{
		Unknown, Box, Cylinder, Prism, Sphere, Torus, Tube, Ring, Sculpt, Mesh;

		public static PrimType setValue(final int value)
		{
			if (0 <= value && value < PrimType.values().length)
				return PrimType.values()[value];
			return PrimType.Unknown;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	// Extra parameters for primitives, these flags are for features that have been
	// added after the original ObjectFlags that has all eight bits reserved already
	public enum ExtraParamType
	{
		// Whether this object has flexible parameters
		Flexible(0x10),
		// Whether this object has light parameters
		Light(0x20),
		// Whether this object is a sculpted prim
		Sculpt(0x30),
		// Wether this object is a light map
		LightImage(0x40),
		// Whether this object is a mesh
		Mesh(0x60);

		public static ExtraParamType setValue(final int value)
		{
			for (final ExtraParamType e : ExtraParamType.values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		ExtraParamType(final int value)
		{
			this._value = (byte) value;
		}
	}

	public enum JointType
	{
		Invalid, Hinge, Point,
		// [Obsolete]
		// LPoint,
		// [Obsolete]
		// Wheel
		;

		public static JointType setValue(final int value)
		{
			if (0 <= value && value < JointType.values().length)
				return JointType.values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	public enum SculptType
	{
		None(0), Sphere(1), Torus(2), Plane(3), Cylinder(4), Mesh(5), Invert(64), Mirror(128);

		public static SculptType setValue(final int value)
		{
			for (final SculptType e : SculptType.values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		SculptType(final int value)
		{
			this._value = (byte) value;
		}
	}

	public enum ObjectCategory
	{
		Invalid(-1), None(0), Owner(1), Group(2), Other(3), Selected(4), Temporary(5);

		public static ObjectCategory setValue(final int value)
		{
			for (final ObjectCategory e : ObjectCategory.values())
			{
				if (e._value == value)
					return e;
			}
			return null;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		ObjectCategory(final int value)
		{
			this._value = (byte) value;
		}
	}

	/**
	 * Attachment points for objects on avatar bodies
	 * 
	 * Both InventoryObject and InventoryAttachment types can be attached
	 * 
	 */
	public enum AttachmentPoint
	{
		/** Right hand if object was not previously attached */
		Default,
		/** Chest */
		Chest,
		/** Skull */
		Skull,
		/** Left shoulder */
		LeftShoulder,
		/** Right shoulder */
		RightShoulder,
		/** Left hand */
		LeftHand,
		/** Right hand */
		RightHand,
		/** Left foot */
		LeftFoot,
		/** Right foot */
		RightFoot,
		/** Spine */
		Spine,
		/** Pelvis */
		Pelvis,
		/** Mouth */
		Mouth,
		/** Chin */
		Chin,
		/** Left ear */
		LeftEar,
		/** Right ear */
		RightEar,
		/** Left eyeball */
		LeftEyeball,
		/** Right eyeball */
		RightEyeball,
		/** Nose */
		Nose,
		/** Right upper arm */
		RightUpperArm,
		/** Right forearm */
		RightForearm,
		/** Left upper arm */
		LeftUpperArm,
		/** Left forearm */
		LeftForearm,
		/** Right hip */
		RightHip,
		/** Right upper leg */
		RightUpperLeg,
		/** Right lower leg */
		RightLowerLeg,
		/** Left hip */
		LeftHip,
		/** Left upper leg */
		LeftUpperLeg,
		/** Left lower leg */
		LeftLowerLeg,
		/** Stomach */
		Stomach,
		/** Left pectoral */
		LeftPec,
		/** Right pectoral */
		RightPec,
		/** HUD Center position 2 */
		HUDCenter2,
		/** HUD Top-right */
		HUDTopRight,
		/** HUD Top */
		HUDTop,
		/** HUD Top-left */
		HUDTopLeft,
		/** HUD Center */
		HUDCenter,
		/** HUD Bottom-left */
		HUDBottomLeft,
		/** HUD Bottom */
		HUDBottom,
		/** HUD Bottom-right */
		HUDBottomRight,
		/** Neck */
		Neck,
		/** Avatar Center */
		Root,
		/** Left Ring Finger */
		LeftHandRing,
		/** Right Ring Finger */
		RightHandRing,
		/** Tail Base */
		TailBase,
		/** Tail Tip */
		TailTip,
		/** Left Wing */
		LeftWing,
		/** Right Wing */
		RightWing,
		/** Jaw */
		Jaw,
		/** Alt Left Ear */
		AltLeftEar,
		/** Alt Right Ear */
		AltRightEar,
		/** Alt Left Eye */
		AltLeftEye,
		/** Alt Right Eye */
		AltRightEye,
		/** Tongue */
		Tongue,
		/** Groin */
		Groin,
		/** Left Hind Foot */
		LeftHindFoot,
		/** Right Hind Foot */
		RightHindFoot;

		private static final String[] strings = { "Default", "Chest", "Head", "Left Shoulder", "Right Shoulder", "Left Hand",
				"Right Hand", "Left Foot", "Right Foot", "Back", "Pelvis", "Mouth", "Chin", "Left Ear", "Right Ear",
				"Left Eye", "Right Eye", "Nose", "Right Upper Arm", "Right Lower Arm", "Left Upper Arm", "Left Lower Arm",
				"Right Hip", "Right Upper Leg", "Right Lower Leg", "Left Hip", "Left Upper Leg", "Left Lower Leg", "Belly",
				"Left Pec", "Right Pec", "HUD Center 2", "HUD Top Right", "HUD Top Center", "HUD Top Left", "HUD Center 1",
				"HUD Bottom Left", "HUD Bottom", "HUD Bottom Right", "Neck", "Avatar Center", "Left Ring Finger",
				"Right Ring Finger", "Tail Base", "Tail Tip", "Left Wing", "Right Wing", "Jaw", "Alt Left Ear", "Alt Right Ear",
				"Alt Left Eye", "Alt Right Eye", "Tongue", "Groin", "Left Hind Foot", "Right Hind Foot"};

		public static AttachmentPoint setValue(final String value)
		{
			for (int i = 0; i < AttachmentPoint.values().length; i++)
			{
				if (value.equals(AttachmentPoint.strings[i]))
				{
					return AttachmentPoint.values()[i];
				}
			}
			return AttachmentPoint.Default;
		}

		public static AttachmentPoint setValue(final int value)
		{
			if (0 <= value && value < AttachmentPoint.values().length)
				return AttachmentPoint.values()[value];
			return AttachmentPoint.Default;
		}

		public static byte getValue(final AttachmentPoint att, final boolean replace)
		{
			return att.getValue(replace);
		}
		
		public byte getValue(final boolean replace)
		{
			byte value = (byte) this.ordinal();
			if (!replace)
				value |= 0x80;
			return value;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}

		@Override
		public String toString()
		{
			return AttachmentPoint.toString(this);
		}

		public static String toString(final AttachmentPoint point)
		{
			return AttachmentPoint.strings[point.ordinal()];
		}
	}

	/** Tree foliage types */
	public enum Tree
	{
		// Pine1 tree
		Pine1,
		// Oak tree
		Oak,
		// Tropical Bush1
		TropicalBush1,
		// Palm1 tree
		Palm1,
		// Dogwood tree
		Dogwood,
		// Tropical Bush2
		TropicalBush2,
		// Palm2 tree
		Palm2,
		// Cypress1 tree
		Cypress1,
		// Cypress2 tree
		Cypress2,
		// Pine2 tree
		Pine2,
		// Plumeria
		Plumeria,
		// Winter pinetree1
		WinterPine1,
		// Winter Aspen tree
		WinterAspen,
		// Winter pinetree2
		WinterPine2,
		// Eucalyptus tree
		Eucalyptus,
		// Fern
		Fern,
		// Eelgrass
		Eelgrass,
		// Sea Sword
		SeaSword,
		// Kelp1 plant
		Kelp1,
		// Beach grass
		BeachGrass1,
		// Kelp2 plant
		Kelp2;

		public static Tree setValue(final byte value)
		{
			if (0 <= value && value < Tree.values().length)
				return Tree.values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	/** Grass foliage types */
	public enum Grass
	{
		Grass0, Grass1, Grass2, Grass3, Grass4, Undergrowth1;

		public static Grass setValue(final int value)
		{
			if (0 <= value && value < Grass.values().length)
				return Grass.values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}

	}

	/** Action associated with clicking on an object */
	public enum ClickAction
	{
		/** Touch object */
		Touch,
		/** Sit on object */
		Sit,
		/** Purchase object or contents */
		Buy,
		/** Pay the object */
		Pay,
		/** Open task inventory */
		OpenTask,
		/** Play parcel media */
		PlayMedia,
		/** Open parcel media */
		OpenMedia;

		public static ClickAction setValue(final int value)
		{
			if (0 <= value && value < ClickAction.values().length)
				return ClickAction.values()[value];
			return null;
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}

	}

	// #region Subclasses

	// Parameters used to construct a visual representation of a primitive
	public class ConstructionData
	{
		public ProfileCurve ProfileCurve;
		public HoleType ProfileHole;
		public PathCurve PathCurve;
		public float PathBegin;
		public float PathEnd;
		public float PathRadiusOffset;
		public float PathSkew;
		public float PathScaleX;
		public float PathScaleY;
		public float PathShearX;
		public float PathShearY;
		public float PathTaperX;
		public float PathTaperY;
		public float PathTwist;
		public float PathTwistBegin;
		public float PathRevolutions;
		public float ProfileBegin;
		public float ProfileEnd;
		public float ProfileHollow;

		public Material Material;
		public byte State;
		public PCode PCode;

		public ConstructionData()
		{
			this.ProfileCurve = Primitive.ProfileCurve.Square;
			this.ProfileBegin = 0.0f;
			this.ProfileEnd = 1.0f;
			this.ProfileHollow = 0.0f;
			this.PathCurve = Primitive.PathCurve.Line;
			this.PathBegin = 0.0f;
			this.PathEnd = 1.0f;
			this.PathRadiusOffset = 0.0f;
			this.PathSkew = 0.0f;
			this.PathScaleX = 1.0f;
			this.PathScaleY = 1.0f;
			this.PathShearX = 0.0f;
			this.PathShearY = 0.0f;
			this.PathTaperX = 0.0f;
			this.PathTaperY = 0.0f;
			this.PathTwist = 0.0f;
			this.PathTwistBegin = 0.0f;
			this.PathRevolutions = 1.0f;
/*
 			ProfileHole = primData.ProfileHole;
 

			Material = primData.Material;
			State = primData.State;
			PCode = primData.PCode;
*/
		}

		public ConstructionData(final OSD osd)
		{
			this.fromOSD(osd);
		}

		// #region Properties

		public ConstructionData(final ConstructionData primData)
		{
			this.ProfileCurve = primData.ProfileCurve;
			this.ProfileHole = primData.ProfileHole;
			this.PathCurve = primData.PathCurve;
			this.PathBegin = primData.PathBegin;
			this.PathEnd = primData.PathEnd;
			this.PathRadiusOffset = primData.PathRadiusOffset;
			this.PathSkew = primData.PathSkew;
			this.PathScaleX = primData.PathScaleX;
			this.PathScaleY = primData.PathScaleY;
			this.PathShearX = primData.PathShearX;
			this.PathShearY = primData.PathShearY;
			this.PathTaperX = primData.PathTaperX;
			this.PathTaperY = primData.PathTaperY;
			this.PathTwistBegin = primData.PathTwistBegin;
			this.PathTwist = primData.PathTwist;
			this.PathRevolutions = primData.PathRevolutions;
			this.ProfileBegin = primData.ProfileBegin;
			this.ProfileEnd = primData.ProfileEnd;
			this.ProfileHollow = primData.ProfileHollow;

			this.Material = primData.Material;
			this.State = primData.State;
			this.PCode = primData.PCode;
		}

	    /**
	     * Setup construction data for a basic primitive shape
	     *
	     * @param type Primitive shape to construct
	     * @returns Construction data that can be plugged into a <seealso cref="Primitive"/>
	     */
	    public ConstructionData(final PrimType type)
	    {
			this.PCode = Primitive.PCode.Prim;
			this.Material = Primitive.Material.Wood;

	        switch (type)
	        {
	            case Box:
					this.ProfileCurve = Primitive.ProfileCurve.Square;
					this.PathCurve = Primitive.PathCurve.Line;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 1.0f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Cylinder:
					this.ProfileCurve = Primitive.ProfileCurve.Circle;
					this.PathCurve = Primitive.PathCurve.Line;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 1.0f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Prism:
					this.ProfileCurve = Primitive.ProfileCurve.EquilateralTriangle;
					this.PathCurve = Primitive.PathCurve.Line;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 0.0f;
					this.PathScaleY = 0.0f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Ring:
					this.ProfileCurve = Primitive.ProfileCurve.EquilateralTriangle;
					this.PathCurve = Primitive.PathCurve.Circle;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 0.25f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Sphere:
					this.ProfileCurve = Primitive.ProfileCurve.HalfCircle;
					this.PathCurve = Primitive.PathCurve.Circle;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 1.0f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Torus:
					this.ProfileCurve = Primitive.ProfileCurve.Circle;
					this.PathCurve = Primitive.PathCurve.Circle;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 0.25f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Tube:
					this.ProfileCurve = Primitive.ProfileCurve.Square;
					this.PathCurve = Primitive.PathCurve.Circle;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 0.25f;
					this.PathRevolutions = 1.0f;
	                break;
	            case Sculpt:
					this.ProfileCurve = Primitive.ProfileCurve.Circle;
					this.PathCurve = Primitive.PathCurve.Circle;
					this.ProfileEnd = 1.0f;
					this.PathEnd = 1.0f;
					this.PathScaleX = 1.0f;
					this.PathScaleY = 0.5f;
					this.PathRevolutions = 1.0f;
	                break;
	            default:
	                throw new UnsupportedOperationException("Unsupported shape: " + type);
	        }
	    }

		// Attachment point to an avatar
		public AttachmentPoint getAttachmentPoint()
		{
			return AttachmentPoint.values()[Helpers.SwapNibbles(this.State)];
		}

		public void setAttachmentPoint(final AttachmentPoint value)
		{
			this.State = Helpers.SwapNibbles((byte) value.ordinal());
		}

		public byte getProfileValue()
		{
			return (byte) (this.ProfileCurve.getValue() | this.ProfileHole.getValue());
		}

		public void setProfileValue(final byte value)
		{
			this.ProfileCurve = Primitive.ProfileCurve.setValue(value & 0xF);
			this.ProfileHole = HoleType.setValue(value >> 4);
		}

		public ProfileCurve getProfileCurve()
		{
			return this.ProfileCurve;
		}

		public void setProfileCurve(final ProfileCurve value)
		{
			this.ProfileCurve = value;
		}

		public HoleType getProfileHole()
		{
			return this.ProfileHole;
		}

		public void setProfileHole(final HoleType value)
		{
			this.ProfileHole = value;
		}

		public Vector2 getPathBeginScale()
		{
			final Vector2 begin = new Vector2(1.0f, 1.0f);
			if (1.0f < PathScaleX)
				begin.X = 2.0f - this.PathScaleX;
			if (1.0f < PathScaleY)
				begin.Y = 2.0f - this.PathScaleY;
			return begin;
		}

		public Vector2 getPathEndScale()
		{
			final Vector2 end = new Vector2(1.0f, 1.0f);
			if (1.0f > PathScaleX)
				end.X = this.PathScaleX;
			if (1.0f > PathScaleY)
				end.Y = this.PathScaleY;
			return end;
		}

		// #endregion Properties

		public OSD Serialize()
		{
			final OSDMap path = new OSDMap(14);
			path.put("begin", OSD.FromReal(this.PathBegin));
			path.put("curve", OSD.FromInteger(this.PathCurve.getValue()));
			path.put("end", OSD.FromReal(this.PathEnd));
			path.put("radius_offset", OSD.FromReal(this.PathRadiusOffset));
			path.put("revolutions", OSD.FromReal(this.PathRevolutions));
			path.put("scale_x", OSD.FromReal(this.PathScaleX));
			path.put("scale_y", OSD.FromReal(this.PathScaleY));
			path.put("shear_x", OSD.FromReal(this.PathShearX));
			path.put("shear_y", OSD.FromReal(this.PathShearY));
			path.put("skew", OSD.FromReal(this.PathSkew));
			path.put("taper_x", OSD.FromReal(this.PathTaperX));
			path.put("taper_y", OSD.FromReal(this.PathTaperY));
			path.put("twist", OSD.FromReal(this.PathTwist));
			path.put("twist_begin", OSD.FromReal(this.PathTwistBegin));

			final OSDMap profile = new OSDMap(4);
			profile.put("begin", OSD.FromReal(this.ProfileBegin));
			profile.put("curve", OSD.FromInteger(this.ProfileCurve.getValue()));
			profile.put("hole", OSD.FromInteger(this.ProfileHole.getValue()));
			profile.put("end", OSD.FromReal(this.ProfileEnd));
			profile.put("hollow", OSD.FromReal(this.ProfileHollow));

			final OSDMap volume = new OSDMap(2);
			volume.put("path", path);
			volume.put("profile", profile);

			return volume;
		}

		public void fromOSD(final OSD osd)
		{
			if (osd instanceof final OSDMap map)
			{

				final OSDMap volume = (OSDMap) map.get("volume");
				final OSDMap path = (OSDMap) volume.get("path");
				final OSDMap profile = (OSDMap) volume.get("profile");

				this.State = 0;
				this.Material = Primitive.Material.setValue(map.get("material").AsInteger());
				this.PCode = Primitive.PCode.Prim; // TODO: Put this in SD

				this.PathBegin = (float) path.get("begin").AsReal();
				this.PathCurve = Primitive.PathCurve.setValue((byte) path.get("curve").AsInteger());
				this.PathEnd = (float) path.get("end").AsReal();
				this.PathRadiusOffset = (float) path.get("radius_offset").AsReal();
				this.PathRevolutions = (float) path.get("revolutions").AsReal();
				this.PathScaleX = (float) path.get("scale_x").AsReal();
				this.PathScaleY = (float) path.get("scale_y").AsReal();
				this.PathShearX = (float) path.get("shear_x").AsReal();
				this.PathShearY = (float) path.get("shear_y").AsReal();
				this.PathSkew = (float) path.get("skew").AsReal();
				this.PathTaperX = (float) path.get("taper_x").AsReal();
				this.PathTaperY = (float) path.get("taper_y").AsReal();
				this.PathTwist = (float) path.get("twist").AsReal();
				this.PathTwistBegin = (float) path.get("twist_begin").AsReal();

				this.ProfileBegin = (float) profile.get("begin").AsReal();
				this.ProfileEnd = (float) profile.get("end").AsReal();
				this.ProfileHollow = (float) profile.get("hollow").AsReal();
				this.ProfileCurve = Primitive.ProfileCurve.setValue(profile.get("curve").AsInteger());
				this.ProfileHole = Primitive.HoleType.setValue(profile.get("hole").AsInteger());

			}
		}

		@Override
		public int hashCode()
		{
			return ((Float) this.PathBegin).hashCode()  ^	((Float) this.PathEnd).hashCode() ^ ((Float) this.PathRadiusOffset).hashCode() ^
					((Float) this.PathRevolutions).hashCode() ^ ((Float) this.PathScaleX).hashCode() ^ ((Float) this.PathScaleY).hashCode() ^
					((Float) this.PathShearX).hashCode() ^ ((Float) this.PathShearY).hashCode() ^ ((Float) this.PathSkew).hashCode() ^
					((Float) this.PathTaperX).hashCode() ^ ((Float) this.PathTaperY).hashCode() ^ ((Float) this.PathTwist).hashCode() ^
					((Float) this.PathTwistBegin).hashCode() ^ ((Float) this.ProfileBegin).hashCode() ^ ((Float) this.ProfileEnd).hashCode() ^
					((Float) this.ProfileHollow).hashCode() ^ this.State ^
					(null == Material ? 0 : this.Material.hashCode()) ^ (null == PCode ? 0 : this.PCode.hashCode()) ^
					(null == ProfileCurve ? 0 : this.ProfileCurve.hashCode()) ^ (null == PathCurve ? 0 : this.PathCurve.hashCode());
		}

		@Override
		public boolean equals(final Object o)
		{
			return null != o && o instanceof ConstructionData && this.equals((ConstructionData) o);
		}

		public boolean equals(final ConstructionData o)
		{
			if (null != o)
			{
				return this.PathBegin == o.PathBegin && this.PathEnd == o.PathEnd && this.PathRadiusOffset == o.PathRadiusOffset &&
						this.PathRevolutions == o.PathRevolutions && this.PathScaleX == o.PathScaleX && this.PathScaleY == o.PathScaleY &&
						this.PathShearX == o.PathShearX && this.PathShearY == o.PathShearY && this.PathSkew == o.PathSkew &&
						this.PathTaperX == o.PathTaperX && this.PathTaperY == o.PathTaperY && this.PathTwist == o.PathTwist &&
						this.PathTwistBegin == o.PathTwistBegin && this.ProfileBegin == o.ProfileBegin && this.ProfileEnd == o.ProfileEnd &&
						this.ProfileHollow == o.ProfileHollow && this.State == o.State &&
					(null == Material ? this.Material == o.Material : this.Material == o.Material) &&
					(null == PCode ? this.PCode == o.PCode : this.PCode == o.PCode) &&
					(null == ProfileCurve ? this.ProfileCurve == o.ProfileCurve : this.ProfileCurve == o.ProfileCurve) &&
					(null == PathCurve ? this.PathCurve == o.PathCurve : this.PathCurve == o.PathCurve);
			}
			return false;
		}
	}

	// Information on the flexible properties of a primitive
	public class FlexibleData
	{
		public int Softness;
		public float Gravity;
		public float Drag;
		public float Wind;
		public float Tension;
		public Vector3 Force;

		// Default constructor
		public FlexibleData()
		{
			this.Softness = 2;
			this.Gravity = 0.3f;
			this.Drag = 2.0f;
			this.Wind = 0.0f;
			this.Tension = 1.0f;
			this.Force = Vector3.Zero;
		}

		public FlexibleData(final OSD osd)
		{
			this();
			this.fromOSD(osd);
		}

		public FlexibleData(final byte[] data, int pos, final int length)
		{
			this();
			if (4 <= length && data.length >= pos + 4)
			{
				this.Softness = ((data[pos] & 0x80) >> 6) | ((data[pos + 1] & 0x80) >> 7);

				this.Tension = (data[pos] & 0x7F) / 10.0f;
				pos++;
				this.Drag = (data[pos] & 0x7F) / 10.0f;
				pos++;
				this.Gravity = data[pos] / 10.0f;
				pos++;
				this.Wind = data[pos] / 10.0f;
				pos++;
				if (16 <= length && data.length >= pos + 12)
				{
					this.Force = new Vector3(data, pos);
				}
			}
		}

		public FlexibleData(final FlexibleData data)
		{
			this.Softness = data.Softness;

			this.Tension = data.Tension;
			this.Drag = data.Drag;
			this.Gravity = data.Gravity;
			this.Wind = data.Wind;
			this.Force = new Vector3(data.Force);
		}

		public byte[] GetBytes()
		{
			final byte[] data = new byte[16];
			int i = 0;
			// Softness is packed in the upper bits of tension and drag
			data[i] = (byte) (((int)(this.Tension * 10.01f) & 0x7F) | ((this.Softness & 2) << 6));
			i++;
			data[i] = (byte) (((int)(this.Drag * 10.01f) & 0x7F) | ((this.Softness & 1) << 7));
			i++;
			data[i] = (byte) (this.Gravity * 10.01f);
			i++;
			data[i] = (byte) (this.Wind * 10.01f);
			i++;

			this.Force.toBytes(data, i);

			return data;
		}

		public OSD Serialize()
		{
			final OSDMap map = new OSDMap();

			map.put("simulate_lod", OSD.FromInteger(this.Softness));
			map.put("gravity", OSD.FromReal(this.Gravity));
			map.put("air_friction", OSD.FromReal(this.Drag));
			map.put("wind_sensitivity", OSD.FromReal(this.Wind));
			map.put("tension", OSD.FromReal(this.Tension));
			map.put("user_force", OSD.FromVector3(this.Force));

			return map;
		}

		public void fromOSD(final OSD osd)
		{
			if (osd.getType() == OSD.OSDType.Map)
			{
				final OSDMap map = (OSDMap) osd;

				this.Softness = map.get("simulate_lod").AsInteger();
				this.Gravity = (float) map.get("gravity").AsReal();
				this.Drag = (float) map.get("air_friction").AsReal();
				this.Wind = (float) map.get("wind_sensitivity").AsReal();
				this.Tension = (float) map.get("tension").AsReal();
				this.Force = map.get("user_force").AsVector3();
			}
		}

		@Override
		public int hashCode()
		{
			return this.Softness ^ ((Float) this.Gravity).hashCode() ^ ((Float) this.Drag).hashCode() ^ ((Float) this.Wind).hashCode() ^ ((Float) this.Tension).hashCode() ^ (null == Force ? 0 : this.Force.hashCode());
		}
		@Override
		public boolean equals(final Object obj)
		{
			return null != obj && (obj instanceof FlexibleData) && this.equals((FlexibleData)obj);
		}

		public boolean equals(final FlexibleData obj)
		{
			return null != obj && this.Softness == obj.Softness && this.Gravity == obj.Gravity && this.Drag == obj.Drag
					           && this.Wind == obj.Wind && this.Tension == obj.Tension && this.Force == obj.Force;
		}
	}

	// Information on the light properties of a primitive
	public class LightData
	{
		public Color4 Color;
		public float Intensity;
		public float Radius;
		public float Cutoff;
		public float Falloff;

		// Default constructor
		public LightData()
		{
			this.Color = Color4.White;
			this.Radius = 10.0f;
			this.Cutoff = 0.0f;
			this.Falloff = 0.75f;
		}

		public LightData(final OSD osd)
		{
			this();
			this.fromOSD(osd);
		}

		public LightData(final byte[] data, final int pos, final int length)
		{
			this();
			if (16 <= length && data.length >= 16 + pos)
			{
				this.Color = new Color4(data, pos, false);
				this.Radius = Helpers.BytesToFloatL(data, pos + 4);
				this.Cutoff = Helpers.BytesToFloatL(data, pos + 8);
				this.Falloff = Helpers.BytesToFloatL(data, pos + 12);

				// Alpha in color is actually intensity
				this.Intensity = this.Color.A;
				this.Color.A = 1.0f;
			}
		}

		public LightData(final LightData light)
		{
			this.Color = new Color4(light.Color);
			this.Radius = light.Radius;
			this.Cutoff = light.Cutoff;
			this.Falloff = light.Falloff;
			this.Intensity = light.Intensity;
		}

		public byte[] GetBytes()
		{
			final byte[] data = new byte[16];

			// Alpha channel in color is intensity
			final Color4 tmpColor = this.Color;
			tmpColor.A = this.Intensity;
			tmpColor.toBytes(data, 0);
			Helpers.FloatToBytesL(this.Radius, data, 4);
			Helpers.FloatToBytesL(this.Cutoff, data, 8);
			Helpers.FloatToBytesL(this.Falloff, data, 12);

			return data;
		}

		public OSD Serialize()
		{
			final OSDMap map = new OSDMap();

			map.put("color", OSD.FromColor4(this.Color));
			map.put("intensity", OSD.FromReal(this.Intensity));
			map.put("radius", OSD.FromReal(this.Radius));
			map.put("cutoff", OSD.FromReal(this.Cutoff));
			map.put("falloff", OSD.FromReal(this.Falloff));

			return map;
		}

		public void fromOSD(final OSD osd)
		{
			if (OSD.OSDType.Map == osd.getType())
			{
				final OSDMap map = (OSDMap) osd;

				this.Color = map.get("color").AsColor4();
				this.Intensity = (float) map.get("intensity").AsReal();
				this.Radius = (float) map.get("radius").AsReal();
				this.Cutoff = (float) map.get("cutoff").AsReal();
				this.Falloff = (float) map.get("falloff").AsReal();
			}
		}

		@Override
		public int hashCode()
		{
			return (null != Color ? this.Color.hashCode() : 0) ^ ((Float) this.Intensity).hashCode() ^ ((Float) this.Radius).hashCode() ^ ((Float) this.Cutoff).hashCode() ^ ((Float) this.Falloff).hashCode();
		}

		@Override
		public boolean equals(final Object obj)
		{
			return null != obj && (obj instanceof LightData) && this.equals((LightData)obj);
		}

		public boolean equals(final LightData obj)
		{
			return null != obj && null == Color ? (this.Color == obj.Color) : (this.Color.equals(obj.Color))
					           && this.Intensity == obj.Intensity && this.Radius == obj.Radius && this.Cutoff == obj.Cutoff && this.Falloff == obj.Falloff;
		}

		@Override
		public String toString()
		{
			return String.format("Color: %s Intensity: %f Radius: %f Cutoff: %f Falloff: %f", this.Color, this.Intensity, this.Radius, this.Cutoff, this.Falloff);
		}
	}

	// Information on the sculpt properties of a sculpted primitive
	public class SculptData
	{
		public UUID SculptTexture;
		private byte type;

		public SculptType getType()
		{
			return SculptType.values()[this.type & 7];
		}

		public void setType(final SculptType value)
		{
			this.type = value.getValue();
		}

		public void setType(final int value)
		{
			this.type = (byte) (value & 0x7);
		}

		// Render inside out (inverts the normals).
		public boolean getInvert()
		{
			return 0 != (type & SculptType.Invert.getValue());
		}

		// Render an X axis mirror of the sculpty.
		public boolean getMirror()
		{
			return 0 != (type & SculptType.Mirror.getValue());
		}

		// Default constructor
		public SculptData()
		{
		}

		public SculptData(final OSD osd)
		{
			this.fromOSD(osd);
		}

		public SculptData(final byte[] data, final int pos, final int length)
		{
			if (17 <= length && data.length >= 17 + pos)
			{
				this.SculptTexture = new UUID(data, pos);
				this.type = data[pos + 16];
			}
			else
			{
				this.SculptTexture = UUID.Zero;
				this.type = SculptType.None.getValue();
			}
		}

		public SculptData(final SculptData value)
		{
			this.SculptTexture = value.SculptTexture;
			type = value.getType().getValue();
		}

		public byte[] GetBytes()
		{
			final byte[] data = new byte[17];

			this.SculptTexture.toBytes(data, 0);
			data[16] = this.type;

			return data;
		}

		public OSD Serialize()
		{
			final OSDMap map = new OSDMap();

			map.put("texture", OSD.FromUUID(this.SculptTexture));
			map.put("type", OSD.FromInteger(this.type));

			return map;
		}

		public void fromOSD(final OSD osd)
		{
			if (OSD.OSDType.Map == osd.getType())
			{
				final OSDMap map = (OSDMap) osd;

				this.SculptTexture = map.get("texture").AsUUID();
				this.type = (byte) map.get("type").AsInteger();
			}
		}

		@Override
		public int hashCode()
		{
			return (null == SculptTexture ? 0 : this.SculptTexture.hashCode()) ^ this.type;
		}

		@Override
		public boolean equals(final Object obj)
		{
			return null != obj && (obj instanceof SculptData) && this.equals((SculptData)obj);
		}

		public boolean equals(final SculptData obj)
		{
			return null != obj && (null == SculptTexture ? (this.SculptTexture == obj.SculptTexture) : (this.SculptTexture.equals(obj.SculptTexture))) && this.type == obj.type;
		}
	}

	// Information on the sculpt properties of a sculpted primitive
	public class LightImage
	{
		public UUID LightTexture;
		public Vector3 Params;

		// Default constructor
		public LightImage()
		{
		}

		public LightImage(final OSD osd)
		{
			this.fromOSD(osd);
		}

		public LightImage(final byte[] data, final int pos, final int length)
		{
			if (28 <= length && data.length >= 28 + pos)
			{
				this.LightTexture = new UUID(data, pos);
				this.Params = new Vector3(data, pos + 16, true);
			}
			else
			{
				this.LightTexture = UUID.Zero;
				this.Params = new Vector3(Helpers.PI_OVER_TWO, 0.0f, 0.0f);
			}
		}

		public LightImage(final LightImage value)
		{
			this.LightTexture = value.LightTexture;
			this.Params = value.Params;
		}

		public byte[] GetBytes()
		{
			final byte[] data = new byte[28];

			this.LightTexture.toBytes(data, 0);
			this.Params.toBytes(data, 16, true);
			return data;
		}

		public OSD Serialize()
		{
			final OSDMap map = new OSDMap();

			map.put("texture", OSD.FromUUID(this.LightTexture));
			map.put("params", OSD.FromVector3(this.Params));

			return map;
		}

		public void fromOSD(final OSD osd)
		{
			if (OSD.OSDType.Map == osd.getType())
			{
				final OSDMap map = (OSDMap) osd;

				this.LightTexture = map.get("texture").AsUUID();
				this.Params = map.get("params").AsVector3();
			}
		}

		public boolean isLightSpotlight()
		{
			return null != LightTexture && !this.LightTexture.equals(UUID.Zero);
		}

		@Override
		public String toString()
		{
			return String.format("LightTexture: %s Params; %s", this.LightTexture.toString(), this.Params.toString());
			
		}
		
		@Override
		public int hashCode()
		{
			return (null == LightTexture ? 0 : this.LightTexture.hashCode()) ^ (null == Params ? 0 : this.Params.hashCode());
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			return null != obj && (obj instanceof LightImage) && this.equals((LightImage)obj);
		}

		public boolean equals(final LightImage obj)
		{
			return null != obj && null == LightTexture ? (this.LightTexture == obj.LightTexture) : (this.LightTexture.equals(obj.LightTexture))
					           && null == Params ? (this.Params == obj.Params) : (this.Params.equals(obj.Params));
		}
	}
	// #endregion Subclasses

	// #region Public Members

	// The Object's UUID, asset server
	public UUID ID;

	public UUID GroupID;

	// Object ID in Region (sim) it is in
	public int LocalID;

	public int ParentID;
	public long RegionHandle;
	public int Flags;
	// Location of Object (x,y,z probably)
	public Vector3 Position;
	// Rotational Position of Object
	public Quaternion Rotation = Quaternion.Identity;
	public Vector3 Scale;
	public Vector3 Velocity;
	public Vector3 AngularVelocity;
	public Vector3 Acceleration;
	public Vector4 CollisionPlane;
	public FlexibleData Flexible;
	public LightData Light;
	public LightImage LightMap;
	public SculptData Sculpt;
	public ClickAction clickAction;
	public UUID SoundID;

	// Identifies the owner if audio or a particle system is active
	public UUID OwnerID;
	// Foliage type for this primitive. Only applicable if this primitive is
	// foliage
	public Tree TreeSpecies;
	public byte[] ScratchPad;
	public byte SoundFlags;
	public float SoundGain;
	public float SoundRadius;
	public String Text;
	public Color4 TextColor;
	public String MediaURL;
	public JointType Joint;
	public Vector3 JointPivot;
	public Vector3 JointAxisOrAnchor;
	public NameValue[] NameValues;
	public ConstructionData PrimData;
	public ObjectProperties Properties;
	public PhysicsProperties PhysicsProps;

	public boolean IsAttachment;

	public TextureEntry Textures;
	public TextureEntry.TextureAnimation TextureAnim;

	public ParticleSystem ParticleSys;

	// Current version of the media data for the prim
	public String MediaVersion = Helpers.EmptyString;

	// Array of media entries indexed by face number
	public MediaEntry[] FaceMedia;

	// #endregion Public Members

	// #region Properties

	// Uses basic heuristics to estimate the primitive shape
	public PrimType getType()
	{
		if (null != Sculpt && SculptType.None != Sculpt.getType() && !this.Sculpt.SculptTexture.equals(UUID.Zero))
        {
            if (SculptType.Mesh == Sculpt.getType())
                return PrimType.Mesh;
			return PrimType.Sculpt;
        }

		final boolean linearPath = (PathCurve.Line == PrimData.PathCurve || PathCurve.Flexible == PrimData.PathCurve);
		final float scaleY = this.PrimData.PathScaleY;

		if (linearPath)
		{
			switch (this.PrimData.ProfileCurve)
			{
				case Circle:
					return PrimType.Cylinder;
				case Square:
					return PrimType.Box;
				case IsometricTriangle:
				case EquilateralTriangle:
				case RightTriangle:
					return PrimType.Prism;
				case HalfCircle:
				default:
					return PrimType.Unknown;
			}
		}

		switch (this.PrimData.PathCurve)
		{
			case Flexible:
				return PrimType.Unknown;
			case Circle:
				switch (this.PrimData.ProfileCurve)
				{
					case Circle:
						if (0.75f < scaleY)
							return PrimType.Sphere;
						return PrimType.Torus;
					case HalfCircle:
						return PrimType.Sphere;
					case EquilateralTriangle:
						return PrimType.Ring;
					case Square:
						if (0.75f >= scaleY)
							return PrimType.Tube;
					default:
						return PrimType.Unknown;
				}
			case Circle2:
				if (ProfileCurve.Circle == PrimData.ProfileCurve)
					return PrimType.Sphere;
			default:
				return PrimType.Unknown;
		}
	}

	// #endregion Properties

	// #region Constructors

	// Default constructor
	public Primitive()
	{
		// Default a few null property values to String.Empty
		this.Text = Helpers.EmptyString;
		this.MediaURL = Helpers.EmptyString;
	}

	public Primitive(final OSD osd)
	{
		this();
		this.fromOSD(osd);
	}

	public Primitive(final Primitive prim)
	{
		this.ID = new UUID(prim.ID);
		this.GroupID = new UUID(prim.GroupID);
		this.LocalID = prim.LocalID;
		this.ParentID = prim.ParentID;
		this.RegionHandle = prim.RegionHandle;
		this.Flags = prim.Flags;
		this.TreeSpecies = prim.TreeSpecies;
		if (null != prim.ScratchPad)
		{
			this.ScratchPad = new byte[prim.ScratchPad.length];
			System.arraycopy(prim.ScratchPad, 0, this.ScratchPad, 0, this.ScratchPad.length);
		}
		else
			this.ScratchPad = Helpers.EmptyBytes;
		this.Position = new Vector3(prim.Position);
		this.Scale = prim.Scale;
		this.Rotation = new Quaternion(prim.Rotation);
		this.Velocity = new Vector3(prim.Velocity);
		this.AngularVelocity = new Vector3(prim.AngularVelocity);
		this.Acceleration = new Vector3(prim.Acceleration);
		this.CollisionPlane = new Vector4(prim.CollisionPlane);
		this.Flexible = new FlexibleData(prim.Flexible);
		this.Light = new LightData(prim.Light);
		this.Sculpt = new SculptData(prim.Sculpt);
		this.clickAction = prim.clickAction;
		this.SoundID = new UUID(prim.SoundID);
		this.OwnerID = new UUID(prim.OwnerID);
		this.SoundFlags = prim.SoundFlags;
		this.SoundGain = prim.SoundGain;
		this.SoundRadius = prim.SoundRadius;
		this.Text = prim.Text;
		this.TextColor = new Color4(prim.TextColor);
		this.MediaURL = prim.MediaURL;
		this.Joint = prim.Joint;
		this.JointPivot = prim.JointPivot;
		this.JointAxisOrAnchor = prim.JointAxisOrAnchor;
		if (null != prim.NameValues)
		{
			if (null == NameValues || this.NameValues.length != prim.NameValues.length)
				this.NameValues = new NameValue[prim.NameValues.length];
			System.arraycopy(prim.NameValues, 0, this.NameValues, 0, prim.NameValues.length);
		}
		else
			this.NameValues = null;
		this.PrimData = new ConstructionData(prim.PrimData);
		this.Properties = new ObjectProperties(prim.Properties);
		this.Textures = new TextureEntry(prim.Textures);
		this.TextureAnim = this.Textures.new TextureEntry.TextureAnimation(prim.TextureAnim);
		this.ParticleSys = new ParticleSystem(prim.ParticleSys);
	}

	// #endregion Constructors

	// #region Public Methods

	public OSD Serialize()
	{

		final OSDMap prim = new OSDMap(9);
		if (null != Properties)
		{
			prim.put("name", OSD.FromString(this.Properties.Name));
			prim.put("description", OSD.FromString(this.Properties.Description));
		}
		else
		{
			prim.put("name", OSD.FromString("Object"));
			prim.put("description", OSD.FromString(Helpers.EmptyString));
		}

		prim.put("phantom", OSD.FromBoolean(0 != (Flags & PrimFlags.Phantom)));
		prim.put("physical", OSD.FromBoolean(0 != (Flags & PrimFlags.Physics)));
		prim.put("position", OSD.FromVector3(this.Position));
		prim.put("rotation", OSD.FromQuaternion(this.Rotation));
		prim.put("scale", OSD.FromVector3(this.Scale));
		prim.put("material", OSD.FromInteger(this.PrimData.Material.getValue()));
		prim.put("shadows", OSD.FromBoolean(0 != (Flags & PrimFlags.CastShadows)));
		prim.put("parentid", OSD.FromInteger(this.ParentID));

		prim.put("volume", this.PrimData.Serialize());

		if (null != Textures)
			prim.put("textures", this.Textures.serialize());

		if (0 != (TextureAnim.Flags & TextureAnimMode.ANIM_ON))
            prim.put("texture_anim", this.TextureAnim.serialize());

		if (null != Light)
			prim.put("light", this.Light.Serialize());

		if (null != LightMap)
			prim.put("light_image", this.LightMap.Serialize());

		if (null != Flexible)
			prim.put("flex", this.Flexible.Serialize());

		if (null != Sculpt)
			prim.put("sculpt", this.Sculpt.Serialize());

		return prim;
	}

	public void fromOSD(final OSD osd)
	{
		if (osd instanceof final OSDMap map)
		{

			if (map.get("phantom").AsBoolean())
				this.Flags = PrimFlags.Phantom;

			if (map.get("physical").AsBoolean())
				this.Flags |= PrimFlags.Physics;

			if (map.get("shadows").AsBoolean())
				this.Flags |= PrimFlags.CastShadows;

			this.ParentID = map.get("parentid").AsInteger();
			this.Position = map.get("position").AsVector3();
			this.Rotation = map.get("rotation").AsQuaternion();
			this.Scale = map.get("scale").AsVector3();

			this.PrimData = new ConstructionData(map.get("volume"));
			this.Flexible = new FlexibleData(map.get("flex"));
			this.Light = new LightData(map.get("light"));
			this.LightMap = new LightImage(map.get("light_image"));

			if (map.containsKey("sculpt"))
				this.Sculpt = new SculptData(map.get("sculpt"));

			this.Textures = new TextureEntry(map.get("textures"));

			if (map.containsKey("texture_anim"))
				this.TextureAnim = this.Textures.new TextureEntry.TextureAnimation(map.get("texture_anim"));

			this.Properties = new ObjectProperties();

			String s;
			s = map.get("name").AsString();
			if (null != s && !s.isEmpty())
			{
				this.Properties.Name = s;
			}
			s = map.get("description").AsString();
			if (null != s && !s.isEmpty())
			{
				this.Properties.Description = s;
			}
		}
	}

	public int SetExtraParamsFromBytes(final byte[] data, final int pos)
	{
		int i = pos;
		int totalLength = 1;

		if (0 == data.length || pos >= data.length)
			return 0;

		final byte extraParamCount = data[i];
		i++;

		for (int k = 0; k < extraParamCount; k++)
		{
			final ExtraParamType type = ExtraParamType.setValue(Helpers.BytesToUInt16L(data, i));
			i += 2;

			final int paramLength = (int) Helpers.BytesToUInt32L(data, i);
			i += 4;

			switch (type)
			{
				case Flexible:
					this.Flexible = new FlexibleData(data, i, paramLength);
					break;
				case Light:
					this.Light = new LightData(data, i, paramLength);
					break;
				case LightImage:
					this.LightMap = new LightImage(data, i, paramLength);
					break;
				case Sculpt:
				case Mesh:
					this.Sculpt = new SculptData(data, i, paramLength);
					break;
				default:
					break;
			}
			i += paramLength;
			totalLength += paramLength + 6;
		}
		return totalLength;
	}

	public byte[] GetExtraParamsBytes() throws IOException
	{
		final ByteArrayOutputStream data = new ByteArrayOutputStream();
		byte[] buffer;
		byte count = 0;

		data.write(0);
		if (null != Flexible)
		{
			data.write(Helpers.UInt16ToBytesL(ExtraParamType.Flexible.getValue()));
			buffer = this.Flexible.GetBytes();
			data.write(Helpers.UInt32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}
	
		if (null != Light)
		{
			data.write(Helpers.UInt16ToBytesL(ExtraParamType.Flexible.getValue()));
			buffer = this.Light.GetBytes();
			data.write(Helpers.UInt32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}
		
		if (null != LightMap)
		{
			data.write(Helpers.UInt16ToBytesL(ExtraParamType.LightImage.getValue()));
			buffer = this.LightMap.GetBytes();
			data.write(Helpers.UInt32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}
		
		if (null != Sculpt)
		{
            if (SculptType.Mesh == Sculpt.getType())
            {
    			data.write(Helpers.UInt16ToBytesL(ExtraParamType.Mesh.getValue()));
            }
            else
            {
    			data.write(Helpers.UInt16ToBytesL(ExtraParamType.Sculpt.getValue()));
            }
			buffer = this.Sculpt.GetBytes();
			data.write(Helpers.UInt32ToBytesL(buffer.length));
			data.write(buffer);
			++count;
		}

		buffer = data.toByteArray();
		buffer[0] = count;
		return buffer;
	}

	// #endregion Public Methods

	// #region Overrides

	@Override
	public boolean equals(final Object obj)
	{
		return obj instanceof Primitive && Primitive.equals(this, (Primitive) obj);
	}

	public boolean equals(final Primitive other)
	{
		return Primitive.equals(this, other);
	}

	public static boolean equals(final Primitive lhs, final Primitive rhs)
	{
		if (null == lhs || null == rhs)
		{
			return rhs == lhs;
		}
		return (lhs.ID == rhs.ID);
	}

	@Override
	public String toString()
	{
		if (PCode.Prim == Objects.requireNonNull(PrimData.PCode)) {
			return String.format("%s (%s)", this.getType().toString(), this.ID.toString());
		}
		return String.format("%s (%s)", this.PrimData.PCode, this.ID.toString());
	}

	@Override
	public int hashCode()
	{
		return this.Position.hashCode() ^ this.Velocity.hashCode() ^ this.Acceleration.hashCode() ^ this.Rotation.hashCode()
				^ this.AngularVelocity.hashCode() ^ this.clickAction.hashCode() ^ (null != Flexible ? this.Flexible.hashCode() : 0)
				^ (null != Light ? this.Light.hashCode() : 0) ^ (null != Sculpt ? this.Sculpt.hashCode() : 0) ^ this.Flags
				^ (null != MediaURL ? this.MediaURL.hashCode() : 0) ^ (null != OwnerID ? this.OwnerID.hashCode() : 0) ^ this.ParentID
				^ (null != PrimData ? this.PrimData.hashCode() : 0) ^ (null != ParticleSys ? this.ParticleSys.hashCode() : 0)
				^ (null != TextColor ? this.TextColor.hashCode() : 0) ^ (null != TextureAnim ? this.TextureAnim.hashCode() : 0)
				^ (null != Textures ? this.Textures.hashCode() : 0) ^ (int) this.SoundRadius ^ (null != Scale ? this.Scale.hashCode() : 0)
				^ this.SoundID.hashCode() ^ this.Text.hashCode() ^ this.TreeSpecies.hashCode();
	}

	// #endregion Overrides

	// #region Parameter Packing Methods

	public static short PackBeginCut(final float beginCut)
	{
		return (short) Helpers.roundFromZero(beginCut / Primitive.CUT_QUANTA);
	}

	public static short PackEndCut(final float endCut)
	{
		return (short) (50000 - Helpers.roundFromZero(endCut / Primitive.CUT_QUANTA));
	}

	public static byte PackPathScale(final float pathScale)
	{
		return (byte) (200 - Helpers.roundFromZero(pathScale / Primitive.SCALE_QUANTA));
	}

	public static byte PackPathShear(final float pathShear)
	{
		return (byte) Helpers.roundFromZero(pathShear / Primitive.SHEAR_QUANTA);
	}

	/**
	 * Packs PathTwist, PathTwistBegin, PathRadiusOffset, and PathSkew
	 * parameters in to signed eight bit values
	 * 
	 * @param pathTwist
	 *            Floating point parameter to pack
	 * @return Signed eight bit value containing the packed parameter
	 */
	public static byte PackPathTwist(final float pathTwist)
	{
		return (byte) Helpers.roundFromZero(pathTwist / Primitive.SCALE_QUANTA);
	}

	public static byte PackPathTaper(final float pathTaper)
	{
		return (byte) Helpers.roundFromZero(pathTaper / Primitive.TAPER_QUANTA);
	}

	public static byte PackPathRevolutions(final float pathRevolutions)
	{
		return (byte) Helpers.roundFromZero((pathRevolutions - 1.0f) / Primitive.REV_QUANTA);
	}

	public static short PackProfileHollow(final float profileHollow)
	{
		return (short) Helpers.roundFromZero(profileHollow / Primitive.HOLLOW_QUANTA);
	}

	// #endregion Parameter Packing Methods

	// #region Parameter Unpacking Methods

	public static float UnpackBeginCut(final short beginCut)
	{
		return beginCut * Primitive.CUT_QUANTA;
	}

	public static float UnpackEndCut(final short endCut)
	{
		return (50000 - endCut) * Primitive.CUT_QUANTA;
	}

	public static float UnpackPathScale(final byte pathScale)
	{
		return (200 - pathScale) * Primitive.SCALE_QUANTA;
	}

	public static float UnpackPathShear(final byte pathShear)
	{
		return pathShear * Primitive.SHEAR_QUANTA;
	}

	/**
	 * Unpacks PathTwist, PathTwistBegin, PathRadiusOffset, and PathSkew
	 * parameters from signed eight bit integers to floating point values
	 * 
	 * @param pathTwist
	 *            Signed eight bit value to unpack
	 * @return Unpacked floating point value
	 */
	public static float UnpackPathTwist(final byte pathTwist)
	{
		return pathTwist * Primitive.SCALE_QUANTA;
	}

	public static float UnpackPathTaper(final byte pathTaper)
	{
		return pathTaper * Primitive.TAPER_QUANTA;
	}

	public static float UnpackPathRevolutions(final byte pathRevolutions)
	{
		return pathRevolutions * Primitive.REV_QUANTA + 1.0f;
	}

	public static float UnpackProfileHollow(final short profileHollow)
	{
		return profileHollow * Primitive.HOLLOW_QUANTA;
	}
}
