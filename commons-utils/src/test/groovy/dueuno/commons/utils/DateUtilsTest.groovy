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
package dueuno.commons.utils

import groovy.test.GroovyTestCase

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DateUtilsTest extends GroovyTestCase {

    void testCurrentDateAndTimeComponentsAreZeroPadded() {
        assertTrue(DateUtils.currentYear ==~ /\d{4}/)
        assertTwoDigitsInRange(DateUtils.currentMonth, 1, 12)
        assertTwoDigitsInRange(DateUtils.currentDay, 1, 31)
        assertTwoDigitsInRange(DateUtils.currentHour, 0, 23)
        assertTwoDigitsInRange(DateUtils.currentMinute, 0, 59)
        assertTwoDigitsInRange(DateUtils.currentSecond, 0, 59)
    }

    void testFilenameTimestampFormat() {
        assertTrue(DateUtils.filenameTimestamp ==~ /\d{4}-\d{2}-\d{2}-\d{2}-\d{2}-\d{2}/)
    }

    void testToDatePreservesLocalDateTimeDateAndTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2026, 2, 10, 14, 35, 20)
        Date date = DateUtils.toDate(localDateTime)

        assertEquals(localDateTime, DateUtils.toLocalDateTime(date))
    }

    void testToDateConvertsLocalDateAtStartOfDay() {
        LocalDate localDate = LocalDate.of(2026, 2, 10)
        Date date = DateUtils.toDate(localDate)

        assertEquals(localDate, DateUtils.toLocalDate(date))
        assertEquals(LocalTime.MIDNIGHT, DateUtils.toLocalTime(date))
    }

    void testDateConversionsReturnNullForNullValues() {
        assertNull(DateUtils.toDate((LocalDateTime) null))
        assertNull(DateUtils.toDate((LocalDate) null))
        assertNull(DateUtils.toLocalDateTime(null))
        assertNull(DateUtils.toLocalDate(null))
        assertNull(DateUtils.toLocalTime(null))
    }

    void testConvertsDateToLocalDateTimeDateAndTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2026, 2, 10, 14, 35, 20)
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

        assertEquals(localDateTime, DateUtils.toLocalDateTime(date))
        assertEquals(LocalDate.of(2026, 2, 10), DateUtils.toLocalDate(date))
        assertEquals(LocalTime.of(14, 35, 20), DateUtils.toLocalTime(date))
    }

    void testFormatsTemporalValuesWithDefaultPatterns() {
        assertEquals('2026-02-10T14:35', DateUtils.format(LocalDateTime.of(2026, 2, 10, 14, 35, 20)))
        assertEquals('2026-02-10', DateUtils.format(LocalDate.of(2026, 2, 10)))
        assertEquals('14:35', DateUtils.format(LocalTime.of(14, 35, 20)))
    }

    void testFormatsTemporalValuesWithCustomPatterns() {
        assertEquals('10/02/2026 14.35.20', DateUtils.format(LocalDateTime.of(2026, 2, 10, 14, 35, 20), 'dd/MM/yyyy HH.mm.ss'))
        assertEquals('10/02/2026', DateUtils.format(LocalDate.of(2026, 2, 10), 'dd/MM/yyyy'))
        assertEquals('14.35.20', DateUtils.format(LocalTime.of(14, 35, 20), 'HH.mm.ss'))
    }

    void testFormatsDateWithDefaultAndCustomPatterns() {
        Date date = DateUtils.toDate(LocalDate.of(2026, 2, 10))

        assertEquals('2026-02-10', DateUtils.format(date))
        assertEquals('10/02/2026', DateUtils.format(date, 'dd/MM/yyyy'))
    }

    void testReformatsDateString() {
        assertEquals('2026-02-10', DateUtils.reformat('10/02/2026', 'dd/MM/yyyy', 'yyyy-MM-dd'))
        assertEquals('', DateUtils.reformat('', 'dd/MM/yyyy', 'yyyy-MM-dd'))
        assertEquals('', DateUtils.reformat(null, 'dd/MM/yyyy', 'yyyy-MM-dd'))
    }

    void testParsesTemporalValuesWithDefaultPatterns() {
        assertEquals(LocalDateTime.of(2026, 2, 10, 14, 35), DateUtils.parseLocalDateTime('10/02/2026 14:35'))
        assertEquals(LocalDate.of(2026, 2, 10), DateUtils.parseLocalDate('10/02/2026'))
        assertEquals(LocalTime.of(14, 35), DateUtils.parseLocalTime('14:35'))
    }

    void testParsesTemporalValuesWithCustomPatterns() {
        assertEquals(LocalDateTime.of(2026, 2, 10, 14, 35, 20), DateUtils.parseLocalDateTime('2026-02-10 14.35.20', 'yyyy-MM-dd HH.mm.ss'))
        assertEquals(LocalDate.of(2026, 2, 10), DateUtils.parseLocalDate('2026-02-10', 'yyyy-MM-dd'))
        assertEquals(LocalTime.of(14, 35, 20), DateUtils.parseLocalTime('14.35.20', 'HH.mm.ss'))
    }

    void testParseDateWithDefaultAndCustomPatterns() {
        assertEquals('2026-02-10', DateUtils.format(DateUtils.parseDate('10/02/2026')))
        assertEquals('2026-02-10', DateUtils.format(DateUtils.parseDate('2026-02-10', 'yyyy-MM-dd')))
    }

    void testParsingReturnsNullForNullOrEmptyValues() {
        assertNull(DateUtils.parseLocalDateTime(null))
        assertNull(DateUtils.parseLocalDateTime(''))
        assertNull(DateUtils.parseLocalDate(null))
        assertNull(DateUtils.parseLocalDate(''))
        assertNull(DateUtils.parseLocalTime(null))
        assertNull(DateUtils.parseLocalTime(''))
        assertNull(DateUtils.parseDate(null))
        assertNull(DateUtils.parseDate(''))
    }

    private static void assertTwoDigitsInRange(String value, Integer min, Integer max) {
        assertTrue(value ==~ /\d{2}/)
        Integer number = value as Integer
        assertTrue(number >= min)
        assertTrue(number <= max)
    }
}
