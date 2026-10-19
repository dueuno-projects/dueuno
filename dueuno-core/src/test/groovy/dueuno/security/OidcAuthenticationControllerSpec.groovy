/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.security

import spock.lang.Specification

import java.lang.reflect.Method

class OidcAuthenticationControllerSpec extends Specification {

    void 'keeps server response bodies unchanged in response details'() {
        given:
        OidcAuthenticationController controller = new OidcAuthenticationController()
        Method responseDetails = OidcAuthenticationController.getDeclaredMethod('responseDetails', List)
        responseDetails.accessible = true
        String body = '{"error":"invalid_grant","error_description":"Code expired"}'
        Map serverResponse = [
            status : 400,
            uri    : 'https://issuer.example.org/token',
            headers: ['content-type': ['application/json']],
            body   : body,
        ]

        when:
        String details = responseDetails.invoke(controller, (Object) [serverResponse]) as String

        then:
        details == "OIDC server responses:\nHTTP 400 from https://issuer.example.org/token\nHeaders:\ncontent-type: application/json\nBody:\n${body}"
    }

    void 'formats response bodies as readable and escaped HTML'() {
        given:
        OidcAuthenticationController controller = new OidcAuthenticationController()
        Method responseDetailsHtml = OidcAuthenticationController.getDeclaredMethod('responseDetailsHtml', String, List)
        responseDetailsHtml.accessible = true
        String body = '{"error":"<script>alert(1)</script>"}'
        Map serverResponse = [
            status : 400,
            uri    : 'https://issuer.example.org/token',
            headers: ['content-type': ['application/json']],
            body   : body,
        ]

        when:
        String html = responseDetailsHtml.invoke(
            controller,
            (Object[]) ['OIDC sign-in failed', [serverResponse]],
        ) as String

        then:
        html.contains('<h6 class="mb-2">HTTP 400 from https://issuer.example.org/token</h6>')
        html.contains('Response headers')
        html.contains('content-type: application/json')
        html.contains('Response body')
        html.contains('\n')
        html.contains('&quot;error&quot;: &quot;&lt;script&gt;alert(1)&lt;/script&gt;')
        !html.contains('<script>')
    }
}
