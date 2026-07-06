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
package dueuno.elements.style

import groovy.transform.CompileStatic

/**
 * Centralises the i18n message keys used by the Elements framework for standard UI action
 * labels and confirmation messages (e.g. "Save", "Delete", "Cancel").
 * <p>
 * Each constant holds the key of a message that must be present in the application's
 * {@code i18n/*.properties} files. Using these constants avoids hard-coded strings and
 * ensures consistent labelling across all Elements components.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class TextDefault {

    // Matching translation must be added to i18n *.properties files as well
    //
    static final String TRUE = 'default.text.true'
    static final String FALSE = 'default.text.false'
    static final String YES = 'default.text.yes'
    static final String NO = 'default.text.no'
    static final String NEW = 'default.text.new'
    static final String ADD = 'default.text.add'
    static final String REMOVE = 'default.text.remove'
    static final String CLONE = 'default.text.clone'
    static final String CREATE = 'default.text.create'
    static final String EDIT = 'default.text.edit'
    static final String VIEW = 'default.text.view'
    static final String DELETE = 'default.text.delete'
    static final String UNDELETE = 'default.text.undelete'
    static final String RESTORE = 'default.text.restore'
    static final String HARD_DELETE = 'default.text.hard.delete'
    static final String SAVE = 'default.text.save'
    static final String PRINT = 'default.text.print'
    static final String OK = 'default.text.ok'
    static final String CANCEL = 'default.text.cancel'
    static final String CONFIRM = 'default.text.confirm'
    static final String CONTINUE = 'default.text.continue'
    static final String INFO = 'default.text.info'
    static final String BACK = 'default.text.back'
    static final String NEXT = 'default.text.next'
    static final String CLOSE = 'default.text.close'
    static final String CLEAR = 'default.text.clear'
    static final String SEARCH = 'default.text.search'
    static final String FIND = 'default.text.find'
    static final String UPDATE = 'default.text.update'
    static final String REFRESH = 'default.text.refresh'
    static final String RELOAD = 'default.text.reload'
    static final String EXECUTE = 'default.text.execute'
    static final String SEND = 'default.text.send'
    static final String RECEIVE = 'default.text.receive'
    static final String SELECT = 'default.text.select'
    static final String UPLOAD = 'default.text.upload'
    static final String DOWNLOAD = 'default.text.download'
    static final String CONFIGURE = 'default.text.configure'
    static final String APPROVE = 'default.text.approve'
    static final String REJECT = 'default.text.reject'
    static final String RESET = 'default.text.reset'
    static final String ENABLED = 'default.text.enabled'
    static final String DISABLED = 'default.text.disabled'
    static final String DELETED = 'default.text.deleted'
    static final String TOTAL = 'default.text.total'
    static final String COMPLETE = 'default.text.complete'

    static final String MESSAGE_CONFIRM_UNRECOVERABLE_OPERATION = 'default.text.confirm.message.unrecoverable'
    static final String MESSAGE_CONFIRM_OPERATION = 'default.text.confirm.message'

}