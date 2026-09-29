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
package apptest


import grails.validation.Validateable

class TabsValidator implements Validateable {
    F1 f1
    F2 f2

    static constraints = {
//        f1 validator: { val, obj, errors ->
//            if (!val.t1) errors.rejectValue('f1.t1', 'tabs.f1.error.t1')
//        }
    }
}

class F1 implements Validateable {
    String t1
    String t2

    static constraints = {
        t2 nullable: true, maxSize: 10
    }
}

class F2 implements Validateable {
    String t1
    String t3

    static constraints = {
        t1 nullable: true, maxSize: 10
    }
}
