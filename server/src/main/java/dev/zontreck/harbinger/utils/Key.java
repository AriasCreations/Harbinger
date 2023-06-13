package dev.zontreck.harbinger.utils;

import dev.zontreck.harbinger.data.types.PresharedKey;

import java.security.NoSuchAlgorithmException;

public enum Key {
	;

	/**
	 * Secures a key for safely storing in a data file
	 *
	 * @param input The key to encode
	 * @return A hash+salt combo key instance
	 */
	public static PresharedKey computeSecuredKey ( final String input ) throws NoSuchAlgorithmException {
		return new PresharedKey ( input );
	}
}
