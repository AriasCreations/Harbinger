package dev.zontreck.harbinger.data.containers;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryType;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.types.PresharedKey;
import dev.zontreck.harbinger.utils.Key;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HTTPServerSettings {
	public static final String TAG_NAME = "http_server";

	public boolean enabled = false;
	public int port = 7768;

	public PresharedKey PSK;

	public HTTPServerSettings() {
	}

	public HTTPServerSettings(Entry<List<Entry>> tag) {
		enabled = EntryUtils.getBool(Folder.getEntry(tag, "enable"));
		port = EntryUtils.getInt(Folder.getEntry(tag, "port"));

		if (Folder.getEntry(tag, "psk").type == EntryType.FOLDER)
			PSK = new PresharedKey(Folder.getEntry(tag, "psk"));
		else {
			try {
				PSK = Key.computeSecuredKey("change_me");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
	}


	public Entry<?> save() {
		Entry<List<Entry>> tag = Folder.getNew(TAG_NAME);
		tag.value.add(EntryUtils.mkBool("enable", enabled));
		tag.value.add(EntryUtils.mkInt("port", port));
		tag.value.add(PSK.save());
		return tag;
	}
}
