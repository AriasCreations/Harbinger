package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;

import java.util.List;

/**
 * Contains the settings for the Harbinger Discord Bot
 */
public class DiscordSettings
{
	public String BOT_TOKEN;

	public Entry<List<Entry>> save()
	{
		Entry<List<Entry>> tag = Folder.getNew("discord");
		tag.value.add(EntryUtils.mkStr("token", BOT_TOKEN));

		return tag;
	}


	public DiscordSettings(){}

	public DiscordSettings(Entry<List<Entry>> tag)
	{
		BOT_TOKEN = EntryUtils.getStr(Folder.getEntry(tag, "token"));
	}
}
