<g:if test="${c.icon || c.prefix}">
    <span class="input-group-text"><g:if test="${c.icon}"><render:icon icon="${c.icon}" force="fa-solid"/></g:if>${c.prefix}</span>
</g:if>
<input type="${c.inputType}"
       class="control-text-field form-control ${c.textStyle} ${c.cssClass}"
       inputmode="${c.inputMode}"
       placeholder="${c.message(c.placeholder)}"
       maxlength="${(c.maxSize > 0) ? c.maxSize: ''}"
       autocapitalize="none"
       autocomplete="${c.autocomplete ? 'on' : 'off'}"
       data-21-control="${c.className}"
       data-21-id="${c.id}"
       data-21-properties="${c.propertiesAsJSON}"
       data-21-events="${c.eventsAsJSON}"
       data-21-value="${c.valueAsJSON}"
       ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
       ${raw(attributes)}
>
<g:if test="${c.displayMaxSize && c.maxSize > 0}">
    <div class="character-counter" data-max="${c.maxSize}" aria-hidden="true"><span class="character-counter-value">${c.maxSize}</span></div>
</g:if>
<g:if test="${c.actions.hasActions()}"><!--
    <g:each var="action" in="${c.actions.defaultAction}">
        --><render:component instance="${action.link}" properties="[cssClass: 'btn btn-secondary', readonly: c.readonly]" /><!--
    </g:each>
    <g:each var="action" in="${c.actions.getMenuActions()}">
        --><render:component instance="${action.link}" properties="[cssClass: 'btn btn-secondary', readonly: c.readonly]" /><!--
    </g:each>
--></g:if>
<g:if test="${c.inputType == 'password'}">
    <button class="show-password btn btn-secondary show-hide-password" type="button">
        <i class="fa-solid fa-eye"></i>
    </button>
</g:if>
