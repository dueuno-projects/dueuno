<%-- Modal --%>
<render:component instance="${c.modal}" />

<%-- MessageBox --%>
<render:component instance="${c.messageBox}" />

<%-- KeyPress --%>
<render:component instance="${c.keyPress}"/>

<%-- JavaScript Helpers --%>
<script>
    const _21_ = {
        app: {
            url: "${g.createLink(absolute:true, uri:"/")}",
            path: "${page.contextPath()}",
            tenant: "${tenant.current()}",
        },
        <g:if test="${c}">
        user: {
            username: "${security.username()}",
            language: "${c.locale}",
            decimalFormat: "${c.decimalFormat}",
            prefixedUnit: ${c.prefixedUnit},
            invertedMonth: ${c.invertedMonth},
            twelveHours: ${c.twelveHours},
            firstDaySunday: ${c.firstDaySunday},
            fontSize: ${c.fontSize},
            mobileFontSize: ${c.mobileFontSize},
            animations: ${c.animations},
        },
        </g:if>
        log: {
            error: ${dev.logError()},
            debug: ${dev.logDebug()},
            trace: ${dev.logTrace()},
        },
    }
</script>

<%-- PWA - Progressive Web Application --%>
<script>const serviceWorkerFilename = "${asset.assetPath(src:'pwa/service-worker.js')}"</script>
<asset:javascript src="pwa/register.js"/>

<%-- Elements --%>
<asset:javascript src="includes.js"/>
<g:each var="componentsImplementation" in="${c.componentsRegistry}">
    <asset:javascript src="${componentsImplementation}.js"/>
</g:each>

<%-- Plugins Specific Javascript --%>
<asset:assetPathExists src="plugin.js">
    <asset:javascript src="plugin.js"/>
</asset:assetPathExists>

<%-- Application Specific Javascript --%>
<asset:assetPathExists src="application.js">
    <asset:javascript src="application.js"/>
</asset:assetPathExists>
