package dev.zontreck.harbinger.data.types;

import dev.zontreck.harbinger.utils.DigestUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;

public class PresharedKey
{
	private String hash;
	private String salt;

	public PresharedKey(String key) throws NoSuchAlgorithmException {
		salt = DigestUtils.md5hex(String.valueOf(System.currentTimeMillis()/1000L).getBytes());
		hash = DigestUtils.md5hex((key+":" + salt).getBytes());
	}
}
