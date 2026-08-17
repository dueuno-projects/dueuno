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
package dueuno.security

import dueuno.application.ApplicationService
import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.MultipleCheckbox
import dueuno.elements.controls.SelectX
import dueuno.elements.controls.TextField
import dueuno.elements.core.Feature
import dueuno.elements.style.TextTransform
import dueuno.tenant.TenantService
import grails.plugin.springsecurity.annotation.Secured

/**
 * Group Management
 *
 * @author Gianluca Sartori
 */
@Secured(['ROLE_SECURITY'])
class GroupController implements ElementsController {

    ApplicationService applicationService
    SecurityService securityService
    TenantService tenantService

    def index() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        List cols = ['systemIcon']
        if (isSuperAdmin) cols += ['tenant']
        cols += [
            'name',
            'landingPage',
        ]
        for (authority in (securityService.listAuthority())) {
            cols.add(authority)
        }

        def c = createContent(ContentTable)
        c.table.with {
            filters.with {
                fold = false
                if (isSuperAdmin) {
                    addField(
                        class: SelectX,
                        id: 'tenant',
                        optionsFromRecordset: tenantService.list(),
                        cols: 2,
                    )
                }
                addField(
                    class: TextField,
                    id: 'name',
                    cols: isSuperAdmin ? 10 : 12,
                )
            }

            sortable = [
                tenant: 'asc',
                name  : 'asc',
            ]
            keys = ['id']
            columns = cols
            labels = [
                systemIcon: '',
            ]

            body.eachRow { TableRow row, Map values ->
                values.system = !values.deletable
                if (values.system) {
                    row.actions.removeTailAction()
                    row.cells.systemIcon.icon = 'fa-gear'
                    row.cells.systemIcon.tooltip = 'group.tooltip.system'
                }

                if (isSuperAdmin) {
                    row.cells.tenant.tag = true
                }
            }
        }

        def filters = c.table.filterParams
        def rows = []
        def groups = securityService.listAllGroup(filters, params)
        for (group in groups) {
            def cells = [
                id         : group.id,
                tenant     : group.tenant,
                name       : group.name,
                landingPage: group.landingPage,
                deletable  : group.deletable,
            ]
            for (role in TRole.findAll()) {
                cells[role.authority] = (group.authorities.any { it.authority == role.authority }) ?: null
            }
            rows.add(cells)
        }

        c.table.body = rows
        c.table.paginate = securityService.countGroup(filters)

        display content: c
    }

    private buildForm(TRoleGroup obj = null) {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        def c = obj
            ? createContent(ContentEdit)
            : createContent(ContentCreate)

        c.form.with {
            validate = TRoleGroup
            if (isSuperAdmin) {
                addField(
                    class: SelectX,
                    id: 'tenant',
                    optionsFromRecordset: tenantService.list(),
                    defaultValue: tenantService.defaultTenant.id,
                    search: false,
                    noSelection: false,
                )
            }
            addField(
                class: TextField,
                id: 'name',
                icon: 'fa-shield-halved',
                textTransform: TextTransform.UPPERCASE,
            )
            addField(
                class: SelectX,
                id: 'landingPage',
                optionsFromRecordset: getLandingPages(),
                prettyPrinter: 'LANDING_PAGE',
            )
        }

        return c
    }

    private List<Map> getLandingPages() {
        List<Map> results = []

        for (feature in applicationService.mainFeatures.features) {
            List<Feature> subFeatures = feature.features
            if (subFeatures) {
                for (subFeature in subFeatures) {
                    results.add(id: subFeature.controller, text: featureToText(subFeature))
                }
            } else {
                results.add(id: feature.controller, text: featureToText(feature))
            }
        }

        return results
    }

    private String featureToText(Feature menu) {
        String code = "shell.${menu.namespace ? menu.namespace + "." : ""}${menu.controller}"
        String text = message(code)
        return "<i class='fa-fw fa-solid ${menu.icon} me-2'></i>${text}"
    }

    def create() {
        def c = buildForm()
        c.form.addField(
            class: MultipleCheckbox,
            id: 'authorities',
            optionsFromList: securityService.listAuthority(),
        )
        display content: c, modal: true
    }

    def onCreate() {
        params.tenant = tenantService.get(params.tenant) ?: tenantService.currentTenant
        def obj = securityService.createGroup(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def edit() {
        def obj = securityService.getGroup(params.id)
        def c = buildForm(obj)

        if (obj.name == SecurityService.GROUP_USERS) {
            c.form['name'].readonly = true
            c.form.addField(
                class: MultipleCheckbox,
                id: 'authorities',
                optionsFromList: ['ROLE_USER'],
                textPrefix: controllerName,
                readonly: true,
            )
        } else {
            c.form.addField(
                class: MultipleCheckbox,
                id: 'authorities',
                optionsFromList: securityService.listAuthority(),
                textPrefix: controllerName,
            )
        }

        c.form.values = obj
        c.form['authorities'].value = obj.authorities.collect { it.authority }
        display content: c, modal: true
    }

    def onEdit() {
        params.tenant = tenantService.get(params.tenant) ?: tenantService.currentTenant
        def obj = securityService.updateGroup(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def onDelete() {
        try {
            securityService.deleteGroup(params.id)
            display action: 'index'

        } catch (Exception e) {
            display exception: e
        }
    }

}
