<img class="component-image w-100 ${c.keepAspectRatio ? (c.fitWidth ? 'image-fit-width' : '') : 'image-stretch'} ${c.cssClass}"
     data-21-component="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
     src="${c.image}"
     ${c.cssStyle ? raw('style="' + c.cssStyle + '"') : ''}
/>