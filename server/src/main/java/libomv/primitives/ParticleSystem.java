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

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.types.Color4;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

// Complete structure for the particle system
public class ParticleSystem
{
	// Particle source pattern
	public enum SourcePattern
	{
		;
		// None
		public static final byte None = 0;
		// Drop particles from source position with no force
		public static final byte Drop = 0x01;
		// "Explode" particles in all directions
		public static final byte Explode = 0x02;
		// Particles shoot across a 2D area
		public static final byte Angle = 0x04;
		// Particles shoot across a 3D Cone
		public static final byte AngleCone = 0x08;
		// Inverse of AngleCone (shoot particles everywhere except the 3D cone
		// defined
		public static final byte AngleConeEmpty = 0x10;

		public static byte setValue(final int value)
		{
			return (byte) (value & SourcePattern._mask);
		}

		public static byte getValue(final byte value)
		{
			return (byte) (value & SourcePattern._mask);
		}

		private static final byte _mask = 0x1F;
	}

	// Particle Data Flags
	// [Flags]
	public enum ParticleDataFlags
	{
		;
		// None
		public static final int None = 0;
		// Interpolate color and alpha from start to end
		public static final int InterpColor = 0x001;
		// Interpolate scale from start to end
		public static final int InterpScale = 0x002;
		// Bounce particles off particle sources Z height
		public static final int Bounce = 0x004;
		// velocity of particles is dampened toward the simulators wind
		public static final int Wind = 0x008;
		// Particles follow the source
		public static final int FollowSrc = 0x010;
		// Particles point towards the direction of source's velocity
		public static final int FollowVelocity = 0x020;
		// Target of the particles
		public static final int TargetPos = 0x040;
		// Particles are sent in a straight line
		public static final int TargetLinear = 0x080;
		// Particles emit a glow
		public static final int Emissive = 0x100;
		// used for point/grab/touch
		public static final int Beam = 0x200;
		// continuous ribbon particle</summary>
		public static final int Ribbon = 0x400;
		// particle data contains glow</summary>
		public static final int DataGlow = 0x10000;
		// particle data contains blend functions</summary>
		public static final int DataBlend = 0x20000;

		public static int setValue(final int value)
		{
			return (value & ParticleDataFlags._mask);
		}

		public static int getValue(final int value)
		{
			return (value & ParticleDataFlags._mask);
		}

		private static final int _mask = 0x3FF;
	}

	// Particle Flags Enum
	// [Flags]
	public enum ParticleFlags
	{
		;
		// None
		public static final byte None = 0;
		// Acceleration and velocity for particles are relative to the object
		// rotation
		public static final byte ObjectRelative = 0x01;
		// Particles use new 'correct' angle parameters
		public static final byte UseNewAngle = 0x02;

		public static byte setValue(final int value)
		{
			return (byte) (value & ParticleFlags._mask);
		}

		public static byte getValue(final byte value)
		{
			return (byte) (value & ParticleFlags._mask);
		}

		private static final byte _mask = 3;
	}

	public enum BlendFunc
	{
	    One,
	    Zero,
	    DestColor,
	    SourceColor,
	    OneMinusDestColor,
	    OneMinusSourceColor,
	    DestAlpha,
	    SourceAlpha,
	    OneMinusDestAlpha,
	    OneMinusSourceAlpha
	}

