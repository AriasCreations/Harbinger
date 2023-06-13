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
public class DiscordSettings {
	public String BOT_TOKEN = "";
	public Map<String, String> WEBHOOKS = new HashMap<>();

	public Entry<List<Entry>> save() {
		final Entry<List<Entry>> tag = Folder.getNew("discord");
		tag.value.add(EntryUtils.mkStr("token", this.BOT_TOKEN));
		final Entry<List<Entry>> hooks = Folder.getNew("hooks");
		for (final Map.Entry<String, String> e : this.WEBHOOKS.entrySet()) {
			hooks.value.add(EntryUtils.mkStr(e.getKey(), e.getValue()));
		}
		tag.value.add(hooks);


		return tag;
	}


	public DiscordSettings() {
	}

	public DiscordSettings(final Entry<List<Entry>> tag) {
		try {

			this.BOT_TOKEN = EntryUtils.getStr(Folder.getEntry(tag, "token"));

			final Entry<List<Entry>> hooks = Folder.getEntry(tag, "hooks");
			for (final Entry<String> e : hooks.value) {
				this.WEBHOOKS.put(e.name, e.value);
			}
		} catch (final Exception e) {
		}

	}
}
