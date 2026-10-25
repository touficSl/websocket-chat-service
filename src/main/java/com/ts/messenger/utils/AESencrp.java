package com.ts.messenger.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ts.messenger.authentication.Credentials;

/**
 * Encryption at rest for chat messages.
 *
 * New values: AES-256-GCM with a random 96 bit IV, stored as "v2:" + base64(iv || ciphertext || tag).
 * The key comes from MESSENGER_ENCRYPTION_KEY (base64 encoded, 32 bytes).
 *
 * Legacy values (no prefix) were written with AES/ECB and a key that was committed to the
 * source repository. They can still be decrypted so old conversations keep working,
 * but nothing new is ever encrypted with that key.
 */
public class AESencrp {

	private static final String PREFIX = "v2:";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int IV_LENGTH = 12;
	private static final int TAG_LENGTH_BITS = 128;

	/* Decrypt-only key used before v2, kept for backward compatibility with existing rows */
	private static final byte[] LEGACY_KEY = new byte[] {
		'!', 'X', '#', '5', '_', '9', 't', 'S', 'e', '&', 'r', '!', 'V', 'W', 'j', 'H'
	};

	private static final SecureRandom RANDOM = new SecureRandom();

	private static volatile SecretKey key;

	public static String encrypt(String data) throws Exception {
		if (data == null || data.isEmpty())
			return data;

		byte[] iv = new byte[IV_LENGTH];
		RANDOM.nextBytes(iv);

		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
		byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

		ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
		buffer.put(iv).put(encrypted);
		return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
	}

	public static String decrypt(String encryptedData) throws Exception {
		if (encryptedData == null || encryptedData.isEmpty())
			return encryptedData;

		if (!encryptedData.startsWith(PREFIX))
			return decryptLegacy(encryptedData);

		byte[] decoded = Base64.getDecoder().decode(encryptedData.substring(PREFIX.length()));
		if (decoded.length <= IV_LENGTH)
			throw new IllegalArgumentException("Invalid encrypted value");

		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_LENGTH_BITS, decoded, 0, IV_LENGTH));
		byte[] decrypted = cipher.doFinal(decoded, IV_LENGTH, decoded.length - IV_LENGTH);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	/** Fails fast at startup when the key is missing or invalid */
	public static void verifyConfiguration() {
		getKey();
	}

	private static String decryptLegacy(String encryptedData) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(LEGACY_KEY, "AES"));
		byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	private static SecretKey getKey() {
		SecretKey current = key;
		if (current == null) {
			synchronized (AESencrp.class) {
				if (key == null)
					key = loadKey(Credentials.getEncryptionKey());
				current = key;
			}
		}
		return current;
	}

	static SecretKey loadKey(String base64Key) {
		if (base64Key == null || base64Key.trim().isEmpty())
			throw new IllegalStateException(
					"MESSENGER_ENCRYPTION_KEY is not set. Generate one with 'openssl rand -base64 32'.");
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(base64Key.trim());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("MESSENGER_ENCRYPTION_KEY must be base64 encoded.");
		}
		if (keyBytes.length != 32)
			throw new IllegalStateException("MESSENGER_ENCRYPTION_KEY must decode to exactly 32 bytes (AES-256).");
		return new SecretKeySpec(keyBytes, "AES");
	}

	/* Visible for tests */
	static void setKeyForTests(String base64Key) {
		key = loadKey(base64Key);
	}
}
