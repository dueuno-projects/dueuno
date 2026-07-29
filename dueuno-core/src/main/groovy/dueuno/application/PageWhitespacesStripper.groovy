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

import org.apache.logging.log4j.util.Chars
import org.grails.gsp.GroovyPageSourceDecorator

class PageWhitespacesStripper implements GroovyPageSourceDecorator {

    @Override
    StringBuilder decorate(StringBuilder source) {
        return removeBlankSpace(source)
    }

    private StringBuilder removeBlankSpace(StringBuilder sb) {
        boolean process = true
        int j = 0
        int i = 0
        long length = sb.length()
        char prev = '*'

        while (i < length) {
            char[] tag = '12345678'
            int tagLength = tag.length
            if (i < sb.length() - tagLength) {
                sb.getChars(i, i + tagLength, tag as char[], 0)
            }

            boolean isScript = tag == '<script>' as char[]
            boolean isStyle = (tag as String).substring(0, 7) == '<style>'

            if (isStyle) {
                tagLength = 7
                tag = (tag as String).substring(0, 7) as char[]
            }

            if (isScript || isStyle) {
                process = false
                for (k in j..(j + tagLength - 1)) {
                    sb.setCharAt(k, sb.charAt(i))
                    j++
                    i++
                }
                continue
            }

            char c = sb.charAt(i)
            if (c == '<') {
                process = false
            } else if (c == '>') {
                process = true
            }

            boolean skipChar = false
                    || c == Chars.LF
                    || process && Character.isWhitespace(c)
                    || !process && prev != Chars.LF && Character.isWhitespace(prev) && Character.isWhitespace(c)

            if (!skipChar) {
                sb.setCharAt(j, c)
                j++
            }

            prev = c
            i++
        }

        sb.delete(j, sb.length())
        return sb
    }
}

