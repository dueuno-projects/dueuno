/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.commons.utils.image

import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.util.zip.CRC32

class ImageUtilsSpec extends Specification {

    @TempDir
    File directory

    @Unroll
    def 'load applies EXIF orientation #orientation'() {
        given:
        File file = new File(directory, 'image.jpg')
        ImageIO.write(sampleImage(), 'jpg', file)
        BufferedImage original = ImageIO.read(file)
        byte[] jpeg = file.bytes
        byte[] exif = [69, 120, 105, 102, 0, 0] as byte[]
        ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        DataOutputStream output = new DataOutputStream(buffer)
        output.write(jpeg, 0, 2)
        output.writeShort(0xffe1)
        output.writeShort(2 + exif.length + 26)
        output.write(exif)
        output.write(tiffOrientation(orientation))
        output.write(jpeg, 2, jpeg.length - 2)
        file.bytes = buffer.toByteArray()

        when:
        BufferedImage loaded = ImageUtils.load(file.path)

        then:
        loaded.width == width
        loaded.height == height
        pixels(loaded) == indices.collect { int index -> original.getRGB(index % 3, index.intdiv(3)) }

        where:
        orientation | width | height | indices
        1           | 3     | 2      | [0, 1, 2, 3, 4, 5]
        2           | 3     | 2      | [2, 1, 0, 5, 4, 3]
        3           | 3     | 2      | [5, 4, 3, 2, 1, 0]
        4           | 3     | 2      | [3, 4, 5, 0, 1, 2]
        5           | 2     | 3      | [0, 3, 1, 4, 2, 5]
        6           | 2     | 3      | [3, 0, 4, 1, 5, 2]
        7           | 2     | 3      | [5, 2, 4, 1, 3, 0]
        8           | 2     | 3      | [2, 5, 1, 4, 0, 3]
        0           | 3     | 2      | [0, 1, 2, 3, 4, 5]
        9           | 3     | 2      | [0, 1, 2, 3, 4, 5]
    }

    def 'load preserves PNG pixels and transparency without EXIF'() {
        given:
        File file = new File(directory, 'image.png')
        BufferedImage original = sampleImage(true)
        ImageIO.write(original, 'png', file)

        expect:
        pixels(ImageUtils.load(file.path)) == pixels(original)
    }

    def 'load preserves transparency when applying PNG EXIF orientation'() {
        given:
        File file = new File(directory, 'image.png')
        BufferedImage original = sampleImage(true)
        ImageIO.write(original, 'png', file)
        byte[] png = file.bytes
        ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        DataOutputStream output = new DataOutputStream(buffer)
        // Insert an eXIf chunk after the PNG header and IHDR chunk.
        output.write(png, 0, 33)
        byte[] type = 'eXIf'.getBytes('US-ASCII')
        byte[] exif = tiffOrientation(6)
        output.writeInt(exif.length)
        output.write(type)
        output.write(exif)
        CRC32 crc = new CRC32()
        crc.update(type)
        crc.update(exif)
        output.writeInt((int) crc.value)
        output.write(png, 33, png.length - 33)
        file.bytes = buffer.toByteArray()

        when:
        BufferedImage loaded = ImageUtils.load(file.path)

        then:
        loaded.width == 2
        loaded.height == 3
        loaded.colorModel.hasAlpha()
        pixels(loaded) == [3, 0, 4, 1, 5, 2].collect { int index -> original.getRGB(index % 3, index.intdiv(3)) }
    }

    def 'load keeps pixels when the metadata reader does not support the format'() {
        given:
        File file = new File(directory, 'image.wbmp')
        BufferedImage original = new BufferedImage(3, 2, BufferedImage.TYPE_BYTE_BINARY)
        original.setRGB(1, 0, -1)
        assert ImageIO.write(original, 'wbmp', file)

        expect:
        pixels(ImageUtils.load(file.path)) == pixels(original)
    }

    def 'load rejects unsupported image content'() {
        given:
        File file = new File(directory, 'invalid.jpg')
        file.text = 'Not an image'

        when:
        ImageUtils.load(file.path)

        then:
        IOException exception = thrown()
        exception.message.contains(file.path)
    }

    def 'load rejects a missing image'() {
        when:
        ImageUtils.load(new File(directory, 'missing.jpg').path)

        then:
        thrown(IOException)
    }

    private static BufferedImage sampleImage(boolean alpha = false) {
        BufferedImage image = new BufferedImage(3, 2,
            alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB)
        List<Integer> colors = [0xff0000, 0x00ff00, 0x0000ff, 0xffffff, 0x000000, 0x808080]
        colors.eachWithIndex { int color, int index ->
            image.setRGB(index % 3, index.intdiv(3), (int) (color | (alpha ? 0x80000000 : 0xff000000)))
        }
        return image
    }

    private static List<Integer> pixels(BufferedImage image) {
        return (0..<image.height).collectMany { int y ->
            (0..<image.width).collect { int x -> image.getRGB(x, y) }
        }
    }

    private static byte[] tiffOrientation(int orientation) {
        // Little-endian TIFF header and one SHORT entry for tag 0x0112.
        return [73, 73, 42, 0, 8, 0, 0, 0, 1, 0,
                18, 1, 3, 0, 1, 0, 0, 0, orientation, 0, 0, 0,
                0, 0, 0, 0] as byte[]
    }
}
