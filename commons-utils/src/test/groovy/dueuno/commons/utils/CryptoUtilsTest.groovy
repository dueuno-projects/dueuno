/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
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

import groovy.test.GroovyTestCase

import javax.crypto.AEADBadTagException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays

class CryptoUtilsTest extends GroovyTestCase {

    void testGenerateAESKeyUsesRequestedLength() {
        byte[] key = CryptoUtils.generateAESKey(16)

        assertNotNull(key)
        assertEquals(16, key.length)
    }

    void testSaveAndLoadAESKey() {
        Path keyFile = Files.createTempFile('dueuno-crypto-', '.key')
        byte[] key = CryptoUtils.generateAESKey()

        try {
            CryptoUtils.saveAESKey(key, keyFile.toString())

            assertTrue(Arrays.equals(key, CryptoUtils.loadAESKey(keyFile.toString())))

        } finally {
            Files.deleteIfExists(keyFile)
        }
    }

    void testLoadAESKeyReturnsNullForMissingFile() {
        Path keyFile = Files.createTempDirectory('dueuno-crypto-').resolve('missing.key')

        try {
            assertNull(CryptoUtils.loadAESKey(keyFile.toString()))

        } finally {
            Files.deleteIfExists(keyFile.parent)
        }
    }

    void testEncryptAndDecryptWithoutAAD() {
        byte[] key = CryptoUtils.generateAESKey()
        String encryptedValue = CryptoUtils.encrypt('Dueuno secret', key)

        assertNotNull(encryptedValue)
        assertTrue(encryptedValue.length() > 0)
        assertFalse('Dueuno secret' == encryptedValue)
        assertEquals('Dueuno secret', CryptoUtils.decrypt(encryptedValue, key))
    }

    void testEncryptAndDecryptWithAAD() {
        byte[] key = CryptoUtils.generateAESKey()
        String encryptedValue = CryptoUtils.encrypt('Dueuno secret', key, 'tenant-1')

        assertEquals('Dueuno secret', CryptoUtils.decrypt(encryptedValue, key, 'tenant-1'))
    }

    void testEncryptionUsesRandomIV() {
        byte[] key = CryptoUtils.generateAESKey()

        String first = CryptoUtils.encrypt('Dueuno secret', key)
        String second = CryptoUtils.encrypt('Dueuno secret', key)

        assertFalse(first == second)
    }

    void testDecryptRejectsWrongAAD() {
        byte[] key = CryptoUtils.generateAESKey()
        String encryptedValue = CryptoUtils.encrypt('Dueuno secret', key, 'tenant-1')

        shouldFail(AEADBadTagException) {
            CryptoUtils.decrypt(encryptedValue, key, 'tenant-2')
        }
    }

    void testDecryptRejectsWrongKey() {
        byte[] key = CryptoUtils.generateAESKey()
        byte[] wrongKey = CryptoUtils.generateAESKey()
        String encryptedValue = CryptoUtils.encrypt('Dueuno secret', key)

        shouldFail(AEADBadTagException) {
            CryptoUtils.decrypt(encryptedValue, wrongKey)
        }
    }

    void testEncryptReturnsEmptyStringForEmptyValues() {
        byte[] key = CryptoUtils.generateAESKey()

        assertEquals('', CryptoUtils.encrypt('', key))
        assertEquals('', CryptoUtils.encrypt(null, key))
    }

    void testDecryptReturnsEmptyStringForEmptyValues() {
        byte[] key = CryptoUtils.generateAESKey()

        assertEquals('', CryptoUtils.decrypt('', key))
        assertEquals('', CryptoUtils.decrypt(null, key))
    }

    void testEncryptRequiresKey() {
        String message = shouldFail(Exception) {
            CryptoUtils.encrypt('Dueuno secret', null)
        }

        assertEquals('Encryption error: no key provided.', message)
    }

    void testDecryptRequiresKey() {
        String encryptedValue = Base64.encoder.encodeToString('value'.getBytes(StandardCharsets.UTF_8))

        String message = shouldFail(Exception) {
            CryptoUtils.decrypt(encryptedValue, null)
        }

        assertEquals('Decryption error: no key provided.', message)
    }
}
