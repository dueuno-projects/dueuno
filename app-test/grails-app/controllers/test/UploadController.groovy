/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test

import dueuno.elements.ElementsController
import dueuno.elements.components.Button
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.TextField
import dueuno.elements.controls.Upload
import dueuno.tenant.TenantService

class UploadController implements ElementsController {

    TenantService tenantService
    DemoService demoService

    private buildForm(TDemo obj) {
        def c = createContent(ContentForm)
        c.header.nextButton.text = 'Ok'
        c.with {
            form.with {
                addField(
                    class: Button,
                    id: 'file',
                    action: 'onDownloadAttachment',
                    params: [filename: obj.filename],
                    icon: 'fa-file-download',
                    text: obj.filename,
                    targetNew: true,
                )
                addField(
                    class: TextField,
                    id: 'test1',
                    value: 'This is a test',
                )
                addField(
                    class: Button,
                    id: 'enable',
                    action: 'onEnable',
                    loading: false,
                    cols: 6,
                )
                addField(
                    class: Button,
                    id: 'disable',
                    action: 'onDisable',
                    loading: false,
                    cols: 6,
                )
                addField(
                    class: Upload,
                    id: 'somefile',
//                        onAddFile: 'onAddFile',
//                        onRemoveFile: 'onRemoveFile',
                    onUpload: 'onUploadFile',
                    onSuccess: 'onUploadSuccess',
                    maxFiles: 1,
                    acceptedFiles: ['.jpg', '.jpeg'],
                    submit: 'form',
                    loading: true,
                    rows: 5,
                )
                addField(
                    class: TextField,
                    id: 'test2',
                    value: 'This is another test',
                )
            }
        }

        if (obj) {
            c.form.values = obj
        }

        return c
    }

    def index() {
        def obj = demoService.get(1)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def onDisable() {
        def t = createTransition()
        t.set('somefile', 'readonly', true)
        display transition: t
    }

    def onEnable() {
        def t = createTransition()
        t.set('somefile', 'readonly', false)
        display transition: t
    }

    def onUploadFile() {
        try {
            println params
            def path = tenantService.getPublicDir() + 'upload/'
            Upload.save(path)
            sleep(1000)

            println 'Uploaded file "' + Upload.filename + '" to path ' + path
            display

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }

    def onUploadSuccess() {
        params.id = 1
        params.filename = params.somefile[0]
        def obj = demoService.update(params)

        def t = createTransition()
        t.set('file', 'text', obj.filename)
        t.set('file', 'icon', 'fa-solid fa-check')
        t.call('somefile', 'clear')

        t.loading(false)

        display transition: t
    }

    def onConfirm() {
        demoService.update(id: 1, filename: params.somefile[0])
        display action: 'index'
    }

    def onDownloadAttachment() {
        def obj = demoService.get(1)
        def path = tenantService.getPublicDir() + 'upload/'
        String filename = path + obj.filename

        download filename, true
    }
}
