package dueuno.commons.utils.image

import groovy.transform.CompileStatic

/**
 * Enum representing different image render quality presets.
 * <p>
 * Each quality preset defines the target width, height, and DPI (dots per inch) for rendering images.
 * <p>
 * Usage examples:
 * <pre>{@code
 * RenderQuality quality = RenderQuality.MEDIUM;
 * Integer w = quality.width;  // 1699
 * Integer h = quality.height; // 2200
 * Integer dpi = quality.dpi;  // 200
 *
 * // Iterate through all qualities
 * RenderQuality.values().each { q ->
 *     println("${q.name()} -> ${q.width}x${q.height} @ ${q.dpi} DPI")
 * }
 *}</pre>
 *
 * Presets:
 * <ul>
 *      <li>{@link #LOW} — 595x842, 72 DPI</li>
 *      <li>{@link #MEDIUM} — 992x1403, 120 DPI</li>
 *      <li>{@link #HIGH} — 1654x2339, 200 DPI</li>
 *      <li>{@link #ULTRA} — 2480x3508, 300 DPI</li>
 * </ul>
 */
@CompileStatic
enum RenderQuality {
    /**
     * Low render quality for thumbnails, previews, and VLM ingestion (≈ A4 @ 72 DPI).
     */
    LOW(595, 842, 72),
    /**
     * Balanced render quality for screen viewing and document AI (≈ A4 @ 120 DPI).
     */
    MEDIUM(992, 1403, 120),

    /**
     * High render quality (≈ A4 @ 200 DPI).
     */
    HIGH(1654, 2339, 200),

    /**
     * Ultra render quality (≈ A4 @ 300 DPI).
     */
    ULTRA(2480, 3508, 300)

    /**
     * Target width in pixels for this quality preset.
     */
    final Integer width

    /**
     * Target height in pixels for this quality preset.
     */
    final Integer height

    /**
     * Target DPI (dots per inch) for this quality preset.
     */
    final Integer dpi

    /**
     * Constructor to initialize the render quality preset.
     *
     * @param width the target width in pixels
     * @param height the target height in pixels
     * @param dpi the target DPI (dots per inch)
     */
    RenderQuality(Integer width, Integer height, Integer dpi) {
        this.width = width
        this.height = height
        this.dpi = dpi
    }
}
