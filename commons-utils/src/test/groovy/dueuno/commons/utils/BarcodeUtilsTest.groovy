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

import com.google.zxing.BarcodeFormat
import groovy.test.GroovyTestCase

import java.awt.image.BufferedImage

class BarcodeUtilsTest extends GroovyTestCase {

    void testGenerateEAN13() {
        BufferedImage image = BarcodeUtils.generateEAN13('5901234123457', 220, 80)

        assertBarcodeImage(image, 220, 80)
    }

    void testGenerateUPCA() {
        BufferedImage image = BarcodeUtils.generateUPCA('036000291452', 220, 80)

        assertBarcodeImage(image, 220, 80)
    }

    void testGenerateCode128() {
        BufferedImage image = BarcodeUtils.generateCode128('DUEUNO-123', 220, 80)

        assertBarcodeImage(image, 220, 80)
    }

    void testGenerateITF() {
        BufferedImage image = BarcodeUtils.generateITF('123456', 220, 80)

        assertBarcodeImage(image, 220, 80)
    }

    void testGenerateDataMatrix() {
        BufferedImage image = BarcodeUtils.generateDataMatrix('DUEUNO-123', 120, 120)

        assertBarcodeImage(image, 120, 120)
    }

    void testGenerateQRCode() {
        BufferedImage image = BarcodeUtils.generateQRCode('DUEUNO-123', 120, 120)

        assertBarcodeImage(image, 120, 120)
    }

    void testEncodeRejectsUnsupportedFormat() {
        shouldFail(IllegalArgumentException) {
            BarcodeUtils.encode(BarcodeFormat.AZTEC, 'DUEUNO-123', 120, 120)
        }
    }

    void testGenerateEAN13RejectsInvalidCode() {
        shouldFail(IllegalArgumentException) {
            BarcodeUtils.generateEAN13('ABC', 220, 80)
        }
    }

    private static void assertBarcodeImage(BufferedImage image, Integer width, Integer height) {
        assertNotNull(image)
        assertEquals(width, image.width)
        assertEquals(height, image.height)
        assertTrue(hasDarkPixel(image))
    }

    private static Boolean hasDarkPixel(BufferedImage image) {
        for (Integer y = 0; y < image.height; y++) {
            for (Integer x = 0; x < image.width; x++) {
                if ((image.getRGB(x, y) & 0x00FFFFFF) == 0) {
                    return true
                }
            }
        }

        return false
    }
}
