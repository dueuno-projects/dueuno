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
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

/**
 * Remove "t" prefix from table names if the domain class name starts with "T"
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class TNamingStrategy extends PhysicalNamingStrategySnakeCaseImpl {

    @Override
    Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        if (logicalName != null && logicalName.text.startsWith('T')) {
            logicalName = Identifier.toIdentifier(logicalName.text.substring(1), logicalName.quoted)
        }

        return super.toPhysicalTableName(logicalName, jdbcEnvironment)
    }

    @Override
    protected Identifier quotedIdentifier(Identifier quotedName) {
        return unquotedIdentifier(quotedName).quoted()
    }

}
