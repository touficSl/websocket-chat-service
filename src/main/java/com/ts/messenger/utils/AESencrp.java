package com.ts.messenger.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESencrp {

    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[] { 
        '!', 'X', '#', '5', '_', '9', 't', 'S', 'e', '&', 'r', '!', 'V',
        'W', 'j', 'H'
    };

    public static String encrypt(String data) throws Exception {
        if (data == null || data.isEmpty())
            return data;

        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);

        byte[] encVal = c.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encVal);
    }

    public static String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty())
            return encryptedData;

        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);

        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);

        return new String(decValue, StandardCharsets.UTF_8);
    }

    private static Key generateKey() {
        return new SecretKeySpec(keyValue, ALGO);
    }
}
