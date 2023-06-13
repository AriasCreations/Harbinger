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
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

public class MediaEntry
{
	// #region enums
	// Permissions for control of object media
	// [Flags]
	public enum MediaPermission
	{
		;
		public static final byte None = 0;
		public static final byte Owner = 1;
		public static final byte Group = 2;
		public static final byte Anyone = 4;
		public static final byte All = MediaPermission.Owner | MediaPermission.Group | MediaPermission.Anyone;

		public static byte setValue(final int value)
		{
			return (byte) (value & MediaPermission._mask);
		}

		public static byte getValue(final byte value)
		{
			return (byte) (value & MediaPermission._mask);
		}

		private static final byte _mask = MediaPermission.All;
	}

	// Style of cotrols that shold be displayed to the user
	public enum MediaControls
	{
		Standard, Mini;

		public byte getValue()
		{
			return (byte) this.ordinal();
		}

		public static MediaControls setValue(final int value)
		{
			if (0 < value && value < MediaControls.values().length)
				return MediaControls.values()[value];
			return null;
		}

	}

	// #endregion enums

	// Is display of the alternative image enabled
	public boolean EnableAlterntiveImage;

	// Should media auto loop
	public boolean AutoLoop;

	// Shoule media be auto played
	public boolean AutoPlay;

	// Auto scale media to prim face
	public boolean AutoScale;

	// Should viewer automatically zoom in on the face when clicked
	public boolean AutoZoom;

	// Should viewer interpret first click as interaction with the media
	// or when false should the first click be treated as zoom in command
	public boolean InteractOnFirstClick;

	// Style of controls viewer should display when viewer media on this face
	public MediaControls Controls;

	// Starting URL for the media
	public String HomeURL;

	// Currently navigated URL
	public String CurrentURL;

	// Media height in pixes
	public int Height;

	// Media width in pixels
	public int Width;

	// Who can controls the media, flags MediaPermission
	public byte ControlPermissions;

	// Who can interact with the media, flags MediaPermission
	public byte InteractPermissions;

	// Is URL whitelist enabled
	public boolean EnableWhiteList;

	// Array of URLs that are whitelisted
	public String[] WhiteList;

	public MediaEntry()
	{

	}

	public MediaEntry(final OSD osd)
	{
		this.fromOSD(osd);
	}

	/**
	 * Serialize to OSD
	 * 
	 * @return OSDMap with the serialized data
	 */
	public OSDMap Serialize()
	{
		final OSDMap map = new OSDMap();

		map.put("alt_image_enable", OSD.FromBoolean(this.EnableAlterntiveImage));
		map.put("auto_loop", OSD.FromBoolean(this.AutoLoop));
		map.put("auto_play", OSD.FromBoolean(this.AutoPlay));
		map.put("auto_scale", OSD.FromBoolean(this.AutoScale));
		map.put("auto_zoom", OSD.FromBoolean(this.AutoZoom));
		map.put("controls", OSD.FromInteger(this.Controls.getValue()));
		map.put("current_url", OSD.FromString(this.CurrentURL));
		map.put("first_click_interact", OSD.FromBoolean(this.InteractOnFirstClick));
		map.put("height_pixels", OSD.FromInteger(this.Height));
		map.put("home_url", OSD.FromString(this.HomeURL));
		map.put("perms_control", OSD.FromInteger(this.ControlPermissions));
		map.put("perms_interact", OSD.FromInteger(this.InteractPermissions));

		final OSDArray wl = new OSDArray();
		if (null != WhiteList && 0 < WhiteList.length)
		{
			for (int i = 0; i < this.WhiteList.length; i++)
				wl.add(OSD.FromString(this.WhiteList[i]));
		}

		map.put("whitelist", wl);
		map.put("whitelist_enable", OSD.FromBoolean(this.EnableWhiteList));
		map.put("width_pixels", OSD.FromInteger(this.Width));

		return map;
	}

	/**
	 * Deserialize from OSD data
	 * 
	 * @param osd
	 *            Serialized OSD data
	 * @return Deserialized object
	 */
	public void fromOSD(final OSD osd)
	{
		if (osd instanceof final OSDMap map)
		{

			this.EnableAlterntiveImage = map.get("alt_image_enable").AsBoolean();
			this.AutoLoop = map.get("auto_loop").AsBoolean();
			this.AutoPlay = map.get("auto_play").AsBoolean();
			this.AutoScale = map.get("auto_scale").AsBoolean();
			this.AutoZoom = map.get("auto_zoom").AsBoolean();
			this.Controls = MediaControls.setValue(map.get("controls").AsInteger());
			this.CurrentURL = map.get("current_url").AsString();
			this.InteractOnFirstClick = map.get("first_click_interact").AsBoolean();
			this.Height = map.get("height_pixels").AsInteger();
			this.HomeURL = map.get("home_url").AsString();
			this.ControlPermissions = MediaPermission.setValue(map.get("perms_control").AsInteger());
			this.InteractPermissions = MediaPermission.setValue(map.get("perms_interact").AsInteger());

			if (OSD.OSDType.Array == map.get("whitelist").getType())
			{
				final OSDArray wl = (OSDArray) map.get("whitelist");
				if (0 < wl.size())
				{
					this.WhiteList = new String[wl.size()];
					for (int i = 0; i < wl.size(); i++)
					{
						this.WhiteList[i] = wl.get(i).AsString();
					}
				}
			}
			this.EnableWhiteList = map.get("whitelist_enable").AsBoolean();
			this.Width = map.get("width_pixels").AsInteger();
		}
	}
}
