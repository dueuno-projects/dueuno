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
import org.apache.hc.core5.http.message.BasicHeader

/**
 * Represents an HTTP request with support for setting method, URL, headers,
 * query parameters, and body. Instances are created using the static factory
 * methods (e.g., {@code GET()}, {@code POST()}). The class validates URL
 * format and ensures only HTTP/HTTPS schemes are allowed.
 * <p>
 * Example usage:
 * <pre>{@code
 * HttpRequest request = HttpRequest.GET("https://example.com/api")
 *     .header("Accept", "application/json")
 *     .query("search", "keyword")
 *     .body("request body");
 *}</pre>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class HttpRequest {

    /** The HTTP method associated with this request. */
    final HttpMethod method
    /** The base URL of the request (query parameters may be appended). */
    String url

    private final List<Header> headers = []
    private final Map<String, Object> query = [:]
    private Object body

    /**
     * Creates a new HttpRequest with the given HTTP method and URL.
     * The constructor is private; use the static factory methods
     * such as {@code GET()}, {@code POST()}, etc.
     *
     * @param method the HTTP method
     * @param url the request URL
     * @throws IllegalArgumentException if the URL is invalid
     */
    private HttpRequest(HttpMethod method, String url) {
        this.method = method
        setUrl(url)
    }

    /**
     * Sets the request URL after validating its format and scheme.
     * Only HTTP and HTTPS URLs are permitted.
     *
     * <p>Example:
     * <pre>{@code
     * request.setUrl("https://example.com/resource");
     *}</pre>
     *
     * @param url the URL to set
     * @return this request instance
     * @throws IllegalArgumentException if the URL is null, empty, or invalid
     */
    HttpRequest setUrl(String url) {
        if (!url) {
            throw new IllegalArgumentException("URL cannot be null or empty")
        }

        try {
            def uri = new URI(url)
            if (!["http", "https"].contains(uri.scheme?.toLowerCase())) {
                throw new IllegalArgumentException("Invalid URL scheme: ${uri.scheme}. Only HTTP/HTTPS are allowed.")
            }
            this.url = uri.toString()

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: ${url}")
        }

        return this
    }

    /**
     * Returns the full URL for this request, including encoded query parameters
     * if any have been added via {@code query()}.
     *
     * <p>Example:
     * <pre>{@code
     * String fullUrl = request.getUrl();
     *}</pre>
     *
     * @return the base URL optionally followed by a query string
     */
    String getUrl() {
        if (query.isEmpty()) return this.url

        String queryString = query.collect { k, v ->
            "${URLEncoder.encode(k, 'UTF-8')}=${URLEncoder.encode(v.toString(), 'UTF-8')}"
        }.join("&")

        return this.url.contains("?") ? "${this.url}&${queryString}" : "${this.url}?${queryString}"
    }

    /**
     * Creates a GET {@link HttpRequest}.
     * <pre>{@code
     * HttpRequest req = HttpRequest.GET("https://example.com");
     *}</pre>
     */
    static HttpRequest GET(String url) { new HttpRequest(HttpMethod.GET, url) }

    /**
     * Creates a POST {@link HttpRequest}.
     * <pre>{@code
     * HttpRequest req = HttpRequest.POST("https://example.com");
     *}</pre>
     */
    static HttpRequest POST(String url) { new HttpRequest(HttpMethod.POST, url) }

    /**
     * Creates a PUT {@link HttpRequest}.
     */
    static HttpRequest PUT(String url) { new HttpRequest(HttpMethod.PUT, url) }

    /**
     * Creates a DELETE {@link HttpRequest}.
     */
    static HttpRequest DELETE(String url) { new HttpRequest(HttpMethod.DELETE, url) }

    /**
     * Creates a PATCH {@link HttpRequest}.
     */
    static HttpRequest PATCH(String url) { new HttpRequest(HttpMethod.PATCH, url) }

    /**
     * Adds a header to the request.
     *
     * <p>Example:
     * <pre>{@code
     * request.header("Content-Type", "application/json");
     *}</pre>
     *
     * @param name the header name
     * @param value the header value (ignored if null)
     * @return this request instance
     */
    HttpRequest header(String name, String value) {
        if (value != null) {
            headers << new BasicHeader(name, value)
        }

        return this
    }

    /**
     * Returns the header with the specified name, case-insensitive.
     *
     * <p>Example:
     * <pre>{@code
     * Header h = request.getHeader("Accept");
     *}</pre>
     *
     * @param name the header name
     * @return the matching Header or {@code null}
     */
    Header getHeader(String name) {
        return headers.find { it.name.equalsIgnoreCase(name) }
    }

    /**
     * Returns all headers added to this request.
     *
     * @return a list of headers
     */
    List<Header> getHeaders() {
        return headers
    }

    /**
     * Adds a single query parameter to the request.
     * The value is URL-encoded when building the final URL.
     *
     * <p>Example:
     * <pre>{@code
     * request.query("page", 2);
     *}</pre>
     *
     * @param name the parameter name
     * @param value the parameter value (ignored if null)
     * @return this request instance
     */
    HttpRequest query(String name, Object value) {
        if (value != null) {
            query[name] = value
        }

        return this
    }

    /**
     * Adds multiple query parameters to the request.
     *
     * <p>Example:
     * <pre>{@code
     * request.query([page: 1, size: 20]);
     *}</pre>
     *
     * @param params a map of parameter names and values
     * @return this request instance
     */
    HttpRequest query(Map<String, Object> params) {
        if (params) {
            query.putAll(params)
        }

        return this
    }

    /**
     * Sets the request body. The body may be any object, such as a String,
     * JSON structure, or binary payload.
     *
     * <p>Example:
     * <pre>{@code
     * request.body("some text payload");
     *}</pre>
     *
     * @param body the request body
     * @return this request instance
     */
    HttpRequest body(Object body) {
        this.body = body
        return this
    }

    /**
     * Sets a multipart request body using a {@code HttpMultipartBody} builder.
     *
     * <p>Example:
     * <pre>{@code
     * HttpMultipartBody multipart = HttpMultipartBody.create()
     *     .addField("name", "value")
     *     .addFile("file", someFile)
     *
     * request.multipartBody(multipart)
     *}</pre>
     *
     * @param multipart the multipart body builder
     * @return this request instance
     */
    HttpRequest multipartBody(HttpMultipartBody multipart) {
        this.body = multipart.build()
        return this
    }

    /**
     * Returns the request body.
     *
     * @return the body object or {@code null} if none was set
     */
    Object getBody() {
        return body
    }
}
