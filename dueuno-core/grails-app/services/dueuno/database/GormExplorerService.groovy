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
package dueuno.database

import dueuno.commons.utils.SqlUtils
import dueuno.elements.core.Elements
import dueuno.tenant.TenantService
import dueuno.types.CustomType
import dueuno.types.Types
import grails.gorm.DetachedCriteria

class GormExplorerService {

    TenantService tenantService
    ConnectionSourceService connectionSourceService

    List getDomainProperties(Class domainClass) {
        List results = []
        domainClass.constrainedProperties.each {
            if (it.value.property.propertyType !in Set) {
                results << it
            }
        }
        return results
    }

    List<String> getDomainColumns(Class domainClass) {
        List<String> results = ['id']
        domainClass.constrainedProperties.each {
            if (it.value.property.propertyType !in Set) {
                results << it.key.toString()
            }
        }
        return results
    }

    Map<String, String> getDomainFieldNames(Class domainClass) {
        Map<String, String> results = [id: 'id']
        domainClass.constrainedProperties.each {
            if (it.value.property.propertyType !in Set) {
                results << [(it.key): it.key.toString()]
            }
        }
        return results
    }

    Map listRecords(String tenantId, Class domainClass, Map filterParams, Map fetchParams) {
        Map result
        tenantService.withTenant(tenantId) {
            Number searchId = filterParams.id as Number
            String searchText = filterParams.find
            Number searchNumber
            try {
                searchNumber = filterParams.find as Long
            } catch (Exception ignore) {
                searchNumber = null
            }

            def query = new DetachedCriteria(domainClass).build {
                // Dynamic associations fetch
                for (property in getDomainProperties(domainClass)) {
                    Class propertyClass = property.value.property.propertyType
                    String propertyName = property.key

                    if (Elements.isDomainClass(propertyClass)) {
                        join propertyName
                    }
                }

                if (searchId) {
                    eq 'id', searchId
                }

                if (searchText) {
                    or {
                        for (property in getDomainProperties(domainClass)) {
                            Class propertyClass = property.value.property.propertyType
                            String propertyName = property.key

                            if (propertyClass in String) {
                                ilike propertyName, "%${searchText}%"

                            } else if (propertyClass in CustomType && Types.getValuePropertyType(propertyClass) == String) {
                                ilike propertyName + '.' + Types.getValuePropertyName(propertyClass), "%${searchText}%"

                            } else if (searchNumber && propertyClass in CustomType && Types.getValuePropertyType(propertyClass) == Number) {
                                eq propertyName + '.' + Types.getValuePropertyName(propertyClass), searchNumber

                            } else if (searchNumber && propertyClass in Number) {
                                eq propertyName, searchNumber
                            }
                        }
                    }
                }
            }

            result = [records: query.list(fetchParams), count: query.count()]
        }
        return result
    }

    Object getRecord(String tenantId, Class domainClass, Serializable id) {
        Object result
        tenantService.withTenant(tenantId) {
            result = domainClass.get(id)
        }
        return result
    }

    Object createRecord(String tenantId, Class domainClass, Map properties) {
        Object result
        tenantService.withTenant(tenantId) {
            domainClass.withTransaction {
                result = domainClass.newInstance(properties)
                result.save(flush: true)
            }
        }
        return result
    }

    Object updateRecord(String tenantId, Class domainClass, Serializable id, Map properties) {
        Object result
        tenantService.withTenant(tenantId) {
            domainClass.withTransaction {
                result = domainClass.get(id)
                result.properties = properties
                result.save(flush: true)
            }
        }
        return result
    }

    void deleteRecord(String tenantId, Class domainClass, Serializable id) {
        tenantService.withTenant(tenantId) {
            domainClass.withTransaction {
                domainClass.get(id).delete(flush: true)
            }
        }
    }

    void executeSql(String connectionSourceName, String sql) {
        SqlUtils.execute(connectionSourceService.getDataSource(connectionSourceName), sql)
    }
}
