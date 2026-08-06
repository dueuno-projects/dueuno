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

import grails.gorm.DetachedCriteria
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.transactions.Transactional
import groovy.contracts.Requires
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@CurrentTenant
@Transactional
@CompileStatic
class CompanyService {

    @CompileDynamic
    private DetachedCriteria<TCompany> buildQuery(Map filterParams) {
        def query = TCompany.where {}

        if (filterParams.containsKey('id')) query = query.where { id == filterParams.id }
        if (filterParams.containsKey('name')) query = query.where { name == filterParams.name }

        if (filterParams.find) {
            String search = filterParams.find.replaceAll('\\*', '%')
            query = query.where {
                name =~ "%${search}%"
            }
        }

        // Add additional filters here

        return query
    }

    TCompany get(Serializable id) {
        // Add single-sided relationships here (Eg. references to other Domain Objects)
        Map fetch = [
            employees: 'join',
        ]

        return buildQuery(id: id).get(fetch: fetch)
    }

    List<TCompany> list(Map filterParams = [:], Map fetchParams = [:]) {
        if (!fetchParams.sort) fetchParams.sort = [dateCreated: 'asc']

        // Add single-sided relationships here (Eg. references to other DomainObjects)
        // DO NOT add hasMany relationships, you are going to have troubles with pagination
        fetchParams.fetch = [:
//                employees: 'join',
        ]

        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    Number count(Map filterParams = [:]) {
        def query = buildQuery(filterParams)
        return query.count()
    }

    TCompany create(Map args = [:]) {
        if (args.failOnError == null) args.failOnError = false

        TCompany obj = new TCompany(args)
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @CompileDynamic
    @Requires({ args.id })
    TCompany update(Map args = [:]) {
        if (args.failOnError == null) args.failOnError = false

        TCompany obj = get(args.id)
        obj.properties = args
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    void delete(Serializable id) {
        TCompany obj = get(id)
        obj.delete(flush: true)
    }
}
