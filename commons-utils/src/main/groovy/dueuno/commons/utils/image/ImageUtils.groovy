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
package dueuno.commons.utils.image

import dueuno.commons.utils.FileUtils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer

import javax.imageio.ImageIO
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.util.List

/**
 * Utility class providing helper methods for loading, saving, converting,
 * transforming, and rendering images.
 * <p>
 * This class centralizes common image manipulation operations such as:
 * <ul>
 *     <li>Loading and saving images from/to disk</li>
 *     <li>Image resizing and high‑quality scaling</li>
 *     <li>Image rotation</li>
 *     <li>Contrast and grayscale adjustments</li>
 *     <li>PDF preview rendering</li>
 *     <li>Conversion to byte arrays</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Load and save an image</h3>
 * <pre>
 * BufferedImage image = ImageUtils.load("/tmp/input.png")
 * ImageUtils.save(image, "/tmp/output.jpg", ImageUtilsFormat.JPG)
 * </pre>
 *
 * <h3>Resize an image</h3>
 * <pre>
 * BufferedImage resized = ImageUtils.resize(image, 800, 600)
 * </pre>
 *
 * <h3>Scale by width keeping proportions</h3>
 * <pre>
 * BufferedImage scaled = ImageUtils.scaleWidth(image, 400)
 * </pre>
 *
 * <h3>Rotate clockwise</h3>
 * <pre>
 * BufferedImage rotated = ImageUtils.rotate(image, true)
 * </pre>
 *
 * <h3>Generate a PDF preview</h3>
 * <pre>
 * BufferedImage preview = ImageUtils.generatePdfPreview("/tmp/file.pdf", 150)
 * </pre>
 *
 * @author Gianluca Sartori
 */
@Slf4j
@CompileStatic
class ImageUtils {

    /**
     * Loads an image from the given file path.
     *
     * @param pathname The pathname of the file to load
     * @return The loaded {@link BufferedImage}
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage image = ImageUtils.load("/images/photo.png")
     * </pre>
     */
    static BufferedImage load(String pathname) {
        return ImageIO.read(new File(pathname))
    }

    /**
     * Saves a {@link BufferedImage} to disk using the specified format.
     * <p>
     * If the format is not provided, it is inferred from the filename
     * extension.
     *
     * @param image The image to save
     * @param pathname The pathname of the file to save
     * @param format File format, it may be 'gif', 'png' or 'jpg'
     * @throws Exception if the file cannot be written
     *
     * <h3>Example</h3>
     * <pre>
     * ImageUtils.save(image, "/tmp/output.png")
     * ImageUtils.save(image, "/tmp/output.jpg", ImageUtilsFormat.JPG)
     * </pre>
     */
    static File save(BufferedImage image, String pathname, ImageFormat format = getFormatFromFilename(pathname)) {
        File file = new File(pathname)

        try {
            ImageIO.write(image, format.extension, file)

        } catch (IOException e) {
            throw new Exception("Error writing file '${file}': ${e.message}")
        }

        return file
    }

    /**
     * Converts a {@link BufferedImage} into a byte array using
     * the specified image format.
     *
     * @param image The image to convert
     * @param format File format, it may be 'gif', 'png' or 'jpg'
     * @return The encoded image as byte[]
     *
     * <h3>Example</h3>
     * <pre>
     * byte[] data = ImageUtils.toByteArray(image, ImageUtilsFormat.PNG)
     * </pre>
     */
    static byte[] toByteArray(BufferedImage image, ImageFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ImageIO.write(image, format.extension, baos)
        return baos.toByteArray()
    }

    /**
     * Scales an image to the specified width while preserving
     * the aspect ratio.
     *
     * @param image The source image
     * @param width Target width
     * @return The scaled image
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage scaled = ImageUtils.scaleWidth(image, 300)
     * </pre>
     */
    static BufferedImage scaleWidth(BufferedImage image, Integer width) {
        return resize(image, width, -1)
    }

