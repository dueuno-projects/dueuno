<div class="control-textarea w-100 ${c.maxSize > 0 ? 'maxlength' : ''} ${c.cssClass}"
     style="${c.cssStyleColors}"
    ><g:if test="${c.maxSize > 0}"><div class="character-counter" data-max="${c.maxSize}" aria-hidden="true"><span class="character-counter-value">${c.maxSize}</span></div></g:if>
    <textarea class="form-control h-100 ${c.textStyle}"
              placeholder="${c.message(c.placeholder)}"
              maxlength="${(c.maxSize > 0) ? c.maxSize : ''}"
              data-21-control="${c.className}"
              data-21-id="${c.id}"
              data-21-properties="${c.propertiesAsJSON}"
              data-21-events="${c.eventsAsJSON}"
              data-21-value="${c.valueAsJSON}"
              ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
              ${raw(attributes)}
    >${c.value}</textarea>
</div>
