<div class="component-progress-bar progress ${c.indefinite ? 'in' : ''}definite ${c.cssClass}"
     data-21-component="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
     role="progressbar"
     aria-label="${c.id}"
     aria-valuemin="${c.min}"
     aria-valuemax="${c.max}"
     ${!c.indefinite ? raw('aria-valuenow="' + c.now + '"') : ''}
     ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
>
    <div class="progress-line"></div>
    <div class="progress-subline progress-inc"></div>
    <div class="progress-subline progress-dec"></div>
    <div
            class="progress-bar"
            style="${"width: " + c.nowCalc + "%"}"
    >
        <span class="progress-label">${c.prettyNowCalc}%</span>
    </div>
</div>
