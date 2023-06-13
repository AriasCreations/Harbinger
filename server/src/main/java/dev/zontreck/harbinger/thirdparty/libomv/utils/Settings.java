/**
 * Copyright (c) 2011-2017, Frederick Martian
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
 * - Neither the name of the dev.zontreck.harbinger.thirdparty.libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package dev.zontreck.harbinger.thirdparty.libomv.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDParser;

public class Settings
{
	public class DefaultSetting
	{
		String key;
		Object value;
		
		public DefaultSetting(final String key, final Object value)
		{
			this.key = key;
			this.value = value;
		}
	}
	
	public class SettingsUpdateCallbackArgs implements CallbackArgs
	{
		private final String name;
		private final OSD value;
		
		public String getName()
		{
			return this.name;
		}
		
		public OSD getValue()
		{
			return this.value;
		}
		
		public SettingsUpdateCallbackArgs(final String name, final OSD value)
		{
			this.name = name;
			this.value = value;
		}
	}
	
	private final File settingsPath;
	private final OSDMap settings;
	private OSDMap defaults;
	
	public CallbackHandler<SettingsUpdateCallbackArgs> OnSettingsUpdate = new CallbackHandler<SettingsUpdateCallbackArgs>();
		
	public Settings(final String settingsPath)
	{
		this.settingsPath = new File(System.getProperty("user.home"), settingsPath);
		this.settings = new OSDMap();
	}
	
	protected void setDefaults(final DefaultSetting[] defaults)
	{
		if (null != defaults)
		{
			if (null == this.defaults)
				this.defaults = new OSDMap();
			
			for (final DefaultSetting setting : defaults)
			{
				this.defaults.put(setting.key, OSD.FromObject(setting.value));
			}
		}
	}
	
	protected void load() throws IOException, ParseException
    {
		try
		{
			final Reader reader = new FileReader(this.settingsPath, StandardCharsets.UTF_8);
			try
			{
				for (final Map.Entry<String, OSD> entry : ((OSDMap)OSDParser.deserialize(reader)).entrySet())
				{
					this.settings.put(entry.getKey(), entry.getValue());
				}
				this.OnSettingsUpdate.dispatch(new SettingsUpdateCallbackArgs(null, null));
			}
			finally
			{
				reader.close();
			}
		}
		catch (final FileNotFoundException ex)
		{
			// Catch FileNotFoundException and ignore as this happens whenever we startup without a settings file
		}
    }

	public void save() throws IOException
	{
		OSDMap temp = null;
		if (null != defaults)
		{
			temp = new OSDMap();
			for (final Map.Entry<String, OSD> entry : this.defaults.entrySet())
			{
				if (!this.settings.get(entry.getKey()).equals(entry.getValue()))
				{
					temp.put(entry);
				}
			}
		}
		else
		{
			temp = this.settings;
		}

		if (0 < temp.size())
		{
			final Writer writer = new FileWriter(this.settingsPath, StandardCharsets.UTF_8);
			try
			{
				OSDParser.serialize(writer, temp, OSD.OSDFormat.Notation);
			}
			finally
			{
				writer.close();
			}
		}
	}

	public OSD get(final String name)
	{
		OSD osd = this.settings.get(name);
		if (osd.getType() == OSD.OSDType.Unknown && null != defaults)
			osd = this.defaults.get(name);
		return osd;
	}

	public boolean getBool(final String name)
	{
		return this.get(name).AsBoolean();
	}

	public int getInt(final String name)
	{
		return this.get(name).AsInteger();
	}

	public long getLong(final String name)
	{
		return this.get(name).AsLong();
	}

	public String getString(final String name)
	{
		return this.get(name).AsString();
	}

	public boolean get(final String name, final boolean defValue)
	{
		final OSD osd = this.get(name);
		if (null != osd)
			return osd.AsBoolean();
		return this.putDefault(name, defValue);
	}

	public int get(final String name, final int defValue)
	{
		final OSD osd = this.get(name);
		if (null != osd)
			return osd.AsInteger();
		return this.putDefault(name, defValue);
	}

	public String get(final String name, final String defValue)
	{
		final OSD osd = this.get(name);
		if (null != osd)
			return osd.AsString();
		return this.putDefault(name, defValue);
	}

	public OSD get(final String name, final OSD defValue)
	{
		final OSD osd = this.get(name);
		if (null != osd)
			return osd;
		return this.putDefault(name, defValue);
	}

	public boolean putDefault(final String name, final boolean value)
	{
		return this.putDefault(name, OSD.FromBoolean(value)).AsBoolean();
	}

	public int putDefault(final String name, final int value)
	{
		return this.putDefault(name, OSD.FromInteger(value)).AsInteger();
	}

	public String putDefault(final String name, final String value)
	{
		return this.putDefault(name, OSD.FromString(value)).AsString();
	}

	public OSD putDefault(final String name, final Object value)
	{
		return this.putDefault(name, OSD.FromObject(value));
	}

	private OSD putDefault(final String name, final OSD value)
	{
		if (null == defaults)
			this.defaults = new OSDMap();

		final OSD osd = this.defaults.put(name, value);
		return null == osd ? new OSD() : osd;
	}

	public boolean put(final String name, final boolean value)
	{
		return this.put(name, OSD.FromBoolean(value)).AsBoolean();
	}

	public int put(final String name, final int value)
	{
		return this.put(name, OSD.FromInteger(value)).AsInteger();
	}

	public long put(final String name, final long value)
	{
		return this.put(name, OSD.FromLong(value)).AsLong();
	}

	public String put(final String name, final String value)
	{
		return this.put(name, OSD.FromString(value)).AsString();
	}

	public OSD put(final String name, final Object value)
	{
		return this.put(name, OSD.FromObject(value));
	}

	private OSD put(final String name, final OSD value)
	{
		OSD def = null, osd = null;
		if (null != defaults)
		{
			/* if the default value is equal to the new value, remove a possible settings value */
			def = this.defaults.get(name);
			if (null != def && OSD.OSDType.Unknown != def.getType() && def.equals(value))
				osd = this.settings.remove(name);
			else
				def = null;
		}

		if (null == def)
			osd = this.settings.put(name, value);

		this.OnSettingsUpdate.dispatch(new SettingsUpdateCallbackArgs(name, value));
		return null == osd ? new OSD() : osd;
	}

	public Collection<String> keys()
	{
		final ArrayList<String> values = new ArrayList<String>(this.defaults.keySet());
		for (final String key : this.settings.keySet())
		{
			if (!this.defaults.containsKey(key))
			{
				values.add(key);
			}
		}
		return values;
	}

	public Collection<OSD> values()
	{
		final ArrayList<OSD> values = new ArrayList<OSD>(this.defaults.values());
		for (final Map.Entry<String, OSD> e : this.settings.entrySet())
		{
			if (!this.defaults.containsKey(e.getKey()))
			{
				values.add(e.getValue());
			}
		}
		return values;
	}

}