    /**
     * Scales an image to the specified height while preserving
     * the aspect ratio.
     *
     * @param image The source image
     * @param height Target height
     * @return The scaled image
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage scaled = ImageUtils.scaleHeight(image, 200)
     * </pre>
     */
    static BufferedImage scaleHeight(BufferedImage image, Integer height) {
        return resize(image, -1, height)
    }

    /**
     * Resizes an image using smooth scaling.
     * <p>
     * If width or height is set to -1, the dimension is
     * automatically calculated to preserve proportions.
     *
     * @param image The source image
     * @param width Target width
     * @param height Target height (auto‑calculated if -1)
     * @return The resized image
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage resized = ImageUtils.resize(image, 1024, 768)
     * </pre>
     */
    static BufferedImage resize(BufferedImage image, Integer width, Integer height = -1 /* auto calculate height by default */) {
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        return imageToBufferedImage(resizedImage)
    }

    /**
     * Performs high‑quality image resizing using bicubic interpolation
     * and rendering quality hints.
     *
     * @param src Source image
     * @param targetWidth Target width
     * @param targetHeight Target height
     * @return The resized high‑quality image
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage hq = ImageUtils.resizeHQ(image, 1920, 1080)
     * </pre>
     */
    static BufferedImage resizeHQ(BufferedImage src, int targetWidth, int targetHeight = -1) {
        if (targetHeight == -1) {
            targetHeight = Math.round((src.height * targetWidth) / (float) src.width) as Integer
        }

        int type = src.type != 0 ? src.type : BufferedImage.TYPE_INT_RGB
        BufferedImage dst = new BufferedImage(targetWidth, targetHeight, type)


        Graphics2D g2 = dst.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON)


        g2.drawImage(src, 0, 0, targetWidth, targetHeight, null)
        g2.dispose()


