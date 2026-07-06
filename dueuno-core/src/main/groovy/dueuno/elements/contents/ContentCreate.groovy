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
package dueuno.elements.contents

import dueuno.elements.style.TextDefault
import groovy.transform.CompileStatic

/**
 * A {@link ContentForm} pre-configured for the "create new record" use case.
 * <p>
 * On construction the header's primary button label is set to
 * {@link dueuno.elements.style.TextDefault#SAVE} and its action is wired to
 * {@code onCreate}, following the Elements CRUD convention. The form can be
 * further populated with fields after instantiation.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ContentCreate extends ContentForm {

    /**
     * Creates a {@code ContentCreate} instance configured from the supplied argument map.
     * Sets the header's next button to "Save" / {@code onCreate}.
     *
     * @param args initialisation arguments forwarded to {@link ContentForm#ContentForm(Map)}
     */
    ContentCreate(Map args) {
        super(args)

        header.nextButton.text = TextDefault.SAVE
        header.nextButton.action = 'onCreate'
    }
}

