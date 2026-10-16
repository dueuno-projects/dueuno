<meta charset="utf-8" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />

<link rel="icon" type="image/png" sizes="64x64" href="${c.favicon}"/>
<link rel="apple-touch-icon" type="image/png" sizes="180x180" href="${c.appicon}"/>
<meta name="mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="default">
<meta name="apple-mobile-web-app-title" content="${c.message('app.name')}">

<title>${c.message('app.name')}</title>
<link rel="manifest" href="${g.createLink(uri: '/pwa/manifest.json')}">

<%-- Elements --%>
<asset:stylesheet src="includes.css" media="screen"/>
<g:each var="componentsImplementation" in="${c.componentsRegistry}">
    <asset:stylesheet src="${componentsImplementation}.css" media="screen"/>
</g:each>

<%-- Plugins Specific Stylesheet --%>
<asset:assetPathExists src="plugin.css">
    <asset:stylesheet src="plugin.css"/>
</asset:assetPathExists>

<%-- Application Specific Stylesheet --%>
<asset:assetPathExists src="application.css">
    <asset:stylesheet src="application.css"/>
</asset:assetPathExists>

<%-- Main --%>
<asset:stylesheet src="main.css" media="screen"/>

<style>
    :root {
        --elements-font-size: ${c.fontSize ?: '14'}px;
        --bs-border-radius-sm: ${c.guiStyle == 'SQUARED' ? '0rem' : '.40rem'}; <%-- Controls --%>
        --bs-border-radius:    ${c.guiStyle == 'SQUARED' ? '0rem' : '.60rem'}; <%-- Buttons --%>
        --bs-border-radius-lg: ${c.guiStyle == 'SQUARED' ? '0rem' : '.60rem'}; <%-- Components --%>
        --bs-border-radius-xl: ${c.guiStyle == 'SQUARED' ? '0rem' : '1rem'};   <%-- Modals --%>
    }
</style>
