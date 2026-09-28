<div id="${c.id}"
     class="component-grid mb-2"
     data-21-component="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
>
    <div class="row g-${c.spacing} ${c.tag ? 'my-0' : ''}">
        <g:each var="column" in="${c.components}">
            <div class="grid-column ${column.breakpoints} ${column.scrollable ? 'scrollable' : ''}" data-21-id="${column.id}" data-21-component="${column.className}">
            <g:if test="${c.tag}">
                <div class="grid-tag shadow ${c.cssClass}"
                     ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}>
                    <div class="grid-tag overflow-hidden">
                        <render:componentList instance="${column}"/>
                    </div>
                </div>
            </g:if>
            <g:else>
                <div class="${c.cssClass}"
                     ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}>
                    <render:componentList instance="${column}"/>
                </div>
            </g:else>
        </div>
        </g:each>
    </div>
</div>
