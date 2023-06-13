package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.utils.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PresharedKey {
	private final String hash;
	private final String salt;

	public PresharedKey(final String key) throws NoSuchAlgorithmException {
		this.salt = DigestUtils.md5hex(String.valueOf(System.currentTimeMillis() / 1000L).getBytes(StandardCharsets.UTF_8));
		this.hash = DigestUtils.md5hex((key + ":" + this.salt).getBytes(StandardCharsets.UTF_8));
	}

	public PresharedKey(final Entry<List<Entry>> tag) {
		this.salt = EntryUtils.getStr(Folder.getEntry(tag, "salt"));
		this.hash = EntryUtils.getStr(Folder.getEntry(tag, "hash"));
	}

	public boolean validate(final String key) {
		final String hsh = DigestUtils.md5hex((key + ":" + this.salt).getBytes(StandardCharsets.UTF_8));
		return hsh.equals(this.hash);
	}

	public Entry<List<Entry>> save() {
		final Entry<List<Entry>> entries = Folder.getNew("psk");
		entries.value.add(EntryUtils.mkStr("hash", this.hash));
		entries.value.add(EntryUtils.mkStr("salt", this.salt));

		return entries;
	}
}
