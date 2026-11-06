/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.security

import spock.lang.Specification

import java.lang.reflect.Method

class UserSessionControllerSpec extends Specification {

    void 'extracts browser name version and icon'() {
        expect:
        invoke('getBrowserInfo', userAgent) == [icon: icon, tooltip: tooltip]

        where:
        userAgent                                             | icon                  | tooltip
        'Mozilla/5.0 Chrome/131.0.0.0 Safari/537.36'          | 'fa-brands fa-chrome' | 'Chrome 131.0.0.0'
        'Mozilla/5.0 Edg/131.0.0.0'                           | 'fa-brands fa-edge'   | 'Microsoft Edge 131.0.0.0'
        'Mozilla/5.0 Version/18.0 Safari/605.1.15'            | 'fa-brands fa-safari' | 'Safari 18.0'
        'Mozilla/5.0 SamsungBrowser/27.0 Chrome/131.0 Safari' | 'fa-solid fa-globe'   | 'Samsung Internet 27.0'
        null                                                  | 'fa-solid fa-globe'   | 'Unknown browser'
    }

    void 'extracts operating system name version and icon'() {
        expect:
        invoke('getOperatingSystemInfo', userAgent) == [icon: icon, tooltip: tooltip]

        where:
        userAgent                                                | icon                       | tooltip
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'              | 'fa-brands fa-windows'     | 'Windows 10/11'
        'Mozilla/5.0 (X11; Linux x86_64)'                        | 'fa-brands fa-linux fa-lg' | 'Linux'
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5)'           | 'fa-brands fa-apple fa-lg' | 'macOS 14.5'
        'Mozilla/5.0 (Linux; Android 14; Pixel 8)'               | 'fa-brands fa-android'     | 'Android 14'
        'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X)' | 'fa-brands fa-apple fa-lg' | 'iOS 17.4'
        null                                                     | 'fa-solid fa-gear'         | 'Unknown OS'
    }

    private Map invoke(String methodName, String userAgent) {
        Method method = UserSessionController.getDeclaredMethod(methodName, String)
        method.accessible = true
        return method.invoke(new UserSessionController(), (Object[]) [userAgent]) as Map
    }
}
