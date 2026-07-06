<g:if test="${c.simple}">
<div class="control-checkbox-simple ${c.cssClass}">
    <div class="checkbox simple">
</g:if>
<g:else>
<span class="input-group-text control-checkbox ${c.cssClass}">
    <div class="text-wrapper">
        <g:if test="${c.message(c.text)}"><render:message code="${c.text}" args="${c.textArgs}" /></g:if><g:else>&nbsp;</g:else>
    </div>
    <div class="form-check form-switch">
</g:else>

<input
    class="form-check-input ${c.simple ? '' : 'form-control'}"
    type="checkbox"
    role="switch"
    data-21-control="${c.className}"
    data-21-id="${c.id}"
    data-21-properties="${c.propertiesAsJSON}"
    data-21-events="${c.eventsAsJSON}"
    data-21-value="${c.valueAsJSON}"
    ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
    ${raw(attributes)}
/>

<g:if test="${c.simple}">
    </div>
</div>
</g:if>
<g:else>
    </div>
</span>
</g:else>
