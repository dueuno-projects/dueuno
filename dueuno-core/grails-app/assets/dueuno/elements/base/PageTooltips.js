class PageTooltips {

    static initialize() {
        for (let tooltip of PageContent.tooltips) {
            if (tooltip) {
                tooltip.dispose();
            }
        }

        PageContent.tooltips.length = 0;
    }

    static finalize() {
        // Initializing Bootstrap tooltips.
        // On Mobile we activate it only for Labels
        let $tooltipTriggerList = Elements.isMobileDevice
            ? $('.component-label [data-bs-toggle="tooltip"]')
            : $('[data-bs-toggle="tooltip"]');

        for (let $tooltipTrigger of $tooltipTriggerList) {
            PageContent.tooltips.push(
                new bootstrap.Tooltip($tooltipTrigger)
            );
        }
    }

}