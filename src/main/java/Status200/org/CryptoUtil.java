package Status200.org;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {
    // AES-GCM params
    private static final String ALGO = "AES";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;      // 96 bits recommended for GCM
    private static final int TAG_LEN = 128;    // bits
    private static final int KEY_LEN_BYTES = 32; // 256-bit key (32 bytes)

    private static final SecretKeySpec KEY_SPEC = loadKey();

    private static SecretKeySpec loadKey() {
        try {
            String b64 = "REMOVED";//System.getenv("PASSWORD_MANAGER_KEY");
            if (b64 == null || b64.isBlank()) {
                b64 = System.getProperty("password.manager.key"); // fallback
            }
            if (b64 == null || b64.isBlank()) {
                throw new IllegalStateException(
                        "Encryption key not found. Set env var PASSWORD_MANAGER_KEY (Base64-encoded 32 bytes). " +
                                "Generate one with: openssl rand -base64 32"
                );
            }
            byte[] keyBytes = Base64.getDecoder().decode(b64);
            if (keyBytes.length != KEY_LEN_BYTES) {
                throw new IllegalStateException("Invalid key length: expected " + KEY_LEN_BYTES + " bytes (Base64 of 32 bytes).");
            }
            return new SecretKeySpec(keyBytes, ALGO);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load encryption key: " + ex.getMessage(), ex);
        }
    }

    // Encrypts plaintext and returns Base64( iv : ciphertext )
    public static String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LEN, iv);
            cipher.init(Cipher.ENCRYPT_MODE, KEY_SPEC, spec);
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // store as base64(iv) + ":" + base64(ciphertext)
            String encodedIv = Base64.getEncoder().encodeToString(iv);
            String encodedCt = Base64.getEncoder().encodeToString(cipherBytes);
            return encodedIv + ":" + encodedCt;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    // Decrypts the stored value (Base64(iv):Base64(ct)) back to plaintext
    public static String decrypt(String stored) {
        if (stored == null) return null;
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Malformed cipher text");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LEN, iv);
            cipher.init(Cipher.DECRYPT_MODE, KEY_SPEC, spec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, "UTF-8");
        } catch (Exception e) {
            // If decryption fails (e.g., existing plaintext rows), throw so caller can handle
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // helper to generate a new random base64 key (use locally to generate env var)
    public static String generateBase64Key() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGO);
            kg.init(KEY_LEN_BYTES * 8);
            SecretKey key = kg.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

