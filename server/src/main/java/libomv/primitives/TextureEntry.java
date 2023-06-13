/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
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

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.utils.Helpers;
import libomv.utils.RefObject;

public class TextureEntry
{
	// The type of bump-mapping applied to a face
	public enum Bumpiness
	{
		None, Brightness, Darkness, Woodgrain, Bark, Bricks, Checker, Concrete, Crustytile, Cutstone, Discs, Gravel, Petridish, Siding, Stonetile, Stucco, Suction, Weave;

		public static Bumpiness setValue(final int value)
		{
			return Bumpiness.values()[value];
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	// The level of shininess applied to a face
	public enum Shininess
	{
		None(0), Low(0x40), Medium(0x80), High(0xC0);

		public static Shininess setValue(final int value)
		{
			for (final Shininess e : Shininess.values())
			{
				if (0 != (e._value & value))
					return e;
			}
			return Shininess.None;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		Shininess(final int value)
		{
			_value = (byte) value;
		}
	}

	// The texture mapping style used for a face
	public enum MappingType
	{
		Default(0), Planar(2);

		public static MappingType setValue(final int value)
		{
			for (final MappingType e : MappingType.values())
			{
				if (e._value == value)
					return e;
			}
			return MappingType.Default;
		}

		public byte getValue()
		{
			return this._value;
		}

		private final byte _value;

		MappingType(final int value)
		{
			_value = (byte) value;
		}
	}

	// Flags in the TextureEntry block that describe which properties are set
	// [Flags]
	public enum TextureAttributes
	{
		;
		public static final int None = 0;
		public static final int TextureID = 1 << 0;
		public static final int RGBA = 1 << 1;
		public static final int RepeatU = 1 << 2;
		public static final int RepeatV = 1 << 3;
		public static final int OffsetU = 1 << 4;
		public static final int OffsetV = 1 << 5;
		public static final int Rotation = 1 << 6;
		public static final int Material = 1 << 7;
		public static final int Media = 1 << 8;
		public static final int Glow = 1 << 9;
		public static final int MaterialID = 1 << 10; 
		public static final int All = 0xFFFFFFFF;

		public static int setValue(final int value)
		{
			return value & TextureAttributes._mask;
		}

		public static int getValue(final int value)
		{
			return value & TextureAttributes._mask;
		}

		private static final int _mask = TextureAttributes.All;
	}

	// Texture animation mode
	// [Flags]
	public enum TextureAnimMode
	{
		;
		// Disable texture animation
		public static final byte ANIM_OFF = 0x00;
		// Enable texture animation
		public static final byte ANIM_ON = 0x01;
		// Loop when animating textures
		public static final byte LOOP = 0x02;
		// Animate in reverse direction
		public static final byte REVERSE = 0x04;
		// Animate forward then reverse
		public static final byte PING_PONG = 0x08;
		// Slide texture smoothly instead of frame-stepping
		public static final byte SMOOTH = 0x10;
		// Rotate texture instead of using frames
		public static final byte ROTATE = 0x20;
		// Scale texture instead of using frames
		public static final byte SCALE = 0x40;

		public static byte setValue(final int value)
		{
			return (byte) (value & TextureAnimMode._mask);
		}

		public static Byte getValue(final byte value)
		{
			return (byte) (value & TextureAnimMode._mask);
		}

		private static final byte _mask = 0x7F;
	}

	// #endregion Enums

	// #region Subclasses

	// A single textured face. Don't instantiate this class yourself, use the
	// methods in TextureEntry
	public class TextureEntryFace implements Cloneable
	{
		// +----------+ S = Shiny
		// | SSFBBBBB | F = Fullbright
		// | 76543210 | B = Bumpmap
		// +----------+
		private final byte BUMP_MASK = 0x1F;
		private final byte FULLBRIGHT_MASK = 0x20;
		private final byte SHINY_MASK = (byte) 0xC0;
		// +----------+ M = Media Flags (web page)
		// | .....TTM | T = Texture Mapping
		// | 76543210 | . = Unused
		// +----------+
		private final byte MEDIA_MASK = 0x01;
		private final byte TEX_MAP_MASK = 0x06;

		private Color4 rgba;
		private float repeatU;
		private float repeatV;
		private float offsetU;
		private float offsetV;
		private float rotation;
		private float glow;
		private byte material;
		private byte media;
		private int hasAttribute;
		private UUID textureID;
		private UUID materialID;
		private final TextureEntryFace defaultTexture;

		// #region Properties
		public byte getMaterial()
		{
			if (0 != (hasAttribute & TextureAttributes.Material))
				return this.material;
			return this.defaultTexture.material;
		}

		public void setMaterial(final byte value)
		{
			this.material = value;
			this.hasAttribute |= TextureAttributes.Material;
		}

		public byte getMedia()
		{
			if (0 != (hasAttribute & TextureAttributes.Media))
				return this.media;
			return this.defaultTexture.media;
		}

		public void setMedia(final byte value)
		{
			this.media = value;
			this.hasAttribute |= TextureAttributes.Media;
		}

		public Color4 getRGBA()
		{
			if (0 != (hasAttribute & TextureAttributes.RGBA))
				return this.rgba;
			return this.defaultTexture.rgba;
		}

		public void setRGBA(final Color4 value)
		{
			this.rgba = value;
			this.hasAttribute |= TextureAttributes.RGBA;
		}

		public float getRepeatU()
		{
			if (0 != (hasAttribute & TextureAttributes.RepeatU))
				return this.repeatU;
			return this.defaultTexture.repeatU;
		}

		public void setRepeatU(final float value)
		{
			this.repeatU = value;
			this.hasAttribute |= TextureAttributes.RepeatU;
		}

		public float getRepeatV()
		{
			if (0 != (hasAttribute & TextureAttributes.RepeatV))
				return this.repeatV;
			return this.defaultTexture.repeatV;
		}

		public void setRepeatV(final float value)
		{
			this.repeatV = value;
			this.hasAttribute |= TextureAttributes.RepeatV;
		}

		public float getOffsetU()
		{
			if (0 != (hasAttribute & TextureAttributes.OffsetU))
				return this.offsetU;
			return this.defaultTexture.offsetU;
		}

		public void setOffsetU(final float value)
		{
			this.offsetU = value;
			this.hasAttribute |= TextureAttributes.OffsetU;
		}

		public float getOffsetV()
		{
			if (0 != (hasAttribute & TextureAttributes.OffsetV))
				return this.offsetV;
			return this.defaultTexture.offsetV;
		}

		public void setOffsetV(final float value)
		{
			this.offsetV = value;
			this.hasAttribute |= TextureAttributes.OffsetV;
		}

		public float getRotation()
		{
			if (0 != (hasAttribute & TextureAttributes.Rotation))
				return this.rotation;
			return this.defaultTexture.rotation;
		}

		public void setRotation(final float value)
		{
			this.rotation = value;
			this.hasAttribute |= TextureAttributes.Rotation;
		}

		public float getGlow()
		{
			if (0 != (hasAttribute & TextureAttributes.Glow))
				return this.glow;
			return this.defaultTexture.glow;
		}

		public void setGlow(final float value)
		{
			this.glow = value;
			this.hasAttribute |= TextureAttributes.Glow;
		}

		public Bumpiness getBump()
		{
			if (0 != (hasAttribute & TextureAttributes.Material))
				return Bumpiness.setValue(this.material & this.BUMP_MASK);
			return this.defaultTexture.getBump();
		}

		public void setBump(final Bumpiness value)
		{
			// Clear out the old material value
			this.material &= ~this.BUMP_MASK;
			// Put the new bump value in the material byte
			this.material |= value.getValue();
			this.hasAttribute |= TextureAttributes.Material;
		}

		public Shininess getShiny()
		{
			if (0 != (hasAttribute & TextureAttributes.Material))
				return Shininess.setValue(this.material & this.SHINY_MASK);
			return this.defaultTexture.getShiny();
		}

		public void setShiny(final Shininess value)
		{
			// Clear out the old shiny value
			this.material &= ~this.SHINY_MASK;
			// Put the new shiny value in the material byte
			this.material |= value.getValue();
			this.hasAttribute |= TextureAttributes.Material;
		}

		public boolean getFullbright()
		{
			if (0 != (hasAttribute & TextureAttributes.Material))
				return 0 != (material & FULLBRIGHT_MASK);
			return this.defaultTexture.getFullbright();
		}

		public void setFullbright(final boolean value)
		{
			// Clear out the old fullbright value
			this.material &= ~this.FULLBRIGHT_MASK;
			if (value)
			{
				this.material |= this.FULLBRIGHT_MASK;
				this.hasAttribute |= TextureAttributes.Material;
			}
		}

		// In the future this will specify whether a webpage is attached to this
		// face
		public boolean getMediaFlags()
		{
			if (0 != (hasAttribute & TextureAttributes.Media))
				return 0 != (media & MEDIA_MASK);
			return this.defaultTexture.getMediaFlags();
		}

		public void setMediaFlags(final boolean value)
		{
			// Clear out the old mediaflags value
			this.media &= ~this.MEDIA_MASK;
			if (value)
			{
				this.media |= this.MEDIA_MASK;
				this.hasAttribute |= TextureAttributes.Media;
			}
		}

		public MappingType getTexMapType()
		{
			if (0 != (hasAttribute & TextureAttributes.Media))
				return MappingType.setValue(this.media & this.TEX_MAP_MASK);
			return this.defaultTexture.getTexMapType();
		}

		public void setTexMapType(final MappingType value)
		{
			// Clear out the old texmap value
			this.media &= ~this.TEX_MAP_MASK;
			// Put the new texmap value in the media byte
			this.media |= value.getValue();
			this.hasAttribute |= TextureAttributes.Media;
		}

		public UUID getTextureID()
		{
			if (0 != (hasAttribute & TextureAttributes.TextureID))
				return this.textureID;
			return this.defaultTexture.textureID;
		}

		public void setTextureID(final UUID value)
		{
			this.textureID = value;
			this.hasAttribute |= TextureAttributes.TextureID;
		}

		public UUID getMaterialID()
		{
			if (0 != (hasAttribute & TextureAttributes.MaterialID))
				return this.materialID;
			return this.defaultTexture.materialID;
		}

		public void setMaterialID(final UUID value)
		{
			this.materialID = value;
			this.hasAttribute |= TextureAttributes.MaterialID;
		}

		/**
		 * Contains the definition for individual faces
		 * 
		 * @param defaultText
		 */
		public TextureEntryFace(final TextureEntryFace defaultText)
		{
			this.rgba = Color4.White;
			this.repeatU = 1.0f;
			this.repeatV = 1.0f;

			this.defaultTexture = defaultText;
			// FIXME: Is this really correct or should this be reversed?
			if (null == defaultTexture)
				this.hasAttribute = TextureAttributes.All;
			else
				this.hasAttribute = TextureAttributes.None;
		}

		public TextureEntryFace(final OSD osd, final TextureEntryFace defaultText, final RefObject<Integer> faceNumber)
		{
			this(defaultText);
			this.fromOSD(osd, faceNumber);
		}

		public OSD serialize(final int faceNumber)
		{
			final OSDMap tex = new OSDMap(10);
			if (0 <= faceNumber)
				tex.put("face_number", OSD.FromInteger(faceNumber));
			tex.put("colors", OSD.FromColor4(this.getRGBA()));
			tex.put("scales", OSD.FromReal(this.getRepeatU()));
			tex.put("scalet", OSD.FromReal(this.getRepeatV()));
			tex.put("offsets", OSD.FromReal(this.getOffsetU()));
			tex.put("offsett", OSD.FromReal(this.getOffsetV()));
			tex.put("imagerot", OSD.FromReal(this.getRotation()));
			tex.put("bump", OSD.FromInteger(this.getBump().getValue()));
//			tex.put("shiny", OSD.FromInteger(getShiny().getValue()));
			tex.put("fullbright", OSD.FromBoolean(this.getFullbright()));
			tex.put("media_flags", OSD.FromInteger(this.getMediaFlags() ? 1 : 0));
//			tex.put("mapping", OSD.FromInteger(getTexMapType().getValue()));
			tex.put("glow", OSD.FromReal(this.getGlow()));

			if (this.getTextureID().equals(TextureEntry.WHITE_TEXTURE))
				tex.put("imageid", OSD.FromUUID(UUID.Zero));
			else
				tex.put("imageid", OSD.FromUUID(this.getTextureID()));

			tex.put("materialid", OSD.FromUUID(this.getMaterialID()));

			return tex;
		}

		public void fromOSD(final OSD osd, final RefObject<Integer> faceNumber)
		{
			if (osd instanceof final OSDMap map)
			{

				faceNumber.argvalue = map.containsKey("face_number") ? map.get("face_number").AsInteger() : -1;
				this.setRGBA(map.get("colors").AsColor4());
				this.setRepeatU((float) map.get("scales").AsReal());
				this.setRepeatV((float) map.get("scalet").AsReal());
				this.setOffsetU((float) map.get("offsets").AsReal());
				this.setOffsetV((float) map.get("offsett").AsReal());
				this.setRotation((float) map.get("imagerot").AsReal());
				this.setBump(Bumpiness.setValue(map.get("bump").AsInteger()));
//				setShiny(Shininess.setValue(map.get("shiny").AsInteger()));
				this.setFullbright(map.get("fullbright").AsBoolean());
				this.setMediaFlags(map.get("media_flags").AsBoolean());
//				setTexMapType(MappingType.setValue(map.get("mapping").AsInteger()));
				this.setGlow((float) map.get("glow").AsReal());
				this.setTextureID(map.get("imageid").AsUUID());
				this.setMaterialID(map.get("materialid").AsUUID());
			}
		}

		
		@Override
		public TextureEntryFace clone()
		{
            final TextureEntryFace ret = new TextureEntryFace(null == this.defaultTexture ? null : defaultTexture.clone());
            ret.rgba = this.rgba;
            ret.repeatU = this.repeatU;
            ret.repeatV = this.repeatV;
            ret.offsetU = this.offsetU;
            ret.offsetV = this.offsetV;
            ret.rotation = this.rotation;
            ret.glow = this.glow;
            ret.material = this.material;
            ret.media = this.media;
            ret.hasAttribute = this.hasAttribute;
            ret.textureID = this.textureID;
            ret.materialID = this.materialID;
            return ret;
		}

		public boolean equals(final TextureEntryFace obj)
		{
			return null != obj && this.getRGBA().equals(obj.getRGBA()) && this.getRepeatU() == obj.getRepeatU() && this.getRepeatV() == obj.getRepeatV() &&
					this.getOffsetU() == obj.getOffsetU() && this.getOffsetV() == obj.getOffsetV() && this.getRotation() == obj.getRotation() &&
					this.getGlow() == obj.getGlow() && this.getBump() == obj.getBump() && this.getShiny() == obj.getShiny() &&
					this.getFullbright() == obj.getFullbright() && this.getMediaFlags() == obj.getMediaFlags() && this.getTexMapType() == obj.getTexMapType() &&
					this.getTextureID().equals(obj.getTextureID()) && this.getMaterialID().equals(obj.getMaterialID());
		}

		@Override
		public boolean equals(final Object obj)
		{
			return null != obj && obj instanceof TextureEntryFace && this.equals((TextureEntryFace)obj);
		}

		@Override
		public int hashCode()
		{
			return this.getRGBA().hashCode() ^ (int) this.getRepeatU() ^ (int) this.getRepeatV() ^ (int) this.getOffsetU()
					^ (int) this.getOffsetV() ^ (int) this.getRotation() ^ (int) this.getGlow() ^ this.getBump().getValue()
					^ this.getShiny().getValue() ^ (this.getFullbright() ? 1 : 0) ^ (this.getMediaFlags() ? 1 : 0)
					^ this.getTexMapType().getValue() ^ this.getTextureID().hashCode() ^ this.getMaterialID().hashCode();
		}

		@Override
		public String toString()
		{
			return String.format("Color: %s RepeatU: %f RepeatV: %f OffsetU: %f OffsetV: %f "
					+ "Rotation: %f Bump: %s Shiny: %s Fullbright: %s Mapping: %s Media: %s Glow: %f ID: %s MaterialID: %s",
					this.getRGBA(), this.getRepeatU(), this.getRepeatV(), this.getOffsetU(), this.getOffsetV(), this.getRotation(), this.getBump(),
					this.getShiny(), this.getFullbright(), this.getTexMapType(), this.getMediaFlags(), this.getGlow(), this.getTextureID(), this.getMaterialID());
		}
	}

	// Controls the texture animation of a particular prim
	public class TextureAnimation
	{
		public byte Flags;
		public int Face;
		public int SizeX;
		public int SizeY;
		public float Start;
		public float Length;
		public float Rate;

		public TextureAnimation()
		{
			this.init();
		}

		public TextureAnimation(final byte[] data, int pos)
		{
			if (data.length >= (16 + pos))
			{
				this.Flags = TextureAnimMode.setValue(data[pos]);
				pos++;
				this.Face = data[pos];
				pos++;
				this.SizeX = data[pos];
				pos++;
				this.SizeY = data[pos];
				pos++;

				this.Start = Helpers.BytesToFloatL(data, pos);
				this.Length = Helpers.BytesToFloatL(data, pos + 4);
				this.Rate = Helpers.BytesToFloatL(data, pos + 8);
			}
			else
			{
				this.init();
			}
		}

		public TextureAnimation(final byte[] data, int pos, final int length)
		{
			if (16 <= length && data.length >= (length + pos))
			{
				this.Flags = TextureAnimMode.setValue(data[pos]);
				pos++;
				this.Face = data[pos];
				pos++;
				this.SizeX = data[pos];
				pos++;
				this.SizeY = data[pos];
				pos++;

				this.Start = Helpers.BytesToFloatL(data, pos);
				this.Length = Helpers.BytesToFloatL(data, pos + 4);
				this.Rate = Helpers.BytesToFloatL(data, pos + 8);
			}
			else
			{
				this.init();
			}
		}

		public TextureAnimation(final OSD osd)
		{
			this.fromOSD(osd);
		}

		public TextureAnimation(final TextureAnimation textureAnim)
		{
			this.Flags = textureAnim.Flags;
			this.Face = textureAnim.Face;
			this.SizeX = textureAnim.SizeX;
			this.SizeY = textureAnim.SizeY;
			this.Start = textureAnim.Start;
			this.Length = textureAnim.Length;
			this.Rate = textureAnim.Rate;
		}

		private void init()
		{
			this.Flags = TextureAnimMode.ANIM_OFF;
			this.Face = 0;
			this.SizeX = 0;
			this.SizeY = 0;

			this.Start = 0.0f;
			this.Length = 0.0f;
			this.Rate = 0.0f;
		}

		public byte[] getBytes()
		{
			final byte[] data = new byte[16];
			int pos = 0;

			data[pos] = TextureAnimMode.getValue(this.Flags);
			pos++;
			data[pos] = (byte) this.Face;
			pos++;
			data[pos] = (byte) this.SizeX;
			pos++;
			data[pos] = (byte) this.SizeY;
			pos++;

			Helpers.FloatToBytesL(this.Start, data, pos);
			Helpers.FloatToBytesL(this.Length, data, pos + 4);
			Helpers.FloatToBytesL(this.Rate, data, pos + 8);

			return data;
		}

		public OSD serialize()
		{
			final OSDMap map = new OSDMap();

			map.put("face", OSD.FromInteger(this.Face));
			map.put("flags", OSD.FromInteger(this.Flags));
			map.put("length", OSD.FromReal(this.Length));
			map.put("rate", OSD.FromReal(this.Rate));
			map.put("size_x", OSD.FromInteger(this.SizeX));
			map.put("size_y", OSD.FromInteger(this.SizeY));
			map.put("start", OSD.FromReal(this.Start));

			return map;
		}

		public void fromOSD(final OSD osd)
		{
			if (osd instanceof final OSDMap map)
			{

				this.Face = map.get("face").AsUInteger();
				this.Flags = TextureAnimMode.setValue(map.get("flags").AsUInteger());
				this.Length = (float) map.get("length").AsReal();
				this.Rate = (float) map.get("rate").AsReal();
				this.SizeX = map.get("size_x").AsUInteger();
				this.SizeY = map.get("size_y").AsUInteger();
				this.Start = (float) map.get("start").AsReal();
			}
			else
			{
				this.init();
			}
		}
	}

	/**
	 * Represents all of the texturable faces for an object
	 * 
	 * Grid objects have infinite faces, with each face using the properties of
	 * the default face unless set otherwise. So if you have a TextureEntry with
	 * a default texture uuid of X, and face 18 has a texture UUID of Y, every
	 * face would be textured with X except for face 18 that uses Y. In practice
	 * however, primitives utilize a maximum of nine faces
	 */
	public static final int MAX_FACES = 32;
	public static final UUID WHITE_TEXTURE = new UUID("5748decc-f629-461c-9a36-a35a221fe21f");

	public TextureEntryFace defaultTexture;
	public TextureEntryFace[] faceTextures = new TextureEntryFace[TextureEntry.MAX_FACES];

	private int numTextures = TextureEntry.MAX_FACES;
	
	public int getNumTextures()
	{
		return this.numTextures;
	}
	
	public void setNumTextures(final int value)
	{
		this.numTextures = MAX_FACES >= value ? value : TextureEntry.MAX_FACES;
	}

	/**
	 * Constructor that takes a default texture UUID
	 * 
	 * @param defaultTextureID
	 *            Texture UUID to use as the default texture
	 */
	public TextureEntry(final UUID defaultTextureID)
	{
		this.defaultTexture = new TextureEntryFace(null);
		this.defaultTexture.setTextureID(defaultTextureID);
	}

	/**
	 * Constructor that takes a {@code TextureEntryFace} for the default
	 * face
	 * 
	 * @param defaultFace
	 *            Face to use as the default face
	 */
	public TextureEntry(final TextureEntryFace defaultFace)
	{
		this.defaultTexture = new TextureEntryFace(null);
		this.defaultTexture.setBump(defaultFace.getBump());
		this.defaultTexture.setFullbright(defaultFace.getFullbright());
		this.defaultTexture.setMediaFlags(defaultFace.getMediaFlags());
		this.defaultTexture.setOffsetU(defaultFace.getOffsetU());
		this.defaultTexture.setOffsetV(defaultFace.getOffsetV());
		this.defaultTexture.setRepeatU(defaultFace.getRepeatU());
		this.defaultTexture.setRepeatV(defaultFace.getRepeatV());
		this.defaultTexture.setRGBA(defaultFace.getRGBA());
		this.defaultTexture.setRotation(defaultFace.getRotation());
		this.defaultTexture.setGlow(defaultFace.getGlow());
		this.defaultTexture.setShiny(defaultFace.getShiny());
		this.defaultTexture.setTexMapType(defaultFace.getTexMapType());
		this.defaultTexture.setTextureID(defaultFace.getTextureID());
	}

	/**
	 * Constructor that takes a {@code TextureEntry} for the default face
	 * 
	 * @param texture
	 *            Texture to copy
	 */
	public TextureEntry(final TextureEntry texture)
	{
		this.defaultTexture = new TextureEntryFace(null);
		this.numTextures = texture.numTextures;
		for (int i = 0; i < this.numTextures; i++)
		{
			this.faceTextures[i] = texture.faceTextures[i];
			if (null != faceTextures[i])
			{
				this.faceTextures[i] = new TextureEntryFace(texture.defaultTexture);
				this.faceTextures[i].setRGBA(texture.faceTextures[i].getRGBA());
				this.faceTextures[i].setRepeatU(texture.faceTextures[i].getRepeatU());
				this.faceTextures[i].setRepeatV(texture.faceTextures[i].getRepeatV());
				this.faceTextures[i].setOffsetU(texture.faceTextures[i].getOffsetU());
				this.faceTextures[i].setOffsetV(texture.faceTextures[i].getOffsetV());
				this.faceTextures[i].setRotation(texture.faceTextures[i].getRotation());
				this.faceTextures[i].setBump(texture.faceTextures[i].getBump());
				this.faceTextures[i].setShiny(texture.faceTextures[i].getShiny());
				this.faceTextures[i].setFullbright(texture.faceTextures[i].getFullbright());
				this.faceTextures[i].setMediaFlags(texture.faceTextures[i].getMediaFlags());
				this.faceTextures[i].setTexMapType(texture.faceTextures[i].getTexMapType());
				this.faceTextures[i].setGlow(texture.faceTextures[i].getGlow());
				this.faceTextures[i].setTextureID(texture.faceTextures[i].getTextureID());
			}
		}
	}

	/**
	 * Constructor that creates the TextureEntry class from a byte array
	 * 
	 * @param data
	 *            Byte array containing the TextureEntry field
	 * @param pos
	 *            Starting position of the TextureEntry field in the byte array
	 * @param length
	 *            Length of the TextureEntry field, in bytes
	 * @throws Exception
	 */
	public TextureEntry(final byte[] data, final int pos, final int length)
	{
		this.fromBytes(data, pos, length);
	}

	public TextureEntry(final byte[] data)
	{
		this.fromBytes(data, 0, data.length);
	}

	public TextureEntry(final OSD osd)
	{
		this.fromOSD(osd);
	}

	/**
	 * This will either create a new face if a custom face for the given index
	 * is not defined, or return the custom face for that index if it already
	 * exists
	 * 
	 * @param index
	 *            The index number of the face to create or retrieve
	 * @return A TextureEntryFace containing all the properties for that
	 * @throws Exception
	 */
	public TextureEntryFace createFace(final int index)
	{
		if (index >= this.numTextures)
			return null;

		if (null == faceTextures[index])
			this.faceTextures[index] = new TextureEntryFace(defaultTexture);

		return this.faceTextures[index];
	}

	public TextureEntryFace getFace(final int index) throws Exception
	{
		if (index >= this.numTextures)
			throw new Exception(index + " is outside the range of MAX_FACES");

		if (null != faceTextures[index])
			return this.faceTextures[index];
		return this.defaultTexture;
	}

	public OSD serialize()
	{
		final OSDArray array = new OSDArray();

		// If DefaultTexture is null, assume the whole TextureEntry is empty
		if (null == defaultTexture)
			return array;

		// Otherwise, always add default texture
		array.add(this.defaultTexture.serialize(-1));

		for (int i = 0; MAX_FACES > i; i++)
		{
			if (null != faceTextures[i])
				array.add(this.faceTextures[i].serialize(i));
		}
		return array;
	}

	public void fromOSD(final OSD osd)
	{
		if (OSD.OSDType.Array == osd.getType())
		{
			final OSDArray array = (OSDArray) osd;

			if (0 < array.size())
			{
				final RefObject<Integer> faceNumber = new RefObject<Integer>(0);
				final OSDMap faceSD = (OSDMap) array.get(0);
				this.defaultTexture = new TextureEntryFace(faceSD, null, faceNumber);

				for (int i = 1; i < array.size(); i++)
				{
					final TextureEntryFace tex = new TextureEntryFace(array.get(i), this.defaultTexture, faceNumber);
					if (0 <= faceNumber.argvalue && faceNumber.argvalue < this.faceTextures.length)
						this.faceTextures[faceNumber.argvalue] = tex;
				}
			}
		}
	}

	private void fromBytes(final byte[] data, final int pos, final int length)
	{
		final Values off = new Values();

		if (length < 16 + pos)
		{
			// No TextureEntry to process
			this.defaultTexture = null;
			return;
		}
		this.defaultTexture = new TextureEntryFace(null);

		off.i = pos;

		// #region Texture
		this.defaultTexture.setTextureID(new UUID(data, off.i));
		off.i += 16;

		while (this.readFaceBitfield(data, off))
		{
			final UUID tmpUUID = new UUID(data, off.i);
			off.i += 16;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setTextureID(tmpUUID);
		}
		// #endregion Texture

		// #region Color
		this.defaultTexture.setRGBA(new Color4(data, off.i, true));
		off.i += 4;

		while (this.readFaceBitfield(data, off))
		{
			final Color4 tmpColor = new Color4(data, off.i, true);
			off.i += 4;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setRGBA(tmpColor);
		}
		// #endregion Color

		// #region RepeatU
		this.defaultTexture.setRepeatU(Helpers.BytesToFloatL(data, off.i));
		off.i += 4;

		while (this.readFaceBitfield(data, off))
		{
			final float tmpFloat = Helpers.BytesToFloatL(data, off.i);
			off.i += 4;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setRepeatU(tmpFloat);
		}
		// #endregion RepeatU

		// #region RepeatV
		this.defaultTexture.setRepeatV(Helpers.BytesToFloatL(data, off.i));
		off.i += 4;

		while (this.readFaceBitfield(data, off))
		{
			final float tmpFloat = Helpers.BytesToFloatL(data, off.i);
			off.i += 4;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setRepeatV(tmpFloat);
		}
		// #endregion RepeatV

		// #region OffsetU
		this.defaultTexture.setOffsetU(Helpers.TEOffsetFloat(data, off.i));
		off.i += 2;

		while (this.readFaceBitfield(data, off))
		{
			final float tmpFloat = Helpers.TEOffsetFloat(data, off.i);
			off.i += 2;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setOffsetU(tmpFloat);
		}
		// #endregion OffsetU

		// #region OffsetV
		this.defaultTexture.setOffsetV(Helpers.TEOffsetFloat(data, off.i));
		off.i += 2;

		while (this.readFaceBitfield(data, off))
		{
			final float tmpFloat = Helpers.TEOffsetFloat(data, off.i);
			off.i += 2;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setOffsetV(tmpFloat);
		}
		// #endregion OffsetV

		// #region Rotation
		this.defaultTexture.setRotation(Helpers.TERotationFloat(data, off.i));
		off.i += 2;

		while (this.readFaceBitfield(data, off))
		{
			final float tmpFloat = Helpers.TERotationFloat(data, off.i);
			off.i += 2;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setRotation(tmpFloat);
		}
		// #endregion Rotation

		// #region Material
		this.defaultTexture.material = data[off.i];
		off.i++;

		while (this.readFaceBitfield(data, off))
		{
			final byte tmpByte = data[off.i];
			off.i++;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).material = tmpByte;
		}
		// #endregion Material

		// #region Media
		this.defaultTexture.media = data[off.i];
		off.i++;

		while (off.i - pos + 1 < length && this.readFaceBitfield(data, off))
		{
			final byte tmpByte = data[off.i];
			off.i++;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).media = tmpByte;
		}
		// #endregion Media

		// #region Glow
		this.defaultTexture.setGlow(Helpers.TEGlowFloat(data, off.i));
		off.i++;

		while (off.i - pos + 4 < length && this.readFaceBitfield(data, off))
		{
			final float tmpFloat = Helpers.TEGlowFloat(data, off.i);
			off.i++;

			for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
				if (0 != (off.faceBits & bit))
					this.createFace(face).setGlow(tmpFloat);
		}
		// #endregion Glow

		// #region MaterialID
		if (off.i - pos + 16 <= length)
		{
			this.defaultTexture.setMaterialID(new UUID(data, off.i));
			off.i += 16;

			while (off.i - pos + 16 < length && this.readFaceBitfield(data, off))
			{
				final UUID tmpUUID = new UUID(data, off.i);
				off.i += 16;

				for (int face = 0, bit = 1; face < off.bitfieldSize; face++, bit <<= 1)
					if (0 != (off.faceBits & bit))
						this.createFace(face).setMaterialID(tmpUUID);
			}
		}
		// #endregion MaterialID
	}

