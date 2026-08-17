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

import dueuno.elements.core.Control
import dueuno.types.Type
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.transform.CompileStatic
import org.grails.web.util.WebUtils
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Paths

/**
 * A file-upload control backed by FilePond.
 * <p>
 * Supports drag-and-drop and click-to-browse file selection, single or multiple uploads,
 * configurable accepted MIME types, file count and size limits, and localised status messages.
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

    /** i18n key (or literal text) for FilePond's idle label. */
    String labelIdle

    /** Whether multiple files can be added. Defaults to {@code false}. */
    Boolean allowMultiple

    /** List of accepted MIME types (e.g. {@code ["image/*", "application/pdf"]}). Empty means all types are accepted. */
    List acceptedFileTypes

    /** Maximum number of files that can be uploaded; {@code null} means no limit. */
    Integer maxFiles

    /** Maximum size of a single file, expressed as a FilePond size string (e.g. {@code "20MB"}). */
    String maxFileSize

    /**
     * Creates an {@code Upload} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     * {@code labelIdle} ({@link String}, default {@code "control.upload.message"}),
     * {@code allowMultiple} ({@link Boolean}, default {@code false}),
     * {@code acceptedFileTypes} ({@link List}),
     * {@code maxFiles} ({@link Integer}),
     * {@code maxFileSize} ({@link String}, default {@code "20MB"}),
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    Upload(Map args) {
        super(args)

        valueType = Type.LIST

        labelIdle = args.labelIdle == null ? 'control.upload.message' : args.labelIdle
        allowMultiple = args.allowMultiple as Boolean ?: false
        acceptedFileTypes = args.acceptedFileTypes as List ?: []
        maxFiles = args.maxFiles as Integer ?: null
        maxFileSize = args.maxFileSize as String ?: '20MB'

        containerSpecs.multiline = true
    }

    /**
     * Returns the original filename of the file submitted with the current request.
     *
     * @return the uploaded file's original name
     */
    static String getFilename() {
        GrailsParameterMap params = WebUtils.retrieveGrailsWebRequest().params
        return params._21Upload['filename']
    }

    /**
     * Saves the uploaded file from the current request to the specified directory path.
     * Does nothing if no file was uploaded. The file is saved using the original filename
     * unless {@code newFilename} is provided.
     *
     * @param path the target directory path (must end with a path separator)
     * @param newFilename optional replacement filename; uses the original filename when {@code null}
     */
    static void save(String path, String newFilename = null) {
        GrailsParameterMap params = WebUtils.retrieveGrailsWebRequest().params
        if (!params._21Upload) {
            return
        }

        MultipartFile request = params._21Upload as MultipartFile
        String pathname = path + (newFilename ?: filename)
        request.transferTo(Paths.get(pathname))
    }

    /**
     * Serialises this control's FilePond configuration and localised messages to JSON.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this control's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
            labelIdle        : message(labelIdle),
            allowMultiple    : allowMultiple,
            acceptedFileTypes: acceptedFileTypes,
            maxFiles         : maxFiles,
            maxFileSize      : maxFileSize,

            messages         : [
                tooBig       : message('control.upload.file.too.big'),
                invalidType  : message('control.upload.invalid.file.type'),
                responseError: message('control.upload.response.error'),
                cancel       : message('control.upload.cancel'),
                canceled     : message('control.upload.canceled'),
                remove       : message('control.upload.remove'),
            ]
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
