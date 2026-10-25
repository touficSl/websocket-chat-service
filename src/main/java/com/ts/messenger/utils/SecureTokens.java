package com.ts.messenger.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokens {

	private static final SecureRandom RANDOM = new SecureRandom();

	private SecureTokens() {}

	/** URL safe random token with the given entropy (in bytes) */
	public static String generate(int bytes) {
		byte[] buffer = new byte[bytes];
		RANDOM.nextBytes(buffer);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
	}

	/** Constant time comparison, avoids leaking token content through timing */
	public static boolean equals(String expected, String provided) {
		if (expected == null || provided == null)
			return false;
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8));
	}
}
