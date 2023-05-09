package dev.zontreck.harbinger.data.types;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.EntryUtils;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.utils.DigestUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.util.List;

public class PresharedKey
{
	private String hash;
	private String salt;

	public PresharedKey(String key) throws NoSuchAlgorithmException {
		salt = DigestUtils.md5hex(String.valueOf(System.currentTimeMillis()/1000L).getBytes());
		hash = DigestUtils.md5hex((key+":" + salt).getBytes());
	}

	public PresharedKey(Entry<List<Entry>> tag)
	{
		salt = EntryUtils.getStr(Folder.getEntry(tag, "salt"));
		hash = EntryUtils.getStr(Folder.getEntry(tag, "hash"));
	}

	public boolean validate(String key)
	{
		String hsh = DigestUtils.md5hex((key + ":" + salt).getBytes());
		return hsh.equals(hash);
	}

	public Entry<List<Entry>> save()
	{
		Entry<List<Entry>> entries = Folder.getNew("psk");
		entries.value.add(EntryUtils.mkStr("hash", hash));
		entries.value.add(EntryUtils.mkStr("salt", salt));

		return entries;
	}
}
