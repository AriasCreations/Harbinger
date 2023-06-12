package dev.zontreck.harbinger.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DigestUtils {
	public static String md5hex(byte[] toHash) {
		try {
			return new BigInteger(1, MessageDigest.getInstance("MD5").digest(toHash)).toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String base64(byte[] bytes)
	{
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] base64ToString(String b64)
	{
		return Base64.getDecoder().decode(b64);
	}
}
