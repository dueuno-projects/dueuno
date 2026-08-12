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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility class for string manipulation and encoding operations.
 * <p>
 * Provides methods for converting between different naming conventions
 * (camelCase, kebab-case, snake_case, SCREAMING_SNAKE_CASE, PascalCase),
 * removing non-printable characters, truncating, checking numeric values,
 * generating random tokens and hashes, parsing variable strings, converting
 * InputStreams to strings, and base64 encoding/decoding.
 * </p>
 *
 * Author: Gianluca Sartori
 */
@Slf4j
@CompileStatic
class StringUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom()

    // --- Naming convention conversions ---

    /**
     * Converts a camelCase string to kebab-case.
     *
     * @param s the input camelCase string
     * @return the kebab-case representation
     */
    static String camelToKebab(String s) {
        def r = ''; for (c in s) {
            if (c in "A".."Z") {
                r += "-${c.toLowerCase()}"
            } else {
                r += c
            }
        }; r
    }

    /**
     * Converts a kebab-case string to camelCase.
     *
     * @param s the input kebab-case string
     * @return the camelCase representation
     */
    static String kebabToCamel(String s) {
        def p = false; def r = '';
        for (c in s) {
            if (c == '-') {
                p = true; continue
            } else {
                r += p ? c.toUpperCase() : c.toLowerCase(); p = false
            }
        }; r
    }

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param s the input camelCase string
     * @return the snake_case representation
     */
    static String camelToSnake(String s) {
        def r = ''; for (c in s) {
            if (c in "A".."Z") {
                r += "_${c.toLowerCase()}"
            } else {
                r += c
            }
        }; r.drop(1)
    }

    /**
     * Converts a snake_case string to camelCase.
     *
     * @param s the input snake_case string
     * @return the camelCase representation
     */
    static String snakeToCamel(String s) {
        def p = false; def r = '';
        for (c in s) {
            if (c == '_') {
                p = true; continue
            } else {
                r += p ? c.toUpperCase() : c.toLowerCase(); p = false
            }
        }; r
    }

    /**
     * Converts a camelCase string to SCREAMING_SNAKE_CASE.
     *
     * @param s the input camelCase string
     * @return the SCREAMING_SNAKE_CASE representation
     */
    static String camelToScreamingSnake(String s) {
        def r = ''; for (c in s) {
            if (c in "A".."Z") {
                r += "_${c.toUpperCase()}"
            } else {
                r += c.toUpperCase()
            }
        }; r
    }

    /**
     * Converts a SCREAMING_SNAKE_CASE string to camelCase.
     *
     * @param s the input SCREAMING_SNAKE_CASE string
     * @return the camelCase representation
     */
    static String screamingSnakeToCamel(String s) {
        snakeToCamel(s)
    }

    /**
     * Converts a PascalCase string to camelCase.
     *
     * @param s the input PascalCase string
     * @return the camelCase representation
     */
    static String pascalToCamel(String s) {
        return s.uncapitalize()
    }

    /**
     * Converts a camelCase string to PascalCase.
     *
     * @param s the input camelCase string
     * @return the PascalCase representation
     */
    static String camelToPascal(String s) {
        return s.capitalize()
    }

    // --- Utilities ---

    /**
     * Removes non-printable characters from a string.
     *
     * @param s the input string
     * @return string without non-printable characters
     */
    static String removeNonPrintableChars(String s) {
        return s.replaceAll('\\P{Print}', '')
    }

    /**
     * Truncates a string if it exceeds the given length.
     *
     * @param s the input string
     * @param size maximum allowed length
     * @return truncated string if longer than size, otherwise original string
     */
    static String truncateIfLonger(String s, Integer size) {
        return s.size() > size ? s.substring(0, size) : s
    }

    /**
     * Checks if a string contains only numeric characters.
     *
     * @param value input string
     * @return true if string contains only digits, false otherwise
     */
    static Boolean isNumeric(String value) {
        for (c in value) {
            if ('1234567890'.indexOf(c) == -1) {
                return false
            }
        }
        return true
    }

    /**
     * Generates a random token string.
     *
     * @param size length of the token
     * @param alphabet optional list of characters to use
     * @return random token string
     */
    static String generateRandomToken(Integer size = 32, List alphabet = []) {
        List defaultAlphabet = ('A'..'Z') + ('0'..'9') + ('a'..'z') + ['!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', ';', ':', '?', '.', '>']
        List tokenAlphabet = alphabet ?: defaultAlphabet

        return (1..size)
            .collect { tokenAlphabet[SECURE_RANDOM.nextInt(tokenAlphabet.size())] }
            .join('')
    }

    /**
     * Generates an MD5 hash for the given input string.
     *
     * @param input input string
     * @return hexadecimal MD5 hash
     */
    static String generateHash(String input) {
        MessageDigest md5 = MessageDigest.getInstance('MD5')
        md5.update(input.getBytes())
        BigInteger hash = new BigInteger(1, md5.digest())
        return hash.toString(16)
    }

    /**
     * Parses a semicolon-separated variable string into a map.
     *
     * @param variablesString input string, e.g. "key1=value1;key2=value2"
     * @return map of key-value pairs
     */
    static Map parseVariablesString(String variablesString) {
        if (!variablesString) {
            return [:]
        }

        List variableList = variablesString.tokenize(';')
        Map result = [:]

        for (var in variableList) {
            List<String> pair = var.split('=') as List<String>
            if (pair.size() > 0) {
                String key = pair[0].trim()
                String value = pair[1]?.trim()
                result.put(key, value)
            }
        }

        return result
    }

    /**
     * Converts an InputStream to a UTF-8 string.
     *
     * @param inputStream the input stream
     * @return string representation
     */
    static String inputStreamToString(InputStream inputStream) {
        ByteArrayOutputStream result = new ByteArrayOutputStream()
        byte[] buffer = new byte[1024]
        for (int length; (length = inputStream.read(buffer)) != -1;) {
            result.write(buffer, 0, length)
        }

        return result.toString("UTF-8")
    }

    /**
     * Encodes a string in Base64.
     *
     * @param inputString the input string
     * @return Base64 encoded string
     */
    static String base64Encode(String inputString) {
        String encoded = inputString.bytes.encodeBase64().toString()
        return encoded
    }

    /**
     * Decodes a Base64 encoded string.
     *
     * @param encodedString Base64 encoded string
     * @return decoded string
     */
    static String base64Decode(String encodedString) {
        byte[] decoded = encodedString.decodeBase64()
        String decode = new String(decoded)
        return decode
    }

}
