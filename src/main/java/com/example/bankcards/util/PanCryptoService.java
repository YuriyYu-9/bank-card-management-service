package com.example.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PanCryptoService {

    private static final String ALG = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public PanCryptoService(@Value("${app.pan.secretBase64}") String secretBase64) {
        byte[] raw = Base64.getDecoder().decode(secretBase64);
        if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
            throw new IllegalArgumentException("app.pan.secretBase64 must decode to 16/24/32 bytes (AES-128/192/256)");
        }
        this.key = new SecretKeySpec(raw, ALG);
    }

    public String encrypt(String pan) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));

            byte[] ct = cipher.doFinal(pan.getBytes(StandardCharsets.UTF_8));

            // store: base64(iv).base64(ciphertext)
            return Base64.getEncoder().encodeToString(iv) + "." + Base64.getEncoder().encodeToString(ct);
        } catch (Exception e) {
            throw new IllegalStateException("PAN encryption failed", e);
        }
    }
}
