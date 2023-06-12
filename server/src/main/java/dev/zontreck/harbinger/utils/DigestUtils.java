package dev.zontreck.harbinger.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {
	public static String md5hex(byte[] toHash) {
		try {
			return new BigInteger(1, MessageDigest.getInstance("MD5").digest(toHash)).toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
