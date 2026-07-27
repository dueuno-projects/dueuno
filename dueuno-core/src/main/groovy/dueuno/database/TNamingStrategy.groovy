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

import groovy.transform.CompileStatic
import org.grails.orm.hibernate.cfg.PersistentEntityNamingStrategy
import org.grails.orm.hibernate.cfg.domainbinding.hibernate.HibernatePersistentProperty

/**
 * Table names start with "t_" to avoid conflicting with database keywords
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class TNamingStrategy implements PersistentEntityNamingStrategy {

    @Override
    String resolveColumnName(String logicalName) {
        return logicalName
    }

    @Override
    String resolveTableName(String logicalName) {
        String tableName = logicalName.startsWith('T')
                ? logicalName.drop(1)
                : logicalName
        return tableName
    }

    @Override
    String resolveForeignKeyForPropertyDomainClass(HibernatePersistentProperty property) {
        return property.name
    }
}