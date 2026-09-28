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
package dueuno.types

import dueuno.tenant.TenantPropertyService
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class QuantityService {

    TenantPropertyService tenantPropertyService

    List<String> listUnitByOffset(Integer offset = 0, Integer max = 0, List<String> unitList = null) {
        if (!unitList) unitList = QuantityUnit.values()*.name()

        List<String> results = []
        def i = 0
        for (unit in unitList) {
            if (i >= offset && (max == 0 || i < offset + max)) {
                results.add(unit)
            }
            i++
        }
        return results
    }

    List<String> listAllUnits(Boolean unit = true, Boolean mass = true, Boolean length = true, Boolean area = true, Boolean volume = true, Boolean volumeLitre = true, Boolean power = true, Boolean time = true) {
        List<String> results = []
        if (unit) results.addAll(listUnit())
        if (mass) results.addAll(listUnitMass())
        if (length) results.addAll(listUnitLength())
        if (area) results.addAll(listUnitArea())
        if (volume) results.addAll(listUnitVolume())
        if (volumeLitre) results.addAll(listUnitVolumeLitre())
        if (time) results.addAll(listUnitTime())
        if (power) results.addAll(listUnitPower())
        return results
    }

    List<String> listUnit() {
        String units = tenantPropertyService.getString('UNIT')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnit(QuantityUnit... units) {
        enableUnit(units as String[])
    }

    void enableUnit(List<String> units) {
        enableUnit(units as String[])
    }

    void enableUnit(String... units) {
        tenantPropertyService.setString('UNIT', units.join(','))
    }

    List<String> listUnitMass() {
        String units = tenantPropertyService.getString('UNIT_MASS')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitMass(QuantityUnit... units) {
        enableUnitMass(units as String[])
    }

    void enableUnitMass(List<String> units) {
        enableUnitMass(units as String[])
    }

    void enableUnitMass(String... units) {
        tenantPropertyService.setString('UNIT_MASS', units.join(','))
    }

    List<String> listUnitLength() {
        String units = tenantPropertyService.getString('UNIT_LENGTH')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitLength(QuantityUnit... units) {
        enableUnitLength(units as String[])
    }

    void enableUnitLength(List<String> units) {
        enableUnitLength(units as String[])
    }

    void enableUnitLength(String... units) {
        tenantPropertyService.setString('UNIT_LENGTH', units.join(','))
    }

    List<String> listUnitArea() {
        String units = tenantPropertyService.getString('UNIT_AREA')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitArea(QuantityUnit... units) {
        enableUnitArea(units as String[])
    }

    void enableUnitArea(List<String> units) {
        enableUnitArea(units as String[])
    }

    void enableUnitArea(String... units) {
        tenantPropertyService.setString('UNIT_AREA', units.join(','))
    }

    List<String> listUnitVolume() {
        String units = tenantPropertyService.getString('UNIT_VOLUME')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitVolume(QuantityUnit... units) {
        enableUnitVolume(units as String[])
    }

    void enableUnitVolume(List<String> units) {
        enableUnitVolume(units as String[])
    }

    void enableUnitVolume(String... units) {
        tenantPropertyService.setString('UNIT_VOLUME', units.join(','))
    }

    List<String> listUnitVolumeLitre() {
        String units = tenantPropertyService.getString('UNIT_LITRE')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitVolumeLitre(QuantityUnit... units) {
        enableUnitVolumeLitre(units as String[])
    }

    void enableUnitVolumeLitre(List<String> units) {
        enableUnitVolumeLitre(units as String[])
    }

    void enableUnitVolumeLitre(String... units) {
        tenantPropertyService.setString('UNIT_LITRE', units.join(','))
    }

    List<String> listUnitTime() {
        String units = tenantPropertyService.getString('UNIT_TIME')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitTime(QuantityUnit... units) {
        enableUnitTime(units as String[])
    }

    void enableUnitTime(List<String> units) {
        enableUnitTime(units as String[])
    }

    void enableUnitTime(String... units) {
        tenantPropertyService.setString('UNIT_TIME', units.join(','))
    }

    List<String> listUnitPower() {
        String units = tenantPropertyService.getString('UNIT_POWER')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitPower(QuantityUnit... units) {
        enableUnitPower(units as String[])
    }

    void enableUnitPower(List<String> units) {
        enableUnitPower(units as String[])
    }

    void enableUnitPower(String... units) {
        tenantPropertyService.setString('UNIT_POWER', units.join(','))
    }

    List<String> listUnitEnergy() {
        String units = tenantPropertyService.getString('UNIT_ENERGY')
        if (units) {
            return units.split(',') as List<String>
        } else {
            return []
        }
    }

    void enableUnitEnergy(QuantityUnit... units) {
        enableUnitEnergy(units as String[])
    }

    void enableUnitEnergy(List<String> units) {
        enableUnitEnergy(units as String[])
    }

    void enableUnitEnergy(String... units) {
        tenantPropertyService.setString('UNIT_ENERGY', units.join(','))
    }

}
