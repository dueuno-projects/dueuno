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
package dueuno

import dueuno.validation.ValidationError
import grails.validation.Validateable
import grails.validation.ValidationErrors

class BookValidator implements Validateable {

    String title
    String author

    BookService bookService

    static constraints = {
        title validator: { Object val, BookValidator obj, ValidationErrors errors ->
            if (obj.bookService.getByTitle(val)) {
                errors.rejectValue('title', ValidationError.IS_NOT_UNIQUE)
            }
        }
    }
}