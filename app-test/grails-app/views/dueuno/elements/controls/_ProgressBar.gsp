<div class="control-progress-bar progress ${c.indefinite ? 'in' : ''}definite ${c.cssClass}"
     data-21-control="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
     data-21-value="${c.valueAsJSON}"
     role="progressbar"
     aria-label="${c.id}"
     aria-valuemin="${c.min}"
     aria-valuemax="${c.max}"
     ${!c.indefinite ? raw('aria-valuenow="' + c.value + '"') : ''}
     ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
>
    <div class="progress-line"></div>
    <div class="progress-subline progress-inc progress-bar-striped"></div>
    <div
        class="progress-bar progress-bar-striped"
        style="width: 0%"
    >
        <span class="progress-label">${c.prettyValue}${c.percentage ? '%' : ''}</span>
    </div>
</div>
