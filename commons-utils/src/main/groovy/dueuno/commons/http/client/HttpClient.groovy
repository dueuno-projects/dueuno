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
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.apache.hc.client5.http.classic.methods.*
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.apache.hc.core5.util.Timeout

import javax.net.ssl.SSLContext

/**
 * Utility class providing simplified HTTP communication using Apache HttpClient.
 * Supports building and executing HTTP requests with JSON, text, or byte responses.
 * NOTE: We use Apache HttpClient 5 for compatibility with SAP BTP.
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * CloseableHttpClient client = HttpClient.create(30, false)
 * HttpRequest request = HttpRequest.GET("https://example.com/data")
 * HttpResponse response = HttpClient.call(client, request)
 * if (response.ok) {
 *     println response.body
 * }
 * </pre>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class HttpClient {

    /**
     * Creates a configured {@link CloseableHttpClient} instance.
     *
     * @param timeoutSeconds timeout in seconds for both connection request and response
     * @param forceCertificateVerification whether SSL certificate verification should be enforced
     * @return a configured {@link CloseableHttpClient}
     *
     * <p><b>Example:</b></p>
     * <pre>
     * CloseableHttpClient client = HttpClient.create(10, true)
     * </pre>
     */
    static CloseableHttpClient create(Integer timeoutSeconds = 30, Boolean forceCertificateVerification = true) {
        return forceCertificateVerification
            ? createValidCertHttpClient(timeoutSeconds)
            : createNoCertHttpClient(timeoutSeconds)
    }

    /**
     * Creates an HTTP client that validates SSL certificates.
     *
     * @param timeoutSeconds timeout in seconds
     * @return a client with SSL certificate validation enabled
     */
    private static CloseableHttpClient createValidCertHttpClient(Integer timeoutSeconds) {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(timeoutSeconds))
            .setResponseTimeout(Timeout.ofSeconds(timeoutSeconds))
            .build()

        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build()
    }

    /**
     * Creates an HTTP client that accepts all SSL certificates.
     *
     * @param timeoutSeconds timeout in seconds
     * @return a client with SSL certificate validation disabled
     */
    private static CloseableHttpClient createNoCertHttpClient(Integer timeoutSeconds) {
        SSLContext sslContext = SSLContextBuilder.create()
            .loadTrustMaterial(TrustAllStrategy.INSTANCE)
            .build()

        DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(
            sslContext,
            HostnameVerificationPolicy.BOTH,
            NoopHostnameVerifier.INSTANCE
        )

        PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .build()

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(timeoutSeconds))
            .setResponseTimeout(Timeout.ofSeconds(timeoutSeconds))
            .build()

        CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connManager)
            .build()

        return httpClient
    }

    /**
     * Builds an Apache HttpClient request object from a custom {@link HttpRequest}.
     * Supports String bodies, JSON bodies, and {@link HttpEntity} bodies.
     *
     * @param request the custom request definition
     * @return a constructed {@link HttpUriRequestBase}
     */
    private static HttpUriRequestBase buildHttpRequest(HttpRequest request) {
        HttpUriRequestBase httpRequest
        switch (request.method) {
            case HttpMethod.GET: httpRequest = new HttpGet(request.url); break
            case HttpMethod.POST: httpRequest = new HttpPost(request.url); break
            case HttpMethod.PUT: httpRequest = new HttpPut(request.url); break
            case HttpMethod.PATCH: httpRequest = new HttpPatch(request.url); break
            case HttpMethod.DELETE: httpRequest = new HttpDelete(request.url); break
            default: throw new IllegalArgumentException("Unsupported HTTP method: ${request.method}")
        }

        for (header in request.headers) {
            httpRequest.setHeader(header)
        }

        if (request.body == null) {
            return httpRequest
        }

        def body = request.body
        if (body == null) {
            return httpRequest
        }

        switch (body) {
            case HttpEntity:
                httpRequest.entity = body as HttpEntity
                break

            case String:
                ContentType contentType = request.getHeader("Content-Type")
                    ? ContentType.parse(request.getHeader("Content-Type").value)
                    : ContentType.TEXT_PLAIN
                httpRequest.entity = new StringEntity(body as String, contentType)

                if (!request.getHeader("Accept")) {
                    httpRequest.setHeader("Accept", ContentType.TEXT_PLAIN.mimeType)
                }
                if (!request.getHeader("Content-Type")) {
                    httpRequest.setHeader("Content-Type", ContentType.TEXT_PLAIN.mimeType)
                }
                break

            default:
                try {
                    String jsonBody = JsonOutput.toJson(body)
                    ContentType contentType = request.getHeader("Content-Type")
                        ? ContentType.parse(request.getHeader("Content-Type").value)
                        : ContentType.APPLICATION_JSON
                    httpRequest.entity = new StringEntity(jsonBody, contentType)

                    if (!request.getHeader("Accept")) {
                        httpRequest.setHeader("Accept", ContentType.APPLICATION_JSON.mimeType)
                    }
                    if (!request.getHeader("Content-Type")) {
                        httpRequest.setHeader("Content-Type", ContentType.APPLICATION_JSON.mimeType)
                    }

                } catch (Exception e) {
                    throw new Exception("Body of type '${body.getClass()}' is not supported: ${e.message ?: e.cause.message}")
                }
        }

        return httpRequest
    }

    /**
     * Executes an HTTP request and returns a structured {@link HttpResponse}.
     *
     * @param client the HTTP client to use
     * @param request the request to execute
     * @param responseType how the response body should be interpreted (STRING, MAP, BYTES)
     * @return a structured {@link HttpResponse} containing the result
     */
    static HttpResponse call(CloseableHttpClient client, HttpRequest request, HttpResponseType responseType = HttpResponseType.JSON) {
        HttpUriRequestBase httpRequest = buildHttpRequest(request)

        if (!request.getHeader("Accept")) {
            httpRequest.setHeader("Accept", "*/*")
        }

        try {
            return client.execute(httpRequest) { ClassicHttpResponse response ->
                switch (responseType) {
                    case HttpResponseType.RAW: return handleRawResponse(response)
                    case HttpResponseType.JSON: return handleJsonResponse(response)
                    case HttpResponseType.BYTES: return handleBytesResponse(response)
                    default: throw new IllegalArgumentException("Unsupported ResponseType: $responseType")
                }
            }

        } catch (Exception e) {
            throw new Exception("Error calling '${request.method} ${request.url}': ${e.message ?: e.cause.message}")
        }
    }

    /**
     * Shortcut for downloading binary content.
     *
     * @param client the HTTP client to use
     * @param request the HTTP request
     * @return a {@link HttpResponse} containing raw bytes
     */
    static HttpResponse download(CloseableHttpClient client, HttpRequest request) {
        return call(client, request, HttpResponseType.BYTES)
    }

    private static HttpResponse handleRawResponse(ClassicHttpResponse response) {
        Integer status = response.code
        String raw = response.entity ? EntityUtils.toString(response.entity) : ''

        if (status >= 200 && status < 300) {
            return HttpResponse.success(status, response.headers, raw)
        } else {
            return HttpResponse.error(status, "HTTP error $status", response.headers, raw)
        }
    }

    private static HttpResponse handleJsonResponse(ClassicHttpResponse response) {
        Integer status = response.code
        String json = response.entity ? EntityUtils.toString(response.entity) : ''

        Map parsed = [:]
        try {
            parsed = new JsonSlurper().parseText(json) as Map
        } catch (Exception e) {
            return HttpResponse.error(status, "Invalid JSON (must be a Map): ${e.message}", response.headers, json)
        }

        if (status >= 200 && status < 300) {
            return HttpResponse.success(status, response.headers, parsed)
        } else {
            return HttpResponse.error(status, "HTTP error $status", response.headers, parsed)
        }
    }

    private static HttpResponse handleBytesResponse(ClassicHttpResponse response) {
        int status = response.code
        byte[] bytes = response.entity ? EntityUtils.toByteArray(response.entity) : new byte[0]

        if (status >= 200 && status < 300) {
            return HttpResponse.success(status, response.headers, bytes)
        } else {
            String errorText = ''
            try {
                errorText = new String(bytes)
            } catch (ignored) {
            }
            return HttpResponse.error(status, "HTTP error $status", response.headers, errorText)
        }
    }

}
