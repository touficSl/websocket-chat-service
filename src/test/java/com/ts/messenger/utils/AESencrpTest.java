package com.ts.messenger.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AESencrpTest {

	private static final String TEST_KEY = Base64.getEncoder().encodeToString(new byte[32]);

	@BeforeAll
	static void setUp() {
		AESencrp.setKeyForTests(TEST_KEY);
	}

	@Test
	void roundTrip() throws Exception {
		String message = "Hello &#128512; room 101";
		String encrypted = AESencrp.encrypt(message);
		assertTrue(encrypted.startsWith("v2:"));
		assertEquals(message, AESencrp.decrypt(encrypted));
	}

	@Test
	void randomIvGivesDifferentCiphertexts() throws Exception {
		assertNotEquals(AESencrp.encrypt("same"), AESencrp.encrypt("same"));
	}

	@Test
	void tamperedCiphertextIsRejected() throws Exception {
		String encrypted = AESencrp.encrypt("secret");
		byte[] raw = Base64.getDecoder().decode(encrypted.substring(3));
		raw[raw.length - 1] ^= 1;
		String tampered = "v2:" + Base64.getEncoder().encodeToString(raw);
		assertThrows(Exception.class, () -> AESencrp.decrypt(tampered));
	}

	@Test
	void legacyRowsAreStillReadable() throws Exception {
		byte[] legacyKey = new byte[] { '!', 'X', '#', '5', '_', '9', 't', 'S', 'e', '&', 'r', '!', 'V', 'W', 'j', 'H' };
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(legacyKey, "AES"));
		String legacy = Base64.getEncoder().encodeToString(cipher.doFinal("old message".getBytes(StandardCharsets.UTF_8)));

		assertEquals("old message", AESencrp.decrypt(legacy));
	}

	@Test
	void legacySampleFromOldCodeDecrypts() throws Exception {
		String sample = "/DGAIKBDppUXAvhBbr7V86J5JKs87BAGUVA97eXforLm4w90f82fVbRceRyAzWNnoEIRXxu0tynwbCrgBSt5Ac7Isq3eNazHE/5AjCEUvlbJCfvLDbpZJYI6w5Q/Tm0hBXxU4KHifxvqGYUHeCBeoli8tg6/1B8f7OHFq1RtBHl1s2a7i/ja4Yj+kFta3BZ0FamOD1gOYfh3TD8HJISa4YAbgKjwDyQz6I8tzgXVGmA=";
		assertTrue(AESencrp.decrypt(sample).startsWith("http"));
	}

	@Test
	void invalidKeysAreRejected() {
		assertThrows(IllegalStateException.class, () -> AESencrp.loadKey(null));
		assertThrows(IllegalStateException.class, () -> AESencrp.loadKey(""));
		assertThrows(IllegalStateException.class, () -> AESencrp.loadKey("not base64 !!"));
		assertThrows(IllegalStateException.class, () -> AESencrp.loadKey(Base64.getEncoder().encodeToString(new byte[16])));
	}
}
