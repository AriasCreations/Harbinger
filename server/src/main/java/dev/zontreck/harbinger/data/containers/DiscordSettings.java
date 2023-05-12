package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the settings for the Harbinger Discord Bot
 */
public class DiscordSettings
{
	public String BOT_TOKEN;
	public Map<String, String> WEBHOOKS = new HashMap<>();

	public Entry<List<Entry>> save()
	{
		Entry<List<Entry>> tag = Folder.getNew("discord");
		tag.value.add(EntryUtils.mkStr("token", BOT_TOKEN));
		Entry<List<Entry>> hooks = Folder.getNew("hooks");
		for(Map.Entry<String,String> e : WEBHOOKS.entrySet())
		{
			hooks.value.add(EntryUtils.mkStr(e.getKey(), e.getValue()));
		}
		tag.value.add(hooks);


		return tag;
	}


	public DiscordSettings(){}

	public DiscordSettings(Entry<List<Entry>> tag)
	{
		BOT_TOKEN = EntryUtils.getStr(Folder.getEntry(tag, "token"));

		Entry<List<Entry>> hooks = Folder.getEntry(tag, "hooks");
		for(Entry<String> e : hooks.value)
		{
			WEBHOOKS.put(e.name, e.value);
		}

	}
}