	public int CRC;
	// Particle Flags
	// There appears to be more data packed in to this area
	// for many particle systems. It doesn't appear to be flag values
	// and serialization breaks unless there is a flag for every
	// possible bit so it is left as an unsigned integer
	public int PartFlags;
	// {@link T:SourcePattern} pattern of particles
	public byte Pattern;
	// A <see langword="float"/> representing the maximimum age (in seconds)
	// particle will be displayed
	// Maximum value is 30 seconds
	public float MaxAge;
	// A <see langword="float"/> representing the number of seconds,
	// from when the particle source comes into view,
	// or the particle system's creation, that the object will emits particles;
	// after this time period no more particles are emitted
	public float StartAge;
	// A <see langword="float"/> in radians that specifies where particles will
	// not be created
	public float InnerAngle;
	// A <see langword="float"/> in radians that specifies where particles will
	// be created
	public float OuterAngle;
	// A <see langword="float"/> representing the number of seconds between
	// burts.
	public float BurstRate;
	// A <see langword="float"/> representing the number of meters
	// around the center of the source where particles will be created.
	public float BurstRadius;
	// A <see langword="float"/> representing in seconds, the minimum speed
	// between bursts of new
	// particles being emitted
	public float BurstSpeedMin;
	// A <see langword="float"/> representing in seconds the maximum speed of
	// new particles being emitted.
	public float BurstSpeedMax;
	// A <see langword="byte"/> representing the maximum number of particles
	// emitted per burst
	public byte BurstPartCount;
	// A <see cref="T:Vector3"/> which represents the velocity (speed) from the
	// source which particles are emitted
	public Vector3 AngularVelocity;
	// A <see cref="T:Vector3"/> which represents the Acceleration from the
	// source which particles are emitted
	public Vector3 PartAcceleration;
	// The <see cref="T:UUID"/> Key of the texture displayed on the particle
	public UUID Texture;
	// The <see cref="T:UUID"/> Key of the specified target object or avatar
	// particles will follow
	public UUID Target;
	// Flags of particle from {@link T:ParticleDataFlags}
	public int PartDataFlags;
	// Max Age particle system will emit particles for
	public float PartMaxAge;
	// The <see cref="T:Color4"/> the particle has at the beginning of its
	// lifecycle
	public Color4 PartStartColor;
	// The <see cref="T:Color4"/> the particle has at the ending of its
	// lifecycle
	public Color4 PartEndColor;
	// A <see langword="float"/> that represents the starting X size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartStartScaleX;
	// A <see langword="float"/> that represents the starting Y size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartStartScaleY;
	// A <see langword="float"/> that represents the ending X size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartEndScaleX;
	// A <see langword="float"/> that represents the ending Y size of the
	// particle
	// Minimum value is 0, maximum value is 4
	public float PartEndScaleY;
	// A <see langword="float"/> that represents the start glow value
	// Minimum value is 0, maximum value is 1
	public float PartStartGlow;
	// A <see langword="float"/> that represents the end glow value
	// Minimum value is 0, maximum value is 1
	public float PartEndGlow;

	// OpenGL blend function to use at particle source
	public byte BlendFuncSource;
	// OpenGL blend function to use at particle destination
	public byte BlendFuncDest;

	public final byte MaxDataBlockSize = 98;
	public final byte LegacyDataBlockSize = 86;
	public final byte SysDataSize = 68;
	public final byte PartDataSize = 18;

	// Can this particle system be packed in a legacy compatible way
	// True if the particle system doesn't use new particle system features
	public boolean IsLegacyCompatible()
	{
	    return !this.HasGlow() && !this.HasBlendFunc();
	}

	public boolean HasGlow()
	{
	    return 0.0f < PartStartGlow || 0.0f < PartEndGlow;
	}

	public boolean HasBlendFunc()
	{
	    return this.BlendFuncSource != BlendFunc.SourceAlpha.ordinal() || this.BlendFuncDest != BlendFunc.OneMinusSourceAlpha.ordinal();
	}

	public ParticleSystem()
	{
		this.init();
	}

	public ParticleSystem(final OSD osd)
	{
		this.fromOSD(osd);
	}

