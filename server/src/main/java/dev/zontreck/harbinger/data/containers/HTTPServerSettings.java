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

	public boolean enabled;
	public int port = 7768;

	public PresharedKey PSK;

	public HTTPServerSettings() {
	}

	public HTTPServerSettings(final Entry<List<Entry>> tag) {
		this.enabled = EntryUtils.getBool(Folder.getEntry(tag, "enable"));
		this.port = EntryUtils.getInt(Folder.getEntry(tag, "port"));

		if (EntryType.FOLDER == Folder.getEntry(tag, "psk").type)
			this.PSK = new PresharedKey(Folder.getEntry(tag, "psk"));
		else {
			try {
				this.PSK = Key.computeSecuredKey("change_me");
			} catch (final NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
	}


	public Entry<?> save() {
		final Entry<List<Entry>> tag = Folder.getNew(HTTPServerSettings.TAG_NAME);
		tag.value.add(EntryUtils.mkBool("enable", this.enabled));
		tag.value.add(EntryUtils.mkInt("port", this.port));
		tag.value.add(this.PSK.save());
		return tag;
	}
}
