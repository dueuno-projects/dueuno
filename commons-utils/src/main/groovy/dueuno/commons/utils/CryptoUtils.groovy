package dueuno.commons.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.security.SecureRandom

/**
 * Utility class for AES encryption and decryption using AES/GCM/NoPadding.
 * <p>
 * Provides methods to generate AES keys, save/load keys from the filesystem,
 * and encrypt/decrypt strings with optional Additional Authenticated Data (AAD).
 * <p>
 * Usage example:
 * <pre>{@code
 * // Generate a 256-bit AES key
 * byte[] key = CryptoUtils.generateAESKey()
 *
 * // Save key to file
 * CryptoUtils.saveAESKey(key, "secret.key")
 *
 * // Load key from file
 * byte[] loadedKey = CryptoUtils.loadAESKey("secret.key")
 *
 * // Encrypt a message
 * String encrypted = CryptoUtils.encrypt("Hello world", loadedKey, "myAAD")
 *
 * // Decrypt the message
 * String decrypted = CryptoUtils.decrypt(encrypted, loadedKey, "myAAD")
 *}</pre>
 */
@Slf4j
@CompileStatic
class CryptoUtils {

    private static final String CIPHER_ALGORITHM = 'AES/GCM/NoPadding'
    private static final int IV_LENGTH = 12      // standard GCM
    private static final int TAG_LENGTH = 128    // bit

    /**
     * Generates a random AES key of the specified length.
     *
     * @param bytes the length of the key in bytes (default is 32 bytes / 256 bits)
     * @return the generated AES key as a byte array
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * byte[] key = CryptoUtils.generateAESKey(32)
     *}</pre>
     */
    static byte[] generateAESKey(int bytes = 32) {
        byte[] key = new byte[bytes]
        new SecureRandom().nextBytes(key)
        return key
    }

    /**
     * Saves an AES key to a file.
     * <p>
     * If the filesystem supports POSIX, the file permissions are set to be readable and writable only by the owner.
     *
     * @param password the AES key as a byte array
     * @param pathname the path of the file to save the key
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * byte[] key = CryptoUtils.generateAESKey()
     * CryptoUtils.saveAESKey(key, "secret.key")
     *}</pre>
     */
    static void saveAESKey(byte[] password, String pathname) {
        Path path = Paths.get(pathname)
        Files.write(path, password)

        if (!Files.getFileStore(path).supportsFileAttributeView('posix')) {
            log.warn "Non-POSIX filesystem detected. '${pathname}' file permissions were not set. Ensure the file is protected."
            return
        }

        try {
            Set<PosixFilePermission> perms = [
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            ] as Set
            Files.setPosixFilePermissions(path, perms)

        } catch (Exception e) {
            log.warn "Unable to set POSIX permissions: ${e.message}"
        }
    }

    /**
     * Loads an AES key from a file.
     *
     * @param pathname the path of the file containing the AES key
     * @return the AES key as a byte array, or null if an error occurs
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * byte[] key = CryptoUtils.loadAESKey("secret.key")
     *}</pre>
     */
    static byte[] loadAESKey(String pathname) {
        try {
            Path path = Paths.get(pathname)
            byte[] keyStorePassword = Files.readAllBytes(path)
            return keyStorePassword

        } catch (Exception e) {
            log.error "Error loading '${pathname}': ${e.message}"
            log.info LogUtils.logStackTrace(e)
            return null
        }
    }

    /**
     * Encrypts a string using AES/GCM with the provided symmetric key and optional Additional Authenticated Data (AAD).
     *
     * @param value the plaintext string to encrypt
     * @param symmetricKey the AES key to use for encryption
     * @param aad optional Additional Authenticated Data to include in encryption (can be null)
     * @return the Base64-encoded ciphertext including the IV
     * @throws Exception if no key is provided
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * String encrypted = CryptoUtils.encrypt("Hello world", key, "myAAD")
     *}</pre>
     */
    static String encrypt(String value, byte[] symmetricKey, String aad = null) {
        if (!value) return ''
        if (!symmetricKey) throw new Exception("Encryption error: no key provided.")

        byte[] iv = new byte[IV_LENGTH]
        new SecureRandom().nextBytes(iv)

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, 'AES')
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)

        if (aad) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8))
        }

        byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8))

        byte[] combined = new byte[iv.length + encrypted.length]
        System.arraycopy(iv, 0, combined, 0, iv.length)
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length)

        return Base64.encoder.encodeToString(combined)
    }

    /**
     * Decrypts a Base64-encoded AES/GCM ciphertext with the provided symmetric key and optional Additional Authenticated Data (AAD).
     *
     * @param encryptedValue the Base64-encoded ciphertext to decrypt
     * @param symmetricKey the AES key to use for decryption
     * @param aad optional Additional Authenticated Data to validate (must match the one used for encryption, can be null)
     * @return the decrypted plaintext string
     * @throws Exception if no key is provided
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * String decrypted = CryptoUtils.decrypt(encrypted, key, "myAAD")
     *}</pre>
     */
    static String decrypt(String encryptedValue, byte[] symmetricKey, String aad = null) {
        if (!encryptedValue) return ''
        if (!symmetricKey) throw new Exception("Decryption error: no key provided.")

        byte[] data = Base64.decoder.decode(encryptedValue)
        byte[] iv = data[0..<IV_LENGTH] as byte[]
        byte[] encrypted = data[IV_LENGTH..-1] as byte[]

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, 'AES')
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)

        if (aad) {
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8))
        }

        byte[] decrypted = cipher.doFinal(encrypted)
        return new String(decrypted, StandardCharsets.UTF_8)
    }
}
