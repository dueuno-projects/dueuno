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
package dueuno.elements.controls

import dueuno.elements.Control
import dueuno.types.Type
import groovy.transform.CompileStatic
import org.grails.web.util.WebUtils
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Paths

/**
 * A file-upload control backed by Dropzone.js.
 * <p>
 * Supports drag-and-drop and click-to-browse file selection, configurable accepted MIME types,
 * file count and size limits, optional thumbnail generation, and localised status messages.
 * The value type is {@link dueuno.types.Type#LIST} (a list of uploaded file references).
 * Uploaded files can be retrieved server-side via {@link #getFilename()} and persisted via
 * {@link #save(String, String)}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class Upload extends Control {

    /** i18n key (or literal text) for the drop-zone invitation message. */
    String text

    /** i18n key (or literal text) shown when the control is disabled. */
    String textDisabled

    /** List of accepted MIME types or file extensions (e.g. {@code ["image/*", ".pdf"]}). Empty means all types are accepted. */
    List acceptedFiles

    /** Maximum number of files that can be uploaded; {@code null} means no limit. */
    Integer maxFiles

    /** Maximum size of a single file in megabytes. Defaults to {@code 20} MB. */
    Integer maxFileSize

    /** Maximum file size in megabytes for which a thumbnail is generated. Defaults to {@code 17} MB. */
    Integer maxThumbnailFilesize

    /** Width in pixels of generated thumbnails; {@code null} uses the Dropzone default. */
    Integer thumbnailWidth

    /** Height in pixels of generated thumbnails; {@code null} uses the Dropzone default. */
    Integer thumbnailHeight

    /** When {@code true}, file preview thumbnails are disabled. Defaults to {@code false}. */
    Boolean disablePreviews

    /**
     * Creates an {@code Upload} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code text} ({@link String}, default {@code "control.upload.message"}),
     *             {@code textDisabled} ({@link String}, default {@code "control.upload.disabled"}),
     *             {@code acceptedFiles} ({@link List}),
     *             {@code maxFiles} ({@link Integer}),
     *             {@code maxFileSize} ({@link Integer}, default {@code 20}),
     *             {@code maxThumbnailFilesize} ({@link Integer}, default {@code 17}),
     *             {@code thumbnailWidth} ({@link Integer}),
     *             {@code thumbnailHeight} ({@link Integer}),
     *             {@code disablePreviews} ({@link Boolean}, default {@code false}),
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    Upload(Map args) {
        super(args)

        valueType = Type.LIST

        text = args.text == null ? 'control.upload.message' : args.text
        textDisabled = args.textDisabled == null ? 'control.upload.disabled' : args.textDisabled

        acceptedFiles = args.acceptedFiles as List ?: []
        maxFiles = args.maxFiles as Integer ?: null
        maxFileSize = args.maxFileSize as Integer ?: 20
        maxThumbnailFilesize = args.maxThumbnailFilesize as Integer ?: 17 // 26 MPixel images
        thumbnailWidth = args.thumbnailWidth as Integer
        thumbnailHeight = args.thumbnailHeight as Integer
        disablePreviews = args.disablePreviews as Boolean ?: false

        containerSpecs.multiline = true
    }

    /**
     * Returns the original filename of the file submitted with the current request.
     *
     * @return the uploaded file's original name
     */
    static String getFilename() {
        return WebUtils.retrieveGrailsWebRequest().params._21Upload['filename']
    }

    /**
     * Saves the uploaded file from the current request to the specified directory path.
     * Does nothing if no file was uploaded. The file is saved using the original filename
     * unless {@code newFilename} is provided.
     *
     * @param path        the target directory path (must end with a path separator)
     * @param newFilename optional replacement filename; uses the original filename when {@code null}
     */
    static void save(String path, String newFilename = null) {
        if (!WebUtils.retrieveGrailsWebRequest().params._21Upload) {
            return
        }

        MultipartFile request = WebUtils.retrieveGrailsWebRequest().params._21Upload as MultipartFile
        String pathname = path + (newFilename ?: filename)
        request.transferTo(Paths.get(pathname))
    }

    /**
     * Serialises this control's Dropzone configuration and localised messages to JSON.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this control's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                acceptedFiles: acceptedFiles.join(','),
                maxFiles: maxFiles,
                maxFileSize: maxFileSize,
                maxThumbnailFilesize: maxThumbnailFilesize,
                thumbnailWidth: thumbnailWidth,
                thumbnailHeight: thumbnailHeight,
                disablePreviews: disablePreviews,

                messages: [
                        upload: message(text),
                        disabled: message(textDisabled),
                        tooBig: message('control.upload.file.too.big'),
                        invalidType: message('control.upload.invalid.file.type'),
                        responseError: message('control.upload.response.error'),
                        cancel: message('control.upload.cancel'),
                        cancelConfirmation: message('control.upload.cancel.confirm'),
                        canceled: message('control.upload.canceled'),
                        remove: message('control.upload.remove'),
                        removeConfirmation: message('control.upload.remove.confirm'),
                        maxExceeded: message('control.upload.files.exceeded'),
                ]
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
