/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.commons.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.crypto.SecretKey
import javax.crypto.spec.PBEParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.KeyStore

/**
 * Utility class for managing Java KeyStores.
 * <p>
 * Provides methods to load and save KeyStores from files or strings,
 * create KeyStores, set and get keys, and check for key existence.
 * Supports PKCS12 KeyStores with AES-256 encryption and a fixed salt.
 * </p>
 * Author: Gianluca Sartori
 */
@Slf4j
@CompileStatic
class KeyStoreUtils {

    private static final String KEYSTORE_TYPE = 'PKCS12'
    private static final String PROTECTION_ALGORITHM = 'PBEWithHmacSHA256AndAES_256'
    private static final byte[] FIXED_SALT = 'keystore-utils-salt'.bytes
    private static final int ITERATIONS = 300_000

    /**
     * Loads a KeyStore from a file.
     *
     * @param filename the path to the KeyStore file
     * @param password the password for the KeyStore
     * @return the loaded KeyStore, or a new KeyStore if loading fails
     */
    static KeyStore loadFromFile(String filename, byte[] password) {
        try (FileInputStream is = new FileInputStream(filename)) {
            return load(is, password)

        } catch (Exception e) {
            log.warn "Error loading KeyStore file '${filename}': ${e.message}"
            log.info LogUtils.logStackTrace(e)
            return create(password)
        }
    }

    /**
     * Saves a KeyStore to a file.
     *
     * @param keyStore the KeyStore to save
     * @param password the password to protect the KeyStore
     * @param filename the path to the file
     * @throws IOException if an error occurs during writing
     */
    static void saveToFile(KeyStore keyStore, byte[] password, String filename) {
        try (FileOutputStream os = new FileOutputStream(filename)) {
            save(keyStore, password, os)
        }
    }

    /**
     * Loads a KeyStore from a Base64-encoded string.
     *
     * @param base64String the Base64-encoded KeyStore content
     * @param password the password for the KeyStore
     * @return the loaded KeyStore, or a new KeyStore if loading fails
     */
    static KeyStore loadFromString(String base64String, byte[] password) {
        byte[] data = Base64.decoder.decode(base64String)
        try (InputStream is = new ByteArrayInputStream(data)) {
            return load(is, password)

        } catch (Exception e) {
            log.warn "Error loading KeyStore string: ${e.message}"
            log.info LogUtils.logStackTrace(e)
            return create(password)
        }
    }

    /**
     * Saves a KeyStore as a Base64-encoded string.
     *
     * @param keyStore the KeyStore to save
     * @param password the password to protect the KeyStore
     * @return a Base64 string representing the KeyStore
     */
    static String saveToString(KeyStore keyStore, byte[] password) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        save(keyStore, password, os)
        return Base64.encoder.encodeToString(os.toByteArray())
    }

    /**
     * Creates a new, empty KeyStore.
     *
     * @param password the password to protect the KeyStore
     * @return the new KeyStore
     */
    static KeyStore create(byte[] password) {
        return load(null, password)
    }

    /**
     * Adds a secret key to a KeyStore.
     *
     * @param keyStore the KeyStore to modify
     * @param password the KeyStore password
     * @param name the alias for the key
     * @param value the key value as a string
     */
    static void setKey(KeyStore keyStore, byte[] password, String name, String value) {
        byte[] keyBytes = value.getBytes(StandardCharsets.UTF_8)
        SecretKey keySpec = new SecretKeySpec(keyBytes, 'AES')
        KeyStore.SecretKeyEntry secretKey = new KeyStore.SecretKeyEntry(keySpec)

        keyStore.setEntry(name, secretKey, buildProtection(password))
    }

    /**
     * Retrieves a secret key from a KeyStore.
     *
     * @param keyStore the KeyStore to query
     * @param password the KeyStore password
     * @param name the alias of the key
     * @return the key value as a string, or null if not found
     */
    static String getKey(KeyStore keyStore, byte[] password, String name) {
        KeyStore.SecretKeyEntry entry =
            keyStore.getEntry(name, buildProtection(password)) as KeyStore.SecretKeyEntry
        return entry
            ? new String(entry.secretKey.encoded, StandardCharsets.UTF_8)
            : null
    }

    /**
     * Checks if a KeyStore contains a key with the given alias.
     *
     * @param keyStore the KeyStore to check
     * @param name the alias of the key
     * @return true if the key exists, false otherwise
     */
    static boolean contains(KeyStore keyStore, String name) {
        return keyStore.containsAlias(name)
    }

    /**
     * Loads a KeyStore from an InputStream.
     *
     * @param is the input stream or null to create a new KeyStore
     * @param password the password for the KeyStore
     * @return the loaded or new KeyStore
     * @throws IOException if loading fails
     */
    private static KeyStore load(InputStream is, byte[] password) {
        try {
            char[] pwd = passwordBytesToChars(password)
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            keyStore.load(is, pwd)
            return keyStore

        } catch (Exception e) {
            throw new IOException("Error loading KeyStore: ${e.message}", e)
        }
    }

    /**
     * Saves a KeyStore to an OutputStream.
     *
     * @param keyStore the KeyStore to save
     * @param password the password to protect the KeyStore
     * @param os the output stream
     * @throws IOException if saving fails
     */
    private static void save(KeyStore keyStore, byte[] password, OutputStream os) {
        try {
            char[] pwd = passwordBytesToChars(password)
            keyStore.store(os, pwd)

        } catch (Exception e) {
            throw new IOException("Error saving KeyStore: ${e.message}", e)
        }
    }

    /**
     * Builds a PasswordProtection object for a KeyStore entry.
     *
     * @param password the password
     * @return the KeyStore.PasswordProtection instance
     */
    private static KeyStore.PasswordProtection buildProtection(byte[] password) {
        char[] pwd = passwordBytesToChars(password)
        PBEParameterSpec spec = new PBEParameterSpec(FIXED_SALT, ITERATIONS)
        return new KeyStore.PasswordProtection(pwd, PROTECTION_ALGORITHM, spec)
    }

    /**
     * Converts a byte array password into a char array suitable for KeyStore operations.
     *
     * @param passwordBytes the password bytes
     * @return a char array representing the password
     */
    private static char[] passwordBytesToChars(byte[] passwordBytes) {
        return Base64.encoder.encodeToString(passwordBytes).toCharArray()
    }
}
