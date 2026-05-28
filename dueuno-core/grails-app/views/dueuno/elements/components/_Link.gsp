<a class="component-link ${c.textAlign} ${c.textWrap} ${c.textStyle} ${c.tag ? 'tag' : ''}  ${c.userSelect ? 'user-select-text' : ''} ${c.html ? 'html' : ''} ${c.cssClass}"
   data-21-component="${c.className}"
   data-21-id="${c.id}"
   data-21-properties="${c.propertiesAsJSON}"
   data-21-events="${c.eventsAsJSON}"
   href="${dev.ifDevelopment() { c.devUrl } }"
   ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
   ${c.tooltip ? raw('data-bs-custom-class="tooltip" data-bs-toggle="tooltip" data-bs-html="true" data-bs-title="' + c.message(c.tooltip) + '"') : ''}
   ${raw(attributes)}
><g:if test="${c.html}">${raw(c.html)}</g:if>
<g:elseif test="${c.components}"><render:componentList instance="${c}" /></g:elseif>
<g:else>
    <% text = c.text %>
    <g:if test="${c.image}"><i><asset:image src="${c.image}" class="image-icon ${c.imageClass}"/></i></g:if>
    <g:elseif test="${c.icon}"><render:icon icon="${c.icon}" /></g:elseif>
    <g:if test="${c.text}"><span class="${c.textStyle}">${text}</span></g:if>
</g:else>
</a>