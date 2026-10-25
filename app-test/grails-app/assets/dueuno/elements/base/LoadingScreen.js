// Temporary solutions until we get support for static fields
// See: https://github.com/google/closure-compiler/issues/2731
let LoadingScreen_timeoutIdPage = null;
let LoadingScreen_timeoutIdModal = null;

class LoadingScreen {

    static get timeoutIdPage() { return LoadingScreen_timeoutIdPage }
    static set timeoutIdPage(value) { LoadingScreen_timeoutIdPage = value }
    static get timeoutIdModal() { return LoadingScreen_timeoutIdModal }
    static set timeoutIdModal(value) { LoadingScreen_timeoutIdModal = value }

    static show(show) {
        if (PageModal.isActive) {
            LoadingScreen.modal(show);
        } else {
            LoadingScreen.page(show);
        }
    }

    static page(show) {
        clearTimeout(LoadingScreen.timeoutIdModal);
        LoadingScreen.timeoutIdModal = null;
        $('#loading-screen-modal').css('display', 'none');

        let $loading = $('#loading-screen-page');
        if (show && LoadingScreen.timeoutIdPage) {
            // Loading screen already visible
            return;

        } else if (show && !LoadingScreen.timeoutIdPage) {
            // Display the loading screen
            LoadingScreen.timeoutIdPage = setTimeout(() => {
                $loading.css('display', 'block');
            }, 0);

        } else {
            // Hide the loading screen
            $loading.css('display', 'none');
            clearTimeout(LoadingScreen.timeoutIdPage);
            LoadingScreen.timeoutIdPage = null;
        }
    }

    static modal(show) {
        clearTimeout(LoadingScreen.timeoutIdPage);
        LoadingScreen.timeoutIdPage = null;
        $('#loading-screen-page').css('display', 'none');

        let $loading = $('#loading-screen-modal');
        if (show && LoadingScreen.timeoutIdModal) {
            // Loading screen already visible
            return;

        } else if (show && !LoadingScreen.timeoutIdModal) {
            // Display the loading screen
            LoadingScreen.timeoutIdModal = setTimeout(() => {
                $loading.css('display', 'block');
            }, 0);

        } else {
            // Hide the loading screen
            $loading.css('display', 'none');
            clearTimeout(LoadingScreen.timeoutIdModal);
            LoadingScreen.timeoutIdModal = null;
        }
    }

}