	/**
	 * Decodes a byte[] array into a ParticleSystem Object
	 * 
	 * @param bytes ParticleSystem object
	 * @param pos Start position for BitPacker
	 */
	public ParticleSystem(final byte[] bytes, int pos)
	{
		this.PartStartGlow = 0.0f;
		this.PartEndGlow = 0.0f;
		this.BlendFuncSource = (byte)BlendFunc.SourceAlpha.ordinal();
		this.BlendFuncDest = (byte)BlendFunc.OneMinusSourceAlpha.ordinal();

		this.CRC = this.PartFlags = 0;
		this.Pattern = SourcePattern.None;
		this.MaxAge = this.StartAge = this.InnerAngle = this.OuterAngle = this.BurstRate = this.BurstRadius = this.BurstSpeedMin =

				this.BurstSpeedMax = 0.0f;
		this.BurstPartCount = 0;
		this.AngularVelocity = this.PartAcceleration = Vector3.Zero;
		this.Texture = this.Target = UUID.Zero;
		this.PartDataFlags = ParticleDataFlags.None;
		this.PartMaxAge = 0.0f;
		this.PartStartColor = this.PartEndColor = Color4.Black;
		this.PartStartScaleX = this.PartStartScaleY = this.PartEndScaleX = this.PartEndScaleY = 0.0f;

		final int size = bytes.length - pos;

		if (LegacyDataBlockSize == size)
        {
			pos += this.unpackSystem(bytes, pos);
			pos += this.unpackLegacyData(bytes, pos);
        }
		else if (LegacyDataBlockSize < size && MaxDataBlockSize >= size)
		{
			final int sysSize = Helpers.BytesToInt32L(bytes, pos);
			pos += 4;
			if (SysDataSize != sysSize) return; // unkown particle system data size
			pos += this.unpackSystem(bytes, pos);
			final int dataSize =  Helpers.BytesToInt32L(bytes, pos);
			pos += 4;
			if (PartDataSize != dataSize) return; // unkown particle data size
			pos += this.unpackLegacyData(bytes, pos);

			if (ParticleDataFlags.DataGlow == (PartDataFlags & ParticleDataFlags.DataGlow))
			{
			    if (2 > bytes.length - pos) return;
				this.PartStartGlow = bytes[pos] / 255.0f;
				pos++;
				this.PartEndGlow = bytes[pos] / 255.0f;
				pos++;
			}

		    if (ParticleDataFlags.DataBlend == (PartDataFlags & ParticleDataFlags.DataBlend))
			{
			    if (2 > bytes.length - pos) return;
				this.BlendFuncSource = bytes[pos];
				pos++;
				this.BlendFuncDest = bytes[pos];
				pos++;
			}
		}
	}
	
	
	private int unpackSystem(final byte[] bytes, int pos)
	{
		this.CRC = (int) Helpers.BytesToUInt32L(bytes, pos);
			pos += 4;
		this.PartFlags = ParticleFlags.setValue((int) Helpers.BytesToUInt32L(bytes, pos));
			pos += 4;
		this.Pattern = SourcePattern.setValue(bytes[pos]);
		pos++;
		this.MaxAge = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
		this.StartAge = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
		this.InnerAngle = Helpers.BytesToFixedL(bytes, pos, false, 3, 5);
		pos++;
		this.OuterAngle = Helpers.BytesToFixedL(bytes, pos, false, 3, 5);
		pos++;
		this.BurstRate = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			if (0.01f > BurstRate) this.BurstRate = 0.01f;
			pos += 2;
		this.BurstRadius = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
		this.BurstSpeedMin = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
		this.BurstSpeedMax = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
			pos += 2;
		this.BurstPartCount = bytes[pos];
		pos++;
		float x = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			float y = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			float z = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
		this.AngularVelocity = new Vector3(x, y, z);
			x = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			y = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
			z = Helpers.BytesToFixedL(bytes, pos, true, 8, 7);
			pos += 2;
		this.PartAcceleration = new Vector3(x, y, z);
		this.Texture = new UUID(bytes, pos);
			pos += 16;
		this.Target = new UUID(bytes, pos);
			pos += 16;
			return pos;
	}
			
