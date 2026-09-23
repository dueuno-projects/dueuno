/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.elements.core

import grails.gorm.annotation.Entity
import grails.testing.gorm.DataTest
import spock.lang.Specification

class ElementsSpec extends Specification implements DataTest {

    Class<?>[] getDomainClassesToMock() {
        [ValuesPerson, ValuesCompany] as Class<?>[]
    }

    void 'includes GORM association identifiers in the values map'() {
        given:
        ValuesCompany company = new ValuesCompany(name: 'Company').save(failOnError: true)
        ValuesPerson person = new ValuesPerson(name: 'Person', company: company)

        when:
        Map values = Elements.toMap(person)

        then:
        values.company.is(company)
        values.companyId == company.id
        values.companyId == person.companyId
        !Elements.toMap(company).containsKey('peopleId')
        values._object_.is(person)
    }

    void 'retains null association identifiers'() {
        when:
        Map values = Elements.toMap(new ValuesPerson(name: 'Person'))

        then:
        values.containsKey('companyId')
        values.companyId == null
    }

    void 'supports excluding association identifiers independently of the association'() {
        given:
        ValuesCompany company = new ValuesCompany(name: 'Company').save(failOnError: true)
        ValuesPerson person = new ValuesPerson(name: 'Person', company: company)

        expect:
        !Elements.toMap(person, [], [], ['companyId']).containsKey('companyId')
        Elements.toMap(person, [], [], ['companyId']).company.is(company)
        Elements.toMap(person, [], [], ['company']).companyId == company.id
        !Elements.toMap(person, [], [], ['company']).containsKey('company')
    }
}

@Entity
class ValuesCompany {
    String name
    static hasMany = [people: ValuesPerson]
}

@Entity
class ValuesPerson {
    String name
    ValuesCompany company
    static constraints = {
        company nullable: true
    }
}
