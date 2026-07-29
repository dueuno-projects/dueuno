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

import dueuno.application.ApplicationService
import dueuno.types.Money
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j

@Slf4j
@CurrentTenant
@Transactional
class TestService {

    ApplicationService applicationService

    void printTestString() {
        log.info "*** TestService.printTestString() -> shell.name = '${applicationService.shell.id}' ***"
    }

    void installDemo() {
        TDemo demo = new TDemo(
                textfield: 'Ventuno',
                numberfield: 21,
                checkbox: true,
                moneyfield: new Money(21),
        ).save(flush: true)
        TCompany none = new TCompany(name: '000.no.company').save(flush: true)
        TCompany myCompany = new TCompany(name: 'MyCompany S.r.l.').save(flush: true)
        TCompany yourCompany = new TCompany(name: 'YourCompany S.r.l.').save(flush: true)
        TPerson user1 = new TPerson(active: true, company: myCompany, name: 'user1', address: 'Oxford Street', postcode: 'W1').save(flush: true)
        TPerson user2 = new TPerson(active: true, company: myCompany, name: 'user2', address: 'Fifth Avenue', postcode: 'NW1').save(flush: true)
        TPerson user3 = new TPerson(active: true, company: myCompany, name: 'user3', address: 'Baker Street', postcode: '10022').save(flush: true)
        TPerson admin = new TPerson(active: true, company: myCompany, name: 'admin', address: 'Park Avenue', postcode: '10055').save(flush: true)

        myCompany.addToEmployees(user1).save(flush: true, failOnError: true)
        myCompany.addToEmployees(user2).save(flush: true, failOnError: true)
        myCompany.addToEmployees(user3).save(flush: true, failOnError: true)

        new TFruit(name: 'Acerola', image: 'acerola_small.png').save(flush: true)
        new TFruit(name: 'Apple', image: 'apple_small.png').save(flush: true)
        new TFruit(name: 'Apricots', image: 'apricot_small.jpg').save(flush: true)
        new TFruit(name: 'Avocado', image: 'avocado_small.jpg').save(flush: true)
        new TFruit(name: 'Banana', image: 'banana_small.png').save(flush: true)
        new TFruit(name: 'Blackberry', image: 'blackberry_small.jpg').save(flush: true)
        new TFruit(name: 'Blackcurrant', image: 'blackcurrant_small.png').save(flush: true)
        new TFruit(name: 'Blueberries', image: 'blueberries_small.jpg').save(flush: true)
        new TFruit(name: 'Breadfruit', image: 'breadfruit_small.png').save(flush: true)
        new TFruit(name: 'Cantaloupe', image: 'cantaloupe_small.png').save(flush: true)
        new TFruit(name: 'Carambola', image: 'carambola_small.jpg').save(flush: true)
        new TFruit(name: 'Cherimoya', image: 'cherimoya_small.png').save(flush: true)
        new TFruit(name: 'Cherries', image: 'cherries_small.jpg').save(flush: true)
        new TFruit(name: 'Clementine', image: 'clementine_small.png').save(flush: true)
        new TFruit(name: 'Coconut Meat', image: 'coconutmeat_small.png').save(flush: true)
        new TFruit(name: 'Cranberries', image: 'cranberries_small.jpg').save(flush: true)
        new TFruit(name: 'Custard-Apple', image: 'custardapple_small.png').save(flush: true)
        new TFruit(name: 'Date Fruit', image: 'datefruit_small.png').save(flush: true)
        new TFruit(name: 'Durian', image: 'durian_samll.png').save(flush: true)
        new TFruit(name: 'Elderberries', image: 'elderberries_small.png').save(flush: true)
        new TFruit(name: 'Feijoa', image: 'feijoa_small.gif').save(flush: true)
        new TFruit(name: 'Figs', image: 'fig_small.png').save(flush: true)
        new TFruit(name: 'Gooseberries', image: 'gooseberries_small.png').save(flush: true)
        new TFruit(name: 'Grapefruit', image: 'grapefruit_samll.png').save(flush: true)
        new TFruit(name: 'Grapes', image: 'grapes_small.jpg').save(flush: true)
        new TFruit(name: 'Guava', image: 'guava_small.jpg').save(flush: true)
        new TFruit(name: 'Honeydew Melon', image: 'honeydewmelon_small.gif').save(flush: true)
        new TFruit(name: 'Jackfruit', image: 'jackfruit_small.gif').save(flush: true)
        new TFruit(name: 'Java-Plum', image: 'javplum_small.png').save(flush: true)
        new TFruit(name: 'Jujube Fruit', image: 'jujube_small.png').save(flush: true)
        new TFruit(name: 'Kiwifruit', image: 'kiwi_small.gif').save(flush: true)
        new TFruit(name: 'Kumquat', image: 'kumquat_small.png').save(flush: true)
        new TFruit(name: 'Lemon', image: 'lemon_small.gif').save(flush: true)
        new TFruit(name: 'Lime', image: 'lime_small.jpg').save(flush: true)
        new TFruit(name: 'Longan', image: 'longan_small.png').save(flush: true)
        new TFruit(name: 'Loquat', image: 'loquat_small.png').save(flush: true)
    }

}