    private int unpackLegacyData(final byte[] bytes, int pos)
    {
		this.PartDataFlags = ParticleDataFlags.setValue((int) Helpers.BytesToUInt32L(bytes, pos));
		pos += 4;
		this.PartMaxAge = Helpers.BytesToFixedL(bytes, pos, false, 8, 8);
		pos += 2;
		this.PartStartColor = new Color4(bytes, pos, false);
		pos += 4;
		this.PartEndColor = new Color4(bytes, pos, false);
		pos += 4;
		this.PartStartScaleX = Helpers.BytesToFixedL(bytes, pos, false, 3, 5);
		pos++;
		this.PartStartScaleY = Helpers.BytesToFixedL(bytes, pos, false, 3, 5);
		pos++;
		this.PartEndScaleX = Helpers.BytesToFixedL(bytes, pos, false, 3, 5);
		pos++;
		this.PartEndScaleY = Helpers.BytesToFixedL(bytes, pos, false, 3, 5);
		pos++;
		return pos;
    }

	public ParticleSystem(final ParticleSystem particleSys)
	{
		this.CRC = particleSys.CRC;
		this.PartFlags = particleSys.PartFlags;
		this.Pattern = particleSys.Pattern;
		this.MaxAge = particleSys.MaxAge;
		this.StartAge = particleSys.StartAge;
		this.InnerAngle = particleSys.InnerAngle;
		this.OuterAngle = particleSys.OuterAngle;
		this.BurstRate = particleSys.BurstRate;
		this.BurstRadius = particleSys.BurstRadius;
		this.BurstSpeedMin = particleSys.BurstSpeedMin;
		this.BurstSpeedMax = particleSys.BurstSpeedMax;
		this.BurstPartCount = particleSys.BurstPartCount;
		this.AngularVelocity = new Vector3(particleSys.AngularVelocity);
		this.PartAcceleration = new Vector3(particleSys.PartAcceleration);
		this.Texture = particleSys.Texture;
		this.Target = particleSys.Target;
		this.PartDataFlags = particleSys.PartDataFlags;
		this.PartMaxAge = particleSys.PartMaxAge;
		this.PartStartColor = new Color4(particleSys.PartStartColor);
		this.PartEndColor = new Color4(particleSys.PartEndColor);
		this.PartStartScaleX = particleSys.PartStartScaleX;
		this.PartStartScaleY = particleSys.PartStartScaleY;
		this.PartEndScaleX = particleSys.PartEndScaleX;
		this.PartEndScaleY = particleSys.PartEndScaleY;
	}

	private void init()
	{
		this.CRC = 0;
		this.PartFlags = ParticleFlags.None;
		this.Pattern = SourcePattern.None;
		this.MaxAge = this.StartAge = this.InnerAngle = this.OuterAngle = this.BurstRate = this.BurstRadius = this.BurstSpeedMin = this.BurstSpeedMax = 0.0f;
		this.BurstPartCount = 0;
		this.AngularVelocity = this.PartAcceleration = Vector3.Zero;
		this.Texture = this.Target = UUID.Zero;
		this.PartDataFlags = ParticleDataFlags.None;
		this.PartMaxAge = 0.0f;
		this.PartStartColor = this.PartEndColor = Color4.Black;
		this.PartStartScaleX = this.PartStartScaleY = this.PartEndScaleX = this.PartEndScaleY = 0.0f;
	}

