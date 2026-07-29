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
package dueuno.elements.core

import dueuno.commons.utils.ObjectUtils
import grails.converters.JSON
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.core.artefact.DomainClassArtefactHandler
import org.springframework.context.support.GenericApplicationContext

/**
 * Elements utils. Internal use only.
 *
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class Elements {

    private static List<String> componentsRegistry = []

    static List<String> getComponentsRegistry() {
        return componentsRegistry
    }

    static void registerComponents(String elementsImplementation) {
        log.info "Registering components '${elementsImplementation}'"
        if (!componentsRegistry.contains(elementsImplementation)) {
            componentsRegistry.add(elementsImplementation)
        }
    }

    static Object getBean(String name) {
        return Holders.applicationContext.getBean(name)
    }

    static Object containsBean(String name) {
        return Holders.applicationContext.containsBean(name)
    }

    static Object removeBean(String name) {
        return (Holders.applicationContext as GenericApplicationContext).removeBeanDefinition(name)
    }

    static Boolean isDomainClass(Class clazz) {
        return DomainClassArtefactHandler.isDomainClass(clazz, false)
    }

    static String getDomainClassName(Class clazz) {
        if (isDomainClass(clazz.superclass)) {
            return clazz.superclass.canonicalName
        } else {
            return clazz.canonicalName
        }
    }

    static final List<String> groovyExclusions = [
            'class',
    ]

    static final List<String> gormExclusions = [
            // GORM lazy fetching may execute queries each time we access
            // one of the following properties
            // Defined watching a debug session, see also: Grails `LazyMetaPropertyMap`
            'all',
            'count',
            'transients',
            'properties',
            'constrainedProperties',
            'gormPersistentEntity',
            'gormDynamicFinders',
            'dirtyPropertyNames',
            'dirty',
            'hasMany',
            'belongsTo',
            'constraints',
            'mapping',
            'errors',
            'attached',
    ]

    static Map toMap(Object object, List<String> columns = [], List<String> includes = [], List<String> excludes = []) {
        if (!object) {
            return [:]
        }

        if (object in Map) {
            return object as Map
        }

        Map results = [:]

        if (columns && object in Collection) {
            // Collection elements will be assigned to each column in order
            Integer i = 0
            for (column in columns) {
                results[column] = (object as Collection)[i]
                i++
            }

        } else if (isDomainClass(object.class)) {
            results.put('_object_', object)

            Set hasMany = []
            if (object.hasProperty('hasMany')) {
                hasMany = (object['hasMany'] as Map).keySet()
            }

            excludes += groovyExclusions
            excludes += gormExclusions

            for (property in object.metaClass.properties) {
                String name = property.name
                if ((name in excludes || name in hasMany) && (name !in columns || name !in includes)) {
                    continue
                }

                Object value = ObjectUtils.getValue(object, name)
                results.put(name, value)
            }

            for (propertyName in includes) {
                Object value = ObjectUtils.getValue(object, propertyName)
                results.put(propertyName, value)
            }

        } else {
            results.put('_object_', object)

            excludes += groovyExclusions

            for (property in object.properties) {
                String name = property.key
                if (name in excludes && (name !in columns || name !in includes)) {
                    continue
                }

                Object value = ObjectUtils.getValue(object, name)
                results.put(name, value)
            }
        }

        return results
    }

    static String encodeAsJSON(Object obj) {
        return obj as JSON
    }
}
