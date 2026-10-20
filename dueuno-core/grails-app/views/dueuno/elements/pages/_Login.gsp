<!doctype html>
<html lang="${c.locale}">
<head>
    <page:header component="${c}" />
    <page:colors component="${c}"/>
</head>

<body class="background-image"
      style="background-image: url('${raw(c.backgroundImage)}');"
      data-21-page="${c.className}"
      data-21-id="${c.id}"
>

<page:loading />

<div id="page-content" class="page-login justify-content-center">

    <div class="page-login-box text-center p-3">
        <img class="page-login-logo py-2"
             src="${raw(c.logoImage)}"
        />
        <div class="page-login-error d-none">
            <render:message code="shell.auth.bad.credentials"/>
        </div>

        <render:component instance="${c.loginKeyPress}" />
        <render:component instance="${c.form}" />
    </div>

</div>

<g:if test="${c.tenantId}">
    <div class="page-login-tenant">
        <render:message code="authentication.tenant" args="${[c.tenantId]}"/>
    </div>
</g:if>

</div>

<page:footer component="${c}" />
<page:initialize />

</body>
</html>
