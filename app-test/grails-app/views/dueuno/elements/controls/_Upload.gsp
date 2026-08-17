<div class="control-upload ${c.isProduction() ? 'hide-error' : ''} ${c.cssClass}"
     data-21-control="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
     data-21-value="${c.valueAsJSON}"
     ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
>
    <input type="file"/>
</div>
