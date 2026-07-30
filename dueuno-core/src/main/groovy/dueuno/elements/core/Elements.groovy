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
import org.grails.datastore.mapping.model.PersistentEntity

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

    static Map toMap(Object object, List<String> properties = [], List<String> includes = [], List<String> excludes = []) {
        if (!object) {
            return [:]
        }

        Map results = [:]

        if (object in Map) {
            results.putAll(object as Map)

        } else if (object in Collection) { // Collection elements will be assigned to each property following their order
            Integer i = 0
            for (property in properties) {
                results[property] = (object as Collection)[i]
                i++
            }

        } else if (isDomainClass(object.class)) {
            PersistentEntity entity = Holders.grailsApplication.mappingContext.getPersistentEntity(object.class.name)

            // id is not present in persistentProperties, we add it explicitly
            if (entity.identity && !(entity.identity.name in excludes)) {
                String name = entity.identity.name
                results[name] = ObjectUtils.getValue(object, name)
            }

            for (persistentProperty in entity.persistentProperties) {
                String name = persistentProperty.name
                if (name in excludes) {
                    continue
                }

                Object value = ObjectUtils.getValue(object, name)
                results.put(name, value)
            }

            for (propertyName in includes) {
                Object value = ObjectUtils.getValue(object, propertyName)
                results.put(propertyName, value)
            }

        } else { // POJOs
            for (property in object.properties) {
                String name = property.key
                if (name in excludes) {
                    continue
                }

                Object value = ObjectUtils.getValue(object, name)
                results.put(name, value)
            }
        }

        results.put('_object_', object)
        return results
    }

    static String encodeAsJSON(Object obj) {
        return obj as JSON
    }
}
