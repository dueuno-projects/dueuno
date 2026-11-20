<img class="component-image w-100 ${c.keepAspectRatio ? '' : 'image-stretch'} ${c.cssClass}"
     data-21-component="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
     src="${c.image}"
     ${c.cssStyleColors ? raw('style="' + c.cssStyleColors + '"') : ''}
/>