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
package dueuno.commons.http.client

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity

import java.nio.charset.Charset

/**
 * Builder-style utility for constructing multipart/form-data HTTP requests.
 * <p>
 * Provides a fluent API for adding text, JSON, binary, or file parts
 * to an HTTP request. All fields are encoded in UTF-8 by default.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * def multipart = HttpMultipart.create()
 *     .addText("description", "Upload di test")
 *     .addJson("metadata", [id: 42])
 *     .addFile("file", new File("/tmp/test.pdf"))
 *
 * def response = HttpClient.post(client, "https://example.com/api/upload", multipart)
 *}</pre>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Fluent builder API for ease of use</li>
 *   <li>Support for text, JSON, file, and binary fields</li>
 *   <li>Automatic JSON serialization using {@link JsonOutput}</li>
 *   <li>UTF-8 encoding by default</li>
 * </ul>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class HttpMultipartBody {

    private final MultipartEntityBuilder builder

    /**
     * Private constructor. Initializes the multipart builder with STRICT mode and UTF-8 charset.
     */
    private HttpMultipartBody() {
        builder = MultipartEntityBuilder.create()
        builder.setMode(HttpMultipartMode.STRICT)
        builder.setCharset(Charset.forName("UTF-8"))
    }

    /**
     * Creates a new {@link HttpMultipartBody} builder instance.
     *
     * @return a new {@link HttpMultipartBody} ready for configuration
     */
    static HttpMultipartBody create() {
        return new HttpMultipartBody()
    }

    /**
     * Adds a plain text field to the multipart request.
     *
     * @param name the name of the form field
     * @param value the text value (empty string if null)
     * @return this {@link HttpMultipartBody} instance for chaining
     */
    HttpMultipartBody addText(String name, String value) {
        builder.addTextBody(name, value ?: "", ContentType.TEXT_PLAIN.withCharset("UTF-8"))
        return this
    }

    /**
     * Adds a field to the multipart request, serializing the given object as JSON.
     * <p>
     * This method converts the provided {@code value} to its JSON representation
     * using Groovy's {@link groovy.json.JsonOutput} and adds it to the underlying
     * multipart builder with the specified {@code name} and content type
     * {@code application/json; charset=UTF-8}.
     * </p>
     *
     * @param name the name of the form field to add
     * @param value the object to serialize as JSON; must be serializable by {@link JsonOutput}
     * @return this {@link HttpMultipartBody} instance for chaining
     * @throws Exception if the object cannot be serialized to JSON
     */
    HttpMultipartBody addJson(String name, Object value) {
        try {
            String json = JsonOutput.toJson(value)
            builder.addTextBody(name, json, ContentType.APPLICATION_JSON.withCharset("UTF-8"))
            return this

        } catch (Exception e) {
            throw new Exception("Body of type '${value.getClass()}' is not supported: ${e.message ?: e.cause.message}")
        }
    }

    /**
     * Adds a file field to the multipart request.
     *
     * @param name the name of the form field
     * @param file the {@link File} to upload
     * @param contentType optional {@link ContentType} (defaults to binary)
     * @return this {@link HttpMultipartBody} instance for chaining
     */
    HttpMultipartBody addFile(String name, File file, ContentType contentType = ContentType.DEFAULT_BINARY) {
        builder.addBinaryBody(name, file, contentType, file.name)
        return this
    }

    /**
     * Adds a binary field from a byte array.
     *
     * @param name the name of the form field
     * @param data the raw bytes to send
     * @param filename the name to assign to the uploaded file
     * @param contentType optional {@link ContentType} (defaults to binary)
     * @return this {@link HttpMultipartBody} instance for chaining
     */
    HttpMultipartBody addBytes(String name, byte[] data, String filename, ContentType contentType = ContentType.DEFAULT_BINARY) {
        builder.addBinaryBody(name, data, contentType, filename)
        return this
    }

    /**
     * Builds and returns the Apache {@link HttpEntity} representing this multipart content.
     *
     * @return a fully constructed {@link HttpEntity} ready for an HTTP request
     */
    HttpEntity build() {
        return builder.build()
    }
}
