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

import groovy.transform.CompileStatic
import org.apache.hc.core5.http.Header

/**
 * Represents an immutable HTTP response containing status code, message,
 * headers, and a response body. This class provides convenience factory methods
 * to construct successful or error responses, along with helper methods to
 * inspect headers and determine the response status category.
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class HttpResponse {

    /** The HTTP status code returned by the server. */
    final Integer status

    /** The optional message associated with the response (typically for errors). */
    final String message

    /** The list of HTTP headers included in the response. */
    final List<Header> headers

    /** The body of the response, which can be of any type. */
    final Object body

    /**
     * Creates a new HttpResponse instance.
     * This constructor is private; use the factory methods
     * {@link #success(Integer, Header [ ], Object)} or
     * {@link #error(Integer, String, Header [ ], String)}.
     *
     * @param status the HTTP status code
     * @param message the message for error responses
     * @param headers the headers returned by the server
     * @param body the response body
     */
    private HttpResponse(Integer status, String message, Header[] headers, Object body) {
        this.status = status
        this.message = message
        this.headers = headers as List<Header> ?: []
        this.body = body
    }

    /**
     * Creates a successful HTTP response.
     * The message is automatically set to OK.
     *
     * <p>Example:
     * <pre>{@code
     * HttpResponse resp = HttpResponse.success(200, headers, responseBody)
     *}</pre>
     *
     * @param status the response status code
     * @param headers the response headers
     * @param body the response body
     * @return a new HttpResponse instance
     */
    static HttpResponse success(Integer status, Header[] headers, Object body) {
        return new HttpResponse(status, 'OK', headers, body)
    }

    /**
     * Creates an error HTTP response.
     *
     * <p>Example:
     * <pre>{@code
     * HttpResponse resp = HttpResponse.error(404, "Not Found", headers, "No resource found")
     *}</pre>
     *
     * @param status the HTTP status code
     * @param message the error message
     * @param headers the response headers
     * @param body the response body
     * @return a new HttpResponse instance
     */
    static HttpResponse error(Integer status, String message, Header[] headers, Object body) {
        return new HttpResponse(status, message, headers, body)
    }

    /**
     * Searches the response headers for one matching the given name
     * (case-insensitive).
     *
     * @param name the header name
     * @return the matching header or {@code null} if not found
     */
    Header getHeader(String name) {
        return headers.find { it.name.equalsIgnoreCase(name) }
    }

    /**
     * Returns all headers included in the response.
     *
     * @return the list of headers
     */
    List<Header> getHeaders() {
        return headers
    }

    /**
     * Indicates whether the response status code represents success
     * (i.e., in the range 200–299).
     *
     * @return {@code true} if the status is 2xx
     */
    boolean isOk() {
        return status >= 200 && status < 300
    }

    /**
     * Indicates whether the response status code represents an error
     * (i.e., 400 or higher).
     *
     * @return {@code true} if the status is 4xx or 5xx
     */
    boolean isError() {
        return status >= 400
    }

}
