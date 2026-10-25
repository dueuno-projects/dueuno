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
package dueuno.security

import groovy.transform.CompileStatic
import org.apache.catalina.Context
import org.apache.catalina.Host
import org.springframework.boot.tomcat.TomcatWebServer
import org.springframework.boot.web.server.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.ApplicationListener

/**
 * Supplies Tomcat's session manager to the user session service after server startup.
 */
@CompileStatic
class UserSessionTomcatContextListener implements ApplicationListener<ServletWebServerInitializedEvent> {

    UserSessionService userSessionService

    @Override
    void onApplicationEvent(ServletWebServerInitializedEvent event) {
        if (!(event.webServer instanceof TomcatWebServer)) return

        TomcatWebServer webServer = event.webServer as TomcatWebServer
        Host host = webServer.tomcat.host
        String contextPath = event.applicationContext.servletContext.contextPath
        Context context = host.findChild(contextPath) as Context
        if (context) userSessionService.initializeTomcat(context)
    }
}
