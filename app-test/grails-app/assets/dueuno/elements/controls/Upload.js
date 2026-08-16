class Upload extends Control {

    static get valueType() {
        return Type.LIST;
    }

    static initialize($element, $root) {
        let properties = Control.getProperties($element);

        let initOptions = {
            paramName: '_21Upload',
            url: 'not/set',
            addRemoveLinks: true,
            thumbnailWidth: properties.thumbnailWidth ?? _21_.user.fontSize * 7,
            thumbnailHeight: properties.thumbnailHeight ?? _21_.user.fontSize * 7,

            maxThumbnailFilesize: properties.maxThumbnailFilesize,
            maxFileSize: properties.maxFileSize, // MB
            maxFiles: properties.maxFiles,
            acceptedFiles: properties.acceptedFiles,

            dictDisabled: properties.messages.disabled,
            dictDefaultMessage: properties.messages.upload,
            dictFileTooBig: properties.messages.tooBig,
            dictInvalidFileType: properties.messages.invalidType,
            dictResponseError: properties.messages.responseError,
            dictCancelUpload: properties.messages.cancel,
            dictCancelUploadConfirmation: properties.messages.cancelConfirmation,
            dictUploadCanceled: properties.messages.canceled,
            dictRemoveFile: properties.messages.remove,
            dictRemoveFileConfirmation: properties.messages.removeConfirmation,
            dictMaxFilesExceeded: properties.messages.maxExceeded,

            // Don't ask me why but this is the only way to
            // register/trigger the events with Dropzone
            init: Upload.registerEvents,
        };

        $element.dropzone(initOptions);
    }

    static finalize($element, $root) {
        let componentEvent = Component.getEvent($element, 'upload');
        if (!componentEvent) {
            return;
        }

        let url = Transition.buildUrl(componentEvent);
        let queryString = Transition.buildQueryString(componentEvent);
        $element[0].dropzone.options.url = url + queryString;
    }

    static registerEvents() {
        let $element = this;
        $element.on('addedfile', Upload.onAddFile);
        $element.on('removedfile', Upload.onRemoveFile);
        $element.on('success', Upload.onSuccess);
        $element.on('error', Upload.onError);
    }

    static onAddFile(file) {
        let $element = $(this.element);

        let componentEvent = Component.getEvent($element, 'upload');
        if (componentEvent && componentEvent.loading) {
            LoadingScreen.show(true);
        }

        Transition.triggerEvent($element, 'addfile');
    }

    static onRemoveFile(file) {
        let $element = $(this.element);
        Transition.triggerEvent($element, 'removefile');
    }

    static onSuccess(file) {
        let $element = $(this.element);
        Transition.triggerEvent($element, 'success');
    }

    static onError(file) {
        let $element = $(this.element);
        LoadingScreen.show(false);
        Transition.triggerEvent($element, 'error');
    }

    static setValue($element, valueMap, trigger = true) {
        // no-op
    }

    static getValue($element) {
        let dropzone = $element[0].dropzone;
        if (!dropzone) {
            return TypedValue.list();
        }

        let results = [];
        let files = dropzone.getAcceptedFiles();
        for (let file of files) {
            results.push(file.upload.filename);
        }

        return TypedValue.list(results);
    }

    static clear($element) {
        $element[0].dropzone.removeAllFiles();
    }

    static setReadonly($element, value) {
        Component.setReadonly($element, value);
        let $button = $element.find('button');
        let dropzone = $element[0].dropzone;

        if (value) {
            $button.html(dropzone.options.dictDisabled);
            dropzone.disable();

        } else {
            $button.html(dropzone.options.dictDefaultMessage);
            dropzone.enable();
        }
    }

}

Control.register(Upload);
