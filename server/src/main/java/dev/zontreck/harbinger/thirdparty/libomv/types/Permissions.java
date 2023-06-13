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
package dev.zontreck.harbinger.thirdparty.libomv.types;

import java.io.Serializable;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.*;

public class Permissions implements Serializable
{
	private static final long serialVersionUID = 1L;

	// [Flags]
	public enum PermissionMask
	{
		;
		public static final int None = 0;
		public static final int Transfer = 1 << 13;
		public static final int Modify = 1 << 14;
		public static final int Copy = 1 << 15;
		public static final int Export = 1 << 16;
		public static final int Move = 1 << 19;
		public static final int Damage = 1 << 20;
		
		// All does not contain Export, which is special and must be explicitly given
		public static final int All = PermissionMask.Transfer | PermissionMask.Modify | PermissionMask.Copy | PermissionMask.Move | PermissionMask.Damage;

		public static int setValue(final int value)
		{
			return value & PermissionMask._mask;
		}

		public static int getValue(final int value)
		{
			return value;
		}

		private static final int _mask = PermissionMask.Transfer | PermissionMask.Modify | PermissionMask.Copy | PermissionMask.Export | PermissionMask.Move | PermissionMask.Damage;
	}

	// [Flags]
	public enum PermissionWho
	{
		;
		public static final byte Base = 0x01;
		public static final byte Owner = 0x02;
		public static final byte Group = 0x04;
		public static final byte Everyone = 0x08;
		public static final byte NextOwner = 0x10;
		public static final byte All = 0x1F;

		public static byte setValue(final int value)
		{
			return (byte) (value & PermissionWho._mask);
		}

		public static int getValue(final int value)
		{
			return value;
		}

		private static final byte _mask = PermissionWho.All;
	}

	public UUID creatorID;
	public UUID ownerID;
	public UUID lastOwnerID;
	public UUID groupID;

	public boolean isGroupOwned;
	public int BaseMask;
	public int OwnerMask;
	public int GroupMask;
	public int EveryoneMask;
	public int NextOwnerMask;

	
	public Permissions()
	{
		this.BaseMask = 0;
		this.EveryoneMask = 0;
		this.GroupMask = 0;
		this.NextOwnerMask = 0;
		this.OwnerMask = 0;
	}

	public Permissions(final OSD osd)
	{
		Permissions.fromOSD(osd);
	}

	public Permissions(final UUID creator, final UUID owner, final UUID lastOwner, final UUID group, final int baseMask, final int everyoneMask, final int groupMask, final int nextOwnerMask, final int ownerMask)
	{
		this.creatorID = creator;
		this.ownerID = owner;
		this.lastOwnerID = lastOwner;
		this.groupID = group;

		this.isGroupOwned = null == ownerID && null != groupID;

		this.BaseMask = baseMask;
		this.EveryoneMask = everyoneMask;
		this.GroupMask = groupMask;
		this.NextOwnerMask = nextOwnerMask;
		this.OwnerMask = ownerMask;
	}

	public Permissions(final UUID creator, final UUID owner, final UUID lastOwner, final UUID group, final boolean groupOwned, final int baseMask, final int everyoneMask, final int groupMask, final int nextOwnerMask, final int ownerMask)
	{
		this.creatorID = creator;
		this.ownerID = owner;
		this.lastOwnerID = lastOwner;
		this.groupID = group;

		this.isGroupOwned = groupOwned;

		this.BaseMask = baseMask;
		this.EveryoneMask = everyoneMask;
		this.GroupMask = groupMask;
		this.NextOwnerMask = nextOwnerMask;
		this.OwnerMask = ownerMask;
	}

	public Permissions(final Permissions perm)
	{
		this.creatorID = perm.creatorID;
		this.ownerID = perm.ownerID;
		this.lastOwnerID = perm.lastOwnerID;
		this.groupID = perm.groupID;

		this.isGroupOwned = perm.isGroupOwned;

		this.OwnerMask = perm.OwnerMask;
		this.GroupMask = perm.GroupMask;
		this.EveryoneMask = perm.EveryoneMask;
		this.NextOwnerMask = perm.NextOwnerMask;
	}

