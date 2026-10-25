package com.ts.messenger.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SecureTokensTest {

	@Test
	void tokensAreLongUrlSafeAndUnique() {
		Set<String> tokens = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			String token = SecureTokens.generate(32);
			assertEquals(43, token.length());
			assertTrue(token.matches("^[A-Za-z0-9_-]+$"));
			tokens.add(token);
		}
		assertEquals(1000, tokens.size());
	}

	@Test
	void comparison() {
		assertTrue(SecureTokens.equals("abc", "abc"));
		assertFalse(SecureTokens.equals("abc", "abd"));
		assertFalse(SecureTokens.equals("abc", null));
		assertFalse(SecureTokens.equals(null, null));
	}
}