	/**
	 * Generate byte[] array from particle data
	 * 
	 * @return Byte array
	 */
	public byte[] getBytes()
	{
		int pos = 0;
		int size = this.LegacyDataBlockSize;

		if (!this.IsLegacyCompatible())
			size += 8; // two new ints for size
		if (this.HasGlow())
			size += 2; // two bytes for start and end glow
		if (this.HasBlendFunc())
			size += 2; // two bytes for start and end blend function

        final byte[] bytes = new byte[size];
        if (this.IsLegacyCompatible())
       	{
       	    pos += this.packSystemBytes(bytes, pos);
       	    pos += this.packLegacyData(bytes, pos);
        }
        else
        {
        	pos += Helpers.UInt32ToBytesL(this.SysDataSize, bytes, pos);
        	pos += this.packSystemBytes(bytes, pos);
        	int partSize = this.PartDataSize;
        	if (this.HasGlow())
        	{
        		partSize += 2; // two bytes for start and end glow
				this.PartDataFlags |= ParticleDataFlags.DataGlow;
        	}
        	if (this.HasBlendFunc())
        	{
        		partSize += 2; // two bytes for start end end blend function
				this.PartDataFlags |= ParticleDataFlags.DataBlend;
        	}
        	pos += Helpers.UInt32ToBytesL(partSize, bytes, pos);
        	pos += this.packLegacyData(bytes, pos);

        	if (this.HasGlow())
        	{
        		bytes[pos] = Helpers.FloatToByte(this.PartStartGlow, 0.0f, 1.0f);
				pos++;
				bytes[pos] = Helpers.FloatToByte(this.PartEndGlow, 0.0f, 1.0f);
				pos++;
			}

        	if (this.HasBlendFunc())
        	{
        		bytes[pos] = Helpers.FloatToByte(this.BlendFuncSource, 0.0f, 1.0f);
				pos++;
				bytes[pos] = Helpers.FloatToByte(this.BlendFuncDest, 0.0f, 1.0f);
				pos++;
			}
        }
        return bytes;
    }