        return dst
    }

    /**
     * Rotates an image by 90 degrees.
     *
     * @param bi The source image
     * @param right If true rotates clockwise, otherwise counter‑clockwise
     * @return The rotated image
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage cw  = ImageUtils.rotate(image, true)
     * BufferedImage ccw = ImageUtils.rotate(image, false)
     * </pre>
     */
    static BufferedImage rotate(BufferedImage bi, Boolean right = true) {
        Integer w = bi.width
        Integer h = bi.height
        Integer angle = right ? 90 : -90
        BufferedImage rotated = new BufferedImage(h, w, bi.type)
        Graphics2D g2d = rotated.createGraphics()

        AffineTransform at = new AffineTransform()
        at.translate((h - w) / 2 as double, (w - h) / 2 as double)
        at.rotate(Math.toRadians(angle), w / 2 as double, h / 2 as double)
        g2d.setTransform(at)
        g2d.drawImage(bi, 0, 0, null)
        g2d.dispose()

        return rotated
    }

    /**
     * Resolves the {@link ImageFormat} from a filename
     * extension.
     *
     * @param filename The filename
     * @return The detected {@link ImageFormat}
     *
     * <h3>Example</h3>
     * <pre>
     * ImageUtilsFormat format = ImageUtils.getFormatFromFilename("image.png")
     * </pre>
     */
    static ImageFormat getFormatFromFilename(String filename) {
        String extension = FileUtils.stripExtension(filename)
        return ImageFormat.get(extension)
    }

    /**
     * Converts a generic {@link Image} into a {@link BufferedImage}.
     *
     * @param img The source image
     * @return The converted buffered image
     */
    private static BufferedImage imageToBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img
        }

        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(
            img.getWidth(null), img.getHeight(null),
            BufferedImage.TYPE_INT_RGB)

        Graphics2D graphics2D = bi.createGraphics()
        graphics2D.drawImage(img, 0, 0, null)
        graphics2D.dispose()

        return bi
    }

    /**
     * Converts an image to grayscale.
     *
     * @param src Source image
     * @return Grayscale image
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage gray = ImageUtils.toGrayscale(image)
     * </pre>
     */
    static BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage gray = new BufferedImage(
            src.width, src.height,
            BufferedImage.TYPE_BYTE_GRAY
        )
        Graphics2D g = gray.createGraphics()
        g.drawImage(src, 0, 0, null)
        g.dispose()
        return gray
    }

    /**
     * Increases image contrast using a rescale operation.
     *
     * @param img Source image
     * @param scale Contrast scale factor
     * @param offset Contrast offset
     * @return Image with adjusted contrast
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage contrasted = ImageUtils.increaseContrast(image, 1.2f, -15f)
     * </pre>
     */
    static BufferedImage increaseContrast(BufferedImage img, float scale = 1.15f, float offset = -10f) {
        RescaleOp op = new RescaleOp(scale, offset, null)
        return op.filter(img, null)
    }

    /**
     * Generates a preview image from the first page of a PDF document.
     *
     * @param pathname Path to the PDF file
     * @param renderQuality the desired rendering quality. Defaults to {@link RenderQuality#LOW}.
     * @return A rendered preview as {@link BufferedImage}
     *
     * <h3>Example</h3>
     * <pre>
     * BufferedImage preview = ImageUtils.previewPdf("doc.pdf", RenderQuality.LOW)
     * </pre>
     */
    static BufferedImage previewPdf(String pathname, RenderQuality renderQuality = RenderQuality.LOW) {
        PDDocument pd = PDDocument.load(new File(pathname))
        PDFRenderer pr = new PDFRenderer(pd)
        return pr.renderImageWithDPI(0, renderQuality.dpi)
    }

    /**
     * Renders a PDF document into a list of images, one per page.
     * <p>
     * If the input file is a PDF, each page will be rendered as a PNG image with the
     * resolution and size specified by the {@link RenderQuality} parameter.
     * The output images are named using the pattern:
     * <pre>
     * &lt;original_filename&gt;_page&lt;page_number&gt;.png
     * </pre>
     * If the input file is not a PDF, it is returned as-is in a single-element list.
     * </p>
     *
     * <p><b>Example usage:</b></p>
     * <pre>
     * File pdfFile = new File("document.pdf")
     * List&lt;File&gt; pages = ImageUtils.renderPdf(pdfFile, RenderQuality.MEDIUM)
     *
     * // pages now contains PNG files for each page of the PDF
     * pages.each { println it.path }
     * </pre>
     *
     * @param document the document file to render
     * @param renderQuality the desired rendering quality. Defaults to {@link RenderQuality#LOW}.
     * @return a list of image files corresponding to the pages of the document, or a single-element list containing the original file if it is not a PDF
     * @throws Exception if an error occurs during rendering or image writing
     */
    static List<File> renderPdf(File document, RenderQuality renderQuality = RenderQuality.LOW) {
        ImageFormat format = ImageFormat.PNG
        String filename = FileUtils.removeExtension(document.path)

        if (!isPdf(document)) {
            return [document]
        }

        log.info "Rendering PDF '${document.name}'..."
        PDDocument pd = null
        List<File> result = []
        try {
            pd = PDDocument.load(document)
            PDFRenderer renderer = new PDFRenderer(pd)
            for (int page = 0; page < pd.numberOfPages; page++) {
                String pathname = "${filename}_${page + 1}.${format.extension}"
                BufferedImage image = renderer.renderImageWithDPI(page, renderQuality.dpi)
                File imageFile = save(image, pathname, format)
                result.add(imageFile)
            }
        } finally {
            pd?.close()
        }

        log.info " ...done"
        return result
    }

    static private Boolean isPdf(File file) {
        PDDocument pd = null
        try {
            pd = PDDocument.load(file)
            return true
        } catch (Exception ignored) {
            return false
        } finally {
            pd?.close()
        }
    }

}
