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

import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@CurrentTenant
@Transactional
@Service(TPerson)
abstract class PersonDataService {
    protected abstract TPerson get(Serializable id)

    protected abstract TPerson getByName(String name)

    protected abstract Long count()

    protected abstract List<TPerson> list(Map args)

    protected abstract List<TPerson> listByNameLike(String name, Map args)

    protected abstract Long countByNameLike(String name)

    protected abstract TPerson save(TPerson obj)

    protected abstract TPerson update(Serializable id, String name)

    protected abstract TPerson delete(Serializable id)

    TPerson create(Map properties) {
        TPerson obj = new TPerson(properties)
        obj.validate()

        if (obj.hasErrors()) {
            return obj
        }

        return save(obj)
    }

    TPerson update(Serializable id, Map properties) {
        TPerson obj = get(id)
        obj.properties = properties
        obj.validate()

        if (obj.hasErrors()) {
            return obj
        }

        return update(id, obj.name)
    }

}
