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

import grails.gorm.DetachedCriteria
import grails.gorm.multitenancy.CurrentTenant

@CurrentTenant
class MovieService {

    private DetachedCriteria<TMovie> buildQuery(Map filters) {
        def query = TMovie.where {}

        if (filters.containsKey('id')) query = query.where { id == filters.id }

        if (filters.find) {
            String search = filters.find.replaceAll('\\*', '%')
            query = query.where {
                title =~ "%${search}%"
            }
        }

        // Add additional filters here

        return query
    }

    TMovie get(Serializable id) {
        // Add any relationships here (Eg. references to other DomainObjects or hasMany)
        Map fetch = [
                actors: 'join',
        ]

        return buildQuery(id: id).get(fetch: fetch)
    }

    List<TMovie> list(Map filterParams = [:], Map fetchParams = [:]) {
        if (!fetchParams.sort) fetchParams.sort = [dateCreated: 'asc']

        // Add single-sided relationships here (Eg. references to other Domain Objects)
        // DO NOT add hasMany relationships, you are going to have troubles with pagination
//        params.fetch = [
//                relationshipName: 'join',
//        ]

        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    Integer count(Map filters = [:]) {
        def query = buildQuery(filters)
        return query.count()
    }

    TMovie create(Map args = [:]) {
        if (args.failOnError == null) args.failOnError = false

        TMovie obj = new TMovie(args)
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @Requires({ args.id })
    TMovie update(Map args = [:]) {
        if (args.failOnError == null) args.failOnError = false

        TMovie obj = get(args.id)
        obj.properties = args
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    void delete(Serializable id) {
        TMovie obj = get(id)
        if (obj) {
            obj.delete(flush: true)
        }
    }
}
