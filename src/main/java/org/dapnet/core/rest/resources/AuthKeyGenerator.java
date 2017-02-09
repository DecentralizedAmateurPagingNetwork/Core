package org.dapnet.core.rest.resources;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Authentication key generator implementation.
 * 
 * @author Philipp Thiel
 */
final class AuthKeyGenerator {

	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };
	private final SecureRandom rng;
	private final MessageDigest sha;

	public AuthKeyGenerator() throws NoSuchAlgorithmException {
		rng = SecureRandom.getInstanceStrong();
		sha = MessageDigest.getInstance("SHA-1");
	}

	/**
	 * Generates a random authentication key.
	 * 
	 * @return New authentication key.
	 */
	public String generateKey() {
		byte[] input = new byte[16];
		rng.nextBytes(input);

		return hexEncode(sha.digest(input));
	}

	private static String hexEncode(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; ++i) {
			byte v = data[i];
			sb.append(CHARS[(v & 0xf0) >> 4]);
			sb.append(CHARS[v & 0x0f]);
		}

		return sb.toString();
	}
}
