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
package dueuno.elements.controls

import dueuno.application.WebRequestAware
import dueuno.commons.utils.FileUtils

/**
 * @author Gianluca Sartori
 */
class UploadController implements WebRequestAware {

    static namespace = 'elements'

    def uploadFile() {
        def file = request.getFile(params.controlName)
        def filename = System.currentTimeMillis()

        file.transferTo(new File(FileUtils.tempDirectory + filename))
        render filename
    }
}
