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
package dueuno.application

import dueuno.commons.utils.FileUtils
import dueuno.commons.utils.StringUtils
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.contracts.Requires
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.annotation.PostConstruct

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class ApplicationPropertyService extends PropertyService {

    ApplicationService applicationService

    void install() {
        // System
        String root = "${FileUtils.workingDirectory}${applicationService.applicationName}/"
        setDirectory('APPLICATION_HOME_DIR', root)
        setDirectory('NEW_TENANT_DIR', "${root}tenants")

        // Languages
        setString('AVAILABLE_LANGUAGES', '')
        setString('EXCLUDED_LANGUAGES', '')
        setString('DEFAULT_LANGUAGE', 'en')

        // Menus
        setBoolean('DISPLAY_MENU', true)
        setBoolean('DISPLAY_MENU_SEARCH', true)
        setBoolean('DISPLAY_HOME_BUTTON', true)
        setBoolean('DISPLAY_USER_MENU', true)
    }

    @PostConstruct
    void init() {
        inMemoryProperties['SYSTEM'] = [:] as Map
    }

    @CompileDynamic
    DetachedCriteria<TApplicationProperty> buildQuery(Map filters) {
        def query = TApplicationProperty.where {}

        if (filters) {
            if (filters.type) query = query.where { type == filters.type }
            if (filters.validation != null) query = query.where { validation != '' }
            if (filters.find) {
                String search = filters.find.replaceAll('\\*', '%')
                query = query.where {
                    true
                            || name =~ "%${search}%"
                            || string =~ "%${search}%"
                            || stringDefault =~ "%${search}%"
                            || filename =~ "%${search}%"
                            || filenameDefault =~ "%${search}%"
                            || directory =~ "%${search}%"
                            || directoryDefault =~ "%${search}%"
                            || url =~ "%${search}%"
                            || urlDefault =~ "%${search}%"
                }
            }
        }

        return query
    }

    TApplicationProperty get(Serializable id) {
        TApplicationProperty p = TApplicationProperty.get(id) as TApplicationProperty
        if (p) p.refresh()
        return p
    }

    @CompileDynamic
    private TApplicationProperty getByName(String name) {
        TApplicationProperty p = TApplicationProperty.findByName(name)
        if (p) p.refresh()
        return p
    }

    List<TApplicationProperty> list(Map filterParams = [:], Map fetchParams = [:]) {
        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    Number count(Map filters = [:]) {
        def query = buildQuery(filters)
        return query.count()
    }

    @Transactional
    private TApplicationProperty create(Map args) {
        TApplicationProperty obj = new TApplicationProperty(args)
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @Transactional
    @CompileDynamic
    @Requires({ args.id })
    private TApplicationProperty update(Map args) {
        if (args.failOnError == null) args.failOnError = false

        TApplicationProperty.withTransaction {
            TApplicationProperty obj = get(args.id)
            obj.properties = args
            obj.save(flush: true, failOnError: args.failOnError)
            return obj
        }
    }

    @Override
    @Transactional
    void setValue(PropertyType type, String name, Object value, Object defaultValue = null, String validation = null) {
        TApplicationProperty property = getByName(name)
        String typeName = StringUtils.screamingSnakeToCamel(type as String)
        String typeNameDefault = typeName + 'Default'

        Object oldValue = null

        if (property) {
            oldValue = property[typeName]
            Map updatedProperty = [
                    id        : property.id,
                    (typeName): value,
                    validation: validation ?: property.validation,
            ]
            if (type != PropertyType.PASSWORD) {
                updatedProperty[typeNameDefault] = defaultValue ?: property[typeNameDefault]
            }
            update(updatedProperty)

        } else {
            Map newProperty = [
                    name      : name,
                    type      : type,
                    (typeName): value,
                    validation: validation,
            ]
            if (type != PropertyType.PASSWORD) {
                newProperty[typeNameDefault] = defaultValue
            }
            create(newProperty)
        }

        inMemoryProperties['SYSTEM'][name] = value
        if (onChangeRegistry[name]) {
            log.info "APPLICATION - Property changed '$name' = '$value'"
            onChangeRegistry[name].call(oldValue, value, defaultValue)
        }

        validateAll()
    }

    @Override
    Object getValue(PropertyType type, String name, Boolean reload = false) {
        if (inMemoryProperties['SYSTEM'].containsKey(name) && !reload) {
            return inMemoryProperties['SYSTEM'][name]
        }

        String typeName = StringUtils.screamingSnakeToCamel(type as String)
        TApplicationProperty property = getByName(name)
        if (!property) {
            return null

        }

        Object value = property[typeName]

        inMemoryProperties['SYSTEM'][name] = value
        return value
    }

    void validateAll() {
        List<TApplicationProperty> properties = list()
        for (property in properties) {
            switch (property.type as PropertyType) {
                case PropertyType.FILENAME:
                    def validation = validateFilename(property.filename)
                    update(id: property.id, validation: validation)
                    break

                case PropertyType.DIRECTORY:
                    def validation = validateDirectory(property.directory)
                    update(id: property.id, validation: validation)
                    break

                case PropertyType.URL:
                    def validation = validateUrl(property.url)
                    update(id: property.id, validation: validation)
                    break
            }
        }
    }

    @Transactional
    void delete(Serializable id) {
        TApplicationProperty obj = get(id)
        obj.delete(flush: true)
    }
}