	public byte[] getBytes() throws IOException
	{
		if (null == defaultTexture)
			return Helpers.EmptyBytes;

		final ByteArrayOutputStream memStream = new ByteArrayOutputStream();
		boolean alreadySent;
		int i, j;
		long bitfield;

		// #region Texture
		UUID tempID, defID = this.defaultTexture.getTextureID();
		defID.write(memStream);
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempID = this.faceTextures[i].getTextureID();
				if (null != tempID && !tempID.equals(defID))
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempID.equals(this.faceTextures[j].getTextureID());
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempID.equals(this.faceTextures[j].getTextureID()))
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						tempID.write(memStream);
					}
				}
			}
		}
		memStream.write(0);
		// #endregion Texture

		// #region Color
		// Serialize the color bytes inverted to optimize for zerocoding
		Color4 tempCol;
		final Color4 defCol = this.defaultTexture.getRGBA();
		defCol.write(memStream, true);
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempCol = this.faceTextures[i].getRGBA();
				if (null != tempCol && !tempCol.equals(defCol))
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempCol.equals(this.faceTextures[j].getRGBA());
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempCol.equals(this.faceTextures[j].getRGBA()))
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						// Serialize the color bytes inverted to optimize for zerocoding
						tempCol.write(memStream, true);
					}
				}
			}
		}
		memStream.write(0);
		// #endregion Color

		// #region RepeatU
		float tempFloat, defFloat = this.defaultTexture.getRepeatU();
		memStream.write(Helpers.FloatToBytesL(defFloat));
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempFloat = this.faceTextures[i].getRepeatU();
				if (tempFloat != defFloat)
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempFloat == this.faceTextures[j].getRepeatU();
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempFloat == this.faceTextures[j].getRepeatU())
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.FloatToBytesL(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion RepeatU

		// #region RepeatV
		defFloat = this.defaultTexture.getRepeatV();
		memStream.write(Helpers.FloatToBytesL(defFloat));
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempFloat = this.faceTextures[i].getRepeatV();
				if (tempFloat != defFloat)
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempFloat == this.faceTextures[j].getRepeatV();
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempFloat == this.faceTextures[j].getRepeatV())
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.FloatToBytesL(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion RepeatV

		// #region OffsetU
		defFloat = this.defaultTexture.getOffsetU();
		memStream.write(Helpers.TEOffsetShort(defFloat));
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempFloat = this.faceTextures[i].getOffsetU();
				if (tempFloat != defFloat)
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempFloat == this.faceTextures[j].getOffsetU();
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempFloat == this.faceTextures[j].getOffsetU())
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.TEOffsetShort(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion OffsetU

		// #region OffsetV
		defFloat = this.defaultTexture.getOffsetV();
		memStream.write(Helpers.TEOffsetShort(defFloat));
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempFloat = this.faceTextures[i].getOffsetV();
				if (tempFloat != defFloat)
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempFloat == this.faceTextures[j].getOffsetV();
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempFloat == this.faceTextures[j].getOffsetV())
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.TEOffsetShort(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion OffsetV

		// #region Rotation
		defFloat = this.defaultTexture.getRotation();
		memStream.write(Helpers.TERotationShort(defFloat));
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempFloat = this.faceTextures[i].getRotation();
				if (tempFloat != defFloat)
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempFloat == this.faceTextures[j].getRotation();
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempFloat == this.faceTextures[j].getRotation())
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(Helpers.TERotationShort(tempFloat));
					}
				}
			}
		}
		memStream.write(0);
		// #endregion Rotation

		// #region Material
		memStream.write(this.defaultTexture.material);
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i] && this.faceTextures[i].material != this.defaultTexture.material)
			{
				alreadySent = false;
				j = 0;
				while (!alreadySent && j < i)
				{
					alreadySent = null != faceTextures[j] && this.faceTextures[i].material == this.faceTextures[j].material;
					j++;
				}
				if (!alreadySent)
				{
					bitfield = 1L << j;
					j++;
					while (j < this.numTextures)
					{
						if (null != faceTextures[j] && this.faceTextures[i].material == this.faceTextures[j].material)
							bitfield = 1L << j;
						j++;
					}
					this.writeFaceBitfieldBytes(memStream, bitfield);
					memStream.write(this.faceTextures[i].material);
				}
			}
		}
		memStream.write(0);
		// #endregion Material

		// #region Media
		memStream.write(this.defaultTexture.media);
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i] && this.faceTextures[i].media != this.defaultTexture.media)
			{
				alreadySent = false;
				j = 0;
				while (!alreadySent && j < i)
				{
					alreadySent = null != faceTextures[j] && this.faceTextures[i].media == this.faceTextures[j].media;
					j++;
				}
				if (!alreadySent)
				{
					bitfield = 1L << j;
					j++;
					while (j < this.numTextures)
					{
						if (null != faceTextures[j] && this.faceTextures[i].media == this.faceTextures[j].media)
							bitfield = 1L << j;
						j++;
					}
					this.writeFaceBitfieldBytes(memStream, bitfield);
					memStream.write(this.faceTextures[i].media);
				}
			}
		}
		memStream.write(0);
		// #endregion Media

		// #region Glow
		byte tempByte;
		final byte defByte = Helpers.TEGlowByte(this.defaultTexture.getGlow());
		memStream.write(defByte);
		for (i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
			{
				tempByte = Helpers.TEGlowByte(this.faceTextures[i].getGlow());
				if (tempByte != defByte)
				{
					alreadySent = false;
					j = 0;
					while (!alreadySent && j < i)
					{
						alreadySent = null != faceTextures[j] && tempByte == Helpers.TEGlowByte(this.faceTextures[j].getGlow());
						j++;
					}
					if (!alreadySent)
					{
						bitfield = 1L << j;
						j++;
						while (j < this.numTextures)
						{
							if (null != faceTextures[j] && tempByte == Helpers.TEGlowByte(this.faceTextures[j].getGlow()))
								bitfield = 1L << j;
							j++;
						}
						this.writeFaceBitfieldBytes(memStream, bitfield);
						memStream.write(tempByte);
					}
				}
			}
		}
		// #endregion Glow

		// #region MaterialID
		defID = this.defaultTexture.getMaterialID();
		if (null != defID)
		{
			memStream.write(0);

			defID.write(memStream);
			for (i = 0; i < this.numTextures; i++)
			{
				if (null != faceTextures[i])
				{
					tempID = this.faceTextures[i].getMaterialID();
					if (null != tempID && !tempID.equals(defID))
					{
						alreadySent = false;
						j = 0;
						while (!alreadySent && j < i)
						{
							alreadySent = null != faceTextures[j] && tempID.equals(this.faceTextures[j].getMaterialID());
							j++;
						}
						if (!alreadySent)
						{
							bitfield = 1L << j;
							j++;
							while (j < this.numTextures)
							{
								if (null != faceTextures[j] && tempID.equals(this.faceTextures[j].getMaterialID()))
									bitfield = 1L << j;
								j++;
							}
							this.writeFaceBitfieldBytes(memStream, bitfield);
							tempID.write(memStream);
						}
					}
				}
			}			
		}
		// #endregion MaterialID
		return memStream.toByteArray();
	}

	@Override
	public int hashCode()
	{
		int hashCode = null != defaultTexture ? this.defaultTexture.hashCode() : 0;
		for (int i = 0; i < this.numTextures; i++)
		{
			if (null != faceTextures[i])
				hashCode ^= this.faceTextures[i].hashCode();
		}
		return hashCode;
	}

	@Override
	public String toString()
	{
		String output = Helpers.EmptyString;

		output += "Default Face: " + this.defaultTexture.toString() + Helpers.NewLine;

		for (int i = 0; i < this.faceTextures.length; i++)
		{
			if (null != faceTextures[i])
				output += "Face " + i + ": " + this.faceTextures[i].toString() + Helpers.NewLine;
		}

		return output;
	}

	// #region Helpers
	class Values
	{
		int bitfieldSize;
		int i;
		long faceBits;
	}

	private boolean readFaceBitfield(final byte[] data, final Values pos)
	{
		pos.faceBits = 0;
		pos.bitfieldSize = 0;

		if (pos.i >= data.length)
			return false;

		byte b = 0;
		do
		{
			b = data[pos.i];
			pos.i++;
			pos.faceBits = (pos.faceBits << 7) | (b & 0x7FL);
			pos.bitfieldSize += 7;
		} while (0 != (b & 0x80));

		return (0 != pos.faceBits);
	}

	private int writeFaceBitfieldBytes(final ByteArrayOutputStream memStream, final long bitfield)
	{
		int byteLength = 0;
		long tmpBitfield = bitfield;
		while (0 != tmpBitfield)
		{
			tmpBitfield >>= 7;
			byteLength++;
		}

		if (0 < byteLength)
		{
			int value;
			for (int i = 1; i <= byteLength; i++)
			{
				value = (int)((bitfield >> (7 * (byteLength - i))) & 0x7F);
				if (i < byteLength)
					value |= 0x80;
				memStream.write(value);
			}
			return byteLength;
		}
		memStream.write(0);
		return 1;
	}
}
