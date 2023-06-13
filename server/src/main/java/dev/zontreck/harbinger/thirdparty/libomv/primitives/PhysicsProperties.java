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
 * - Neither the name of the openmetaverse.org or dev.zontreck.harbinger.thirdparty.libomv-java project nor the
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
package dev.zontreck.harbinger.thirdparty.libomv.primitives;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

// Describes physics attributes of the prim
public class PhysicsProperties
{
	// Type of physics representation used for this prim in the simulator
	public enum PhysicsShapeType
	{
		// Use prim physics form this object
		Prim,
		// No physics, prim doesn't collide
		None,
		// Use convex hull represantion of this prim
		ConvexHull;

		public static PhysicsShapeType setValue(final int value)
		{
			if (value <= PhysicsShapeType.ConvexHull.getValue())
				return PhysicsShapeType.values()[value];
			return PhysicsShapeType.Prim;
		}

		public static byte getValue(final PhysicsShapeType value)
		{
			return (byte) value.ordinal();
		}

		public byte getValue()
		{
			return (byte) this.ordinal();
		}
	}

	// Primitive's local ID
	public int LocalID;
	// Density (1000 for normal density)
	public float Density;
	// Friction
	public float Friction;
	// Gravity multiplier (1 for normal gravity)
	public float GravityMultiplier;
	// Type of physics representation of this primitive in the simulator
	public PhysicsShapeType ShapeType;
	// Restitution
	public float Restitution;

	/**
	 * Creates PhysicsProperties from OSD
	 * 
	 * @param osd
	 *            OSDMap with incoming data</param>
	 */
	public PhysicsProperties(final OSD osd)
	{
		if (osd instanceof final OSDMap map)
		{
			this.LocalID = map.get("LocalID").AsUInteger();
			this.Density = (float) map.get("Density").AsReal();
			this.Friction = (float) map.get("Friction").AsReal();
			this.GravityMultiplier = (float) map.get("GravityMultiplier").AsReal();
			this.Restitution = (float) map.get("Restitution").AsReal();
			this.ShapeType = PhysicsShapeType.setValue(map.get("PhysicsShapeType").AsInteger());
		}
	}

	/**
	 * Serializes PhysicsProperties to OSD
	 * 
	 * @returns OSDMap with serialized PhysicsProperties data
	 */
	public OSDMap getOSD()
	{
		final OSDMap map = new OSDMap(6);
		map.put("LocalID", OSD.FromUInteger(this.LocalID));
		map.put("Density", OSD.FromReal(this.Density));
		map.put("Friction", OSD.FromReal(this.Friction));
		map.put("GravityMultiplier", OSD.FromReal(this.GravityMultiplier));
		map.put("Restitution", OSD.FromReal(this.Restitution));
		map.put("PhysicsShapeType", OSD.FromInteger(this.ShapeType.getValue()));
		return map;
	}
}
