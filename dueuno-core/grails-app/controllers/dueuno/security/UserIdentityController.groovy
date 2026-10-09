/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.security

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentTable
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j

@Slf4j
@Secured(['ROLE_SECURITY'])
class UserIdentityController implements ElementsController {

    AuthenticationIdentityService authenticationIdentityService
    SecurityService securityService

    def index() {
        TUser user = securityService.getUser(params.userId as Serializable)
        List<TUserAuthenticationIdentity> identities = user
            ? authenticationIdentityService.listByUser(user.id)
            : []

        def c = createContent(ContentTable)
        c.header.removeNextButton()
        c.table.with {
            keys = [
                'id',
                'userId',
            ]
            columns = [
                'provider.providerType',
                'issuer',
                'subject',
            ]
            labels = [
                'provider.providerType': 'authenticationIdentity.providerType',
                issuer                 : 'authenticationIdentity.issuer',
                subject                : 'authenticationIdentity.subject',
            ]
            sortable = [
                issuer : 'asc',
                subject: 'asc',
            ]
            actions.removeDefaultAction()
            body.eachRow { TableRow row, Map values ->
                values.userId = user.id
            }
            body = identities
        }

        display content: c, modal: true, large: true
    }

    def onDelete() {
        authenticationIdentityService.delete(params.id)
        display action: 'index', params: [userId: params.userId]
    }

}
