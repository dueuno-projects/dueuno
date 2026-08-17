class Upload extends Control {

    static get valueType() {
        return Type.LIST;
    }

    static initialize($element, $root) {
        let properties = Control.getProperties($element);

        FilePond.registerPlugin(
            FilePondPluginFileValidateType,
            FilePondPluginFileValidateSize,
            FilePondPluginImagePreview,
        );

        let pond = FilePond.create($element.find('input[type="file"]')[0], {
            name: '_21Upload',
            allowMultiple: properties.allowMultiple,
            acceptedFileTypes: properties.acceptedFileTypes,
            maxFiles: properties.maxFiles,
            maxFileSize: properties.maxFileSize,
            allowRevert: false,
            credits: false,

            labelIdle: properties.labelIdle,
            labelMaxFileSizeExceeded: properties.messages.tooBig,
            labelFileTypeNotAllowed: properties.messages.invalidType,
            labelFileProcessingError: properties.messages.responseError,
            labelTapToCancel: properties.messages.cancel,
            labelFileProcessingAborted: properties.messages.canceled,
            labelButtonRemoveItem: properties.messages.remove,
        });

        $element[0]._filePond = pond;
        Upload.registerEvents($element, pond);
    }

    static finalize($element, $root) {
        let componentEvent = Component.getEvent($element, 'upload');
        if (!componentEvent) {
            return;
        }

        let url = Transition.buildUrl(componentEvent);
        let values = Transition.build21Params(componentEvent);
        $element[0]._filePond.setOptions({
            server: {
                process: {
                    url: url,
                    method: 'POST',
                    ondata: (formData) => {
                        for (let [key, value] of Object.entries(values)) {
                            formData.append(key, value);
                        }
                        for (let [key, value] of Object.entries(componentEvent['params'] ?? {})) {
                            formData.append(key, value);
                        }
                        return formData;
                    },
                },
            },
        });
    }

    static registerEvents($element, pond) {
        pond.on('addfilestart', () => Upload.onAddFile($element));
        pond.on('removefile', () => Upload.onRemoveFile($element));
        pond.on('processfile', (error) => Upload.onProcessFile($element, error));
        pond.on('error', () => Upload.onError($element));
    }

    static onAddFile($element) {
        let componentEvent = Component.getEvent($element, 'upload');
        if (componentEvent && componentEvent.loading) {
            LoadingScreen.show(true);
        }

        Transition.triggerEvent($element, 'addfile');
    }

    static onRemoveFile($element) {
        Transition.triggerEvent($element, 'removefile');
    }

    static onProcessFile($element, error) {
        if (!error) {
            Transition.triggerEvent($element, 'success');
        }
    }

    static onError($element) {
        LoadingScreen.show(false);
        Transition.triggerEvent($element, 'error');
    }

    static setValue($element, valueMap, trigger = true) {
        // no-op
    }

    static getValue($element) {
        let pond = $element[0]._filePond;
        if (!pond) {
            return TypedValue.list();
        }

        let filenames = pond.getFiles()
            .filter(file => file.status === FilePond.FileStatus.PROCESSING_COMPLETE)
            .map(file => file.filename);

        return TypedValue.list(filenames);
    }

    static clear($element) {
        let pond = $element[0]._filePond;
        if (pond) {
            pond.removeFiles();
        }
    }

    static setReadonly($element, value) {
        Component.setReadonly($element, value);

        let pond = $element[0]._filePond;
        if (pond) {
            pond.disabled = value;
        }
    }

}

Control.register(Upload);
