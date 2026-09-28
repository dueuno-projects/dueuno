package dueuno.commons.utils.image

import groovy.transform.CompileStatic

/**
 * Enum representing common image formats.
 * <p>
 * Each enum constant has an associated file extension.
 * <p>
 * Usage examples:
 * <pre>{@code
 * ImageFormat format = ImageFormat.PNG;
 * String ext = format.extension; // "png"
 *
 * // Lookup by extension
 * ImageFormat fromExt = ImageFormat.get("jpg"); // ImageFormat.JPEG
 * ImageFormat unknown = ImageFormat.get("bmp"); // null
 *}</pre>
 *
 * Supported formats:
 * <ul>
 *     <li>{@link #GIF} — extension "gif"</li>
 *     <li>{@link #PNG} — extension "png"</li>
 *     <li>{@link #JPEG} — extension "jpg"</li>
 * </ul>
 */
@CompileStatic
enum ImageFormat {
    /**
     * GIF image format.
     */
    GIF('gif'),

    /**
     * PNG image format.
     */
    PNG('png'),

    /**
     * JPEG image format.
     */
    JPEG('jpg')

    /**
     * The standard file extension for the image format.
     */
    final String extension

    /**
     * Constructor to set the file extension for the image format.
     *
     * @param extension the file extension associated with this format
     */
    ImageFormat(String extension) {
        this.extension = extension
    }

    /**
     * Returns the {@link ImageFormat} corresponding to the given file extension.
     *
     * @param extension the file extension to look up
     * @return the matching {@link ImageFormat}, or null if none matches
     *
     * Example:
     * <pre>{@code
     * ImageFormat format = ImageFormat.get("png"); // ImageFormat.PNG
     * ImageFormat unknown = ImageFormat.get("bmp"); // null
     *}</pre>
     */
    static ImageFormat get(String extension) {
        return values().find { it.extension == extension }
    }
}
