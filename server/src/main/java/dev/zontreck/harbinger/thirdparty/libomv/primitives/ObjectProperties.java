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

import java.util.Date;

import dev.zontreck.harbinger.thirdparty.libomv.types.Permissions;
import dev.zontreck.harbinger.thirdparty.libomv.types.SaleType;
import dev.zontreck.harbinger.thirdparty.libomv.types.UUID;
import dev.zontreck.harbinger.thirdparty.libomv.utils.Helpers;

// Extended properties to describe an object
public class ObjectProperties
{
	public UUID ObjectID;
	public Date CreationDate;
	public Permissions Permissions;
	public int OwnershipCost;
	public SaleType SaleType;
	public int SalePrice;
	public byte AggregatePerms;
	public byte AggregatePermTextures;
	public byte AggregatePermTexturesOwner;
	public Primitive.ObjectCategory Category;
	public short InventorySerial;
	public UUID ItemID;
	public UUID FolderID;
	public UUID FromTaskID;
	public String Name;
	public String Description;
	public String TouchName;
	public String SitName;
	public UUID[] TextureIDs;

	// Default constructor
	public ObjectProperties()
	{
		this.Name = Helpers.EmptyString;
		this.Description = Helpers.EmptyString;
		this.TouchName = Helpers.EmptyString;
		this.SitName = Helpers.EmptyString;
	}

	public ObjectProperties(final ObjectProperties p)
	{
		this.ObjectID = p.ObjectID;
		this.CreationDate = p.CreationDate;
		this.Permissions = new Permissions(p.Permissions);
		this.OwnershipCost = p.OwnershipCost;
		this.SaleType = p.SaleType;
		this.SalePrice = p.SalePrice;
		this.AggregatePerms = p.AggregatePerms;
		this.AggregatePermTextures = p.AggregatePermTextures;
		this.AggregatePermTexturesOwner = p.AggregatePermTexturesOwner;
		this.Category = p.Category;
		this.InventorySerial = p.InventorySerial;
		this.ItemID = p.ItemID;
		this.FolderID = p.FolderID;
		this.FromTaskID = p.FromTaskID;
		this.Name = p.Name;
		this.Description = p.Description;
		this.TouchName = p.TouchName;
		this.SitName = p.SitName;
		this.TextureIDs = new UUID[p.TextureIDs.length];
		System.arraycopy(p.TextureIDs, 0, this.TextureIDs, 0, p.TextureIDs.length);
	}

	/**
	 * Set the properties that are set in an ObjectPropertiesFamily packet
	 * 
	 * @param props
	 *             that has been partially filled by an
	 *            ObjectPropertiesFamily packet
	 */
	public void SetFamilyProperties(final ObjectProperties props)
	{
		this.ObjectID = props.ObjectID;
		this.Permissions = props.Permissions;
		this.OwnershipCost = props.OwnershipCost;
		this.SaleType = props.SaleType;
		this.SalePrice = props.SalePrice;
		this.Category = props.Category;
		this.Name = props.Name;
		this.Description = props.Description;
	}

	public byte[] GetTextureIDBytes()
	{
		if (null == TextureIDs || 0 == TextureIDs.length)
			return Helpers.EmptyBytes;

		final byte[] bytes = new byte[16 * this.TextureIDs.length];
		for (int i = 0; i < this.TextureIDs.length; i++)
			this.TextureIDs[i].toBytes(bytes, 16 * i);

		return bytes;
	}
}