	private int packSystemBytes(final byte[] bytes, int pos)
    {
		pos += Helpers.UInt32ToBytesL(this.CRC, bytes, pos);
		pos += Helpers.UInt32ToBytesL(this.PartFlags, bytes, pos);
		bytes[pos] = this.Pattern;
		pos++;
		pos += Helpers.FixedToBytesL(bytes, pos, this.MaxAge, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, this.StartAge, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, this.InnerAngle, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, this.OuterAngle, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, this.BurstRate, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, this.BurstRadius, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, this.BurstSpeedMin, false, 8, 8);
		pos += Helpers.FixedToBytesL(bytes, pos, this.BurstSpeedMax, false, 8, 8);
		bytes[pos] = this.BurstPartCount;
		pos++;
		pos += Helpers.FixedToBytesL(bytes, pos, this.AngularVelocity.X, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, this.AngularVelocity.Y, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, this.AngularVelocity.Z, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartAcceleration.X, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartAcceleration.Y, true, 8, 7);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartAcceleration.Z, true, 8, 7);
		pos += this.Texture.toBytes(bytes, pos);
		pos += this.Target.toBytes(bytes, pos);
		return pos;
    }
	
	private int packLegacyData(final byte[] bytes, int pos)
    {
		pos += Helpers.UInt32ToBytesL(this.PartDataFlags, bytes, pos);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartMaxAge, false, 8, 8);
		pos += this.PartStartColor.toBytes(bytes, pos);
		pos += this.PartEndColor.toBytes(bytes, pos);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartStartScaleX, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartStartScaleY, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartEndScaleX, false, 3, 5);
		pos += Helpers.FixedToBytesL(bytes, pos, this.PartEndScaleY, false, 3, 5);
		return pos;
	}

	public OSD serialize()
	{
		final OSDMap map = new OSDMap();

		map.put("crc", OSD.FromInteger(this.CRC));
		map.put("part_flags", OSD.FromInteger(this.PartFlags));
		map.put("pattern", OSD.FromInteger(this.Pattern));
		map.put("max_age", OSD.FromReal(this.MaxAge));
		map.put("start_age", OSD.FromReal(this.StartAge));
		map.put("inner_angle", OSD.FromReal(this.InnerAngle));
		map.put("outer_angle", OSD.FromReal(this.OuterAngle));
		map.put("burst_rate", OSD.FromReal(this.BurstRate));
		map.put("burst_radius", OSD.FromReal(this.BurstRadius));
		map.put("burst_speed_min", OSD.FromReal(this.BurstSpeedMin));
		map.put("burst_speed_max", OSD.FromReal(this.BurstSpeedMax));
		map.put("burst_part_count", OSD.FromInteger(this.BurstPartCount));
		map.put("ang_velocity", OSD.FromVector3(this.AngularVelocity));
		map.put("part_acceleration", OSD.FromVector3(this.PartAcceleration));
		map.put("texture", OSD.FromUUID(this.Texture));
		map.put("target", OSD.FromUUID(this.Target));

		map.put("part_data_flags", OSD.FromInteger(this.PartDataFlags));
		map.put("part_max_age", OSD.FromReal(this.PartMaxAge));
		map.put("part_start_color", OSD.FromColor4(this.PartStartColor));
		map.put("part_end_color", OSD.FromColor4(this.PartEndColor));
		map.put("part_start_scale", OSD.FromVector3(new Vector3(this.PartStartScaleX, this.PartStartScaleY, 0.0f)));
		map.put("part_end_scale", OSD.FromVector3(new Vector3(this.PartEndScaleX, this.PartEndScaleY, 0.0f)));

		if (this.HasGlow())
		{
		    map.put("part_start_glow", OSD.FromReal(this.PartStartGlow));
		    map.put("part_end_glow", OSD.FromReal(this.PartEndGlow));
		}

		if (this.HasBlendFunc())
		{
		    map.put("blendfunc_source", OSD.FromInteger(this.BlendFuncSource));
		    map.put("blendfunc_dest", OSD.FromInteger(this.BlendFuncDest));
		}
		return map;
	}

	public void fromOSD(final OSD osd)
	{
		if (osd instanceof final OSDMap map)
		{

			this.CRC = map.get("crc").AsUInteger();
			this.PartFlags = map.get("part_flags").AsUInteger();
			this.Pattern = SourcePattern.setValue(map.get("pattern").AsInteger());
			this.MaxAge = (float) map.get("max_age").AsReal();
			this.StartAge = (float) map.get("start_age").AsReal();
			this.InnerAngle = (float) map.get("inner_angle").AsReal();
			this.OuterAngle = (float) map.get("outer_angle").AsReal();
			this.BurstRate = (float) map.get("burst_rate").AsReal();
			this.BurstRadius = (float) map.get("burst_radius").AsReal();
			this.BurstSpeedMin = (float) map.get("burst_speed_min").AsReal();
			this.BurstSpeedMax = (float) map.get("burst_speed_max").AsReal();
			this.BurstPartCount = (byte) map.get("burst_part_count").AsInteger();
			this.AngularVelocity = map.get("ang_velocity").AsVector3();
			this.PartAcceleration = map.get("part_acceleration").AsVector3();
			this.Texture = map.get("texture").AsUUID();
			this.Target = map.get("target").AsUUID();

			this.PartDataFlags = ParticleDataFlags.setValue(map.get("part_data_flags").AsUInteger());
			this.PartMaxAge = (float) map.get("part_max_age").AsReal();
			this.PartStartColor = map.get("part_start_color").AsColor4();
			this.PartEndColor = map.get("part_end_color").AsColor4();

			final Vector3 ss = map.get("part_start_scale").AsVector3();
			this.PartStartScaleX = ss.X;
			this.PartStartScaleY = ss.Y;

			final Vector3 es = map.get("part_end_scale").AsVector3();
			this.PartEndScaleX = es.X;
			this.PartEndScaleY = es.Y;

			if (map.containsKey("part_start_glow"))
			{
				this.PartStartGlow = (float)map.get("part_start_glow").AsReal();
				this.PartEndGlow = (float)map.get("part_end_glow").AsReal();
			}

			if (map.containsKey("blendfunc_source"))
			{
				this.BlendFuncSource = (byte)map.get("blendfunc_source").AsUInteger();
				this.BlendFuncDest = (byte)map.get("blendfunc_dest").AsUInteger();
			}
		}
		else
		{
			this.init();
		}
	}
}