	public Permissions getNextPermissions(final UUID newOwner, final UUID group)
	{
		final int nextMask = this.NextOwnerMask;

		return new Permissions(this.creatorID, newOwner, this.ownerID, group, this.BaseMask & nextMask, this.EveryoneMask & nextMask, this.GroupMask & nextMask, this.NextOwnerMask,
				this.OwnerMask & nextMask);
	}

	public OSD serialize()
	{
		final OSDMap permissions = new OSDMap(5);
		permissions.put("creator_id", OSD.FromUUID(this.creatorID));
		permissions.put("owner_id", OSD.FromUUID(this.ownerID));
		permissions.put("last_owner_id", OSD.FromUUID(this.lastOwnerID));
		permissions.put("group_id", OSD.FromUUID(this.groupID));
		permissions.put("is_owner_group", OSD.FromBoolean(this.isGroupOwned));
		
		permissions.put("base_mask", OSD.FromInteger(this.BaseMask));
		permissions.put("owner_mask", OSD.FromInteger(this.OwnerMask));
		permissions.put("group_mask", OSD.FromInteger(this.GroupMask));
		permissions.put("everyone_mask", OSD.FromInteger(this.EveryoneMask));
		permissions.put("next_owner_mask", OSD.FromInteger(this.NextOwnerMask));
		return permissions;
	}

	public static Permissions fromOSD(final OSD llsd)
	{
		final Permissions permissions = new Permissions();
		final OSDMap map = (OSDMap) ((llsd instanceof OSDMap) ? llsd : null);

		if (null != map)
		{
			permissions.creatorID = map.get("creator_id").AsUUID();
			permissions.ownerID = map.get("owner_id").AsUUID();
			permissions.lastOwnerID = map.get("last_owner_id").AsUUID();
			permissions.groupID = map.get("group_id").AsUUID();
			permissions.isGroupOwned = map.get("is_owner_group").AsBoolean();

			permissions.BaseMask = map.get("base_mask").AsUInteger();
			permissions.EveryoneMask = map.get("everyone_mask").AsUInteger();
			permissions.GroupMask = map.get("group_mask").AsUInteger();
			permissions.NextOwnerMask = map.get("next_owner_mask").AsUInteger();
			permissions.OwnerMask = map.get("owner_mask").AsUInteger();
		}

		return permissions;
	}

	@Override
	public String toString()
	{
		return String.format("Base: %s, Everyone: %s, Group: %s, NextOwner: %s, Owner: %s", this.BaseMask, this.EveryoneMask,
				this.GroupMask, this.NextOwnerMask, this.OwnerMask);
	}

	@Override
	public int hashCode()
	{
		return this.BaseMask ^ this.EveryoneMask ^ this.GroupMask ^ this.NextOwnerMask ^ this.OwnerMask;
	}

	@Override
	public boolean equals(final Object obj)
	{
		return null != obj & (obj instanceof Permissions) && Permissions.equals(this, (Permissions) obj);
	}

	public boolean equals(final Permissions other)
	{
		return Permissions.equals(this, other);
	}

	public static boolean equals(final Permissions lhs, final Permissions rhs)
	{
		return (lhs.BaseMask == rhs.BaseMask) && (lhs.EveryoneMask == rhs.EveryoneMask)
				&& (lhs.GroupMask == rhs.GroupMask) && (lhs.NextOwnerMask == rhs.NextOwnerMask)
				&& (lhs.OwnerMask == rhs.OwnerMask);
	}

	public static boolean hasPermissions(final int perms, final int checkPerms)
	{
		return (perms & checkPerms) == checkPerms;
	}

	public static final Permissions NoPermissions = new Permissions();
	public static final Permissions FullPermissions = new Permissions(null, null, null, null, PermissionMask.All, PermissionMask.All,
			PermissionMask.All, PermissionMask.All, PermissionMask.All);
}
