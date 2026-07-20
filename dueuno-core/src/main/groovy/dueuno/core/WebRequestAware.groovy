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
package dueuno.core

import dueuno.exceptions.ElementsException
import dueuno.utils.LocaleUtils
import grails.web.servlet.mvc.GrailsHttpSession
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.transform.CompileStatic
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.validation.ObjectError
import org.springframework.web.servlet.i18n.SessionLocaleResolver

/**
 * Helper trait to access request bound properties and methods from any class
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
trait WebRequestAware {

    Boolean hasRequest() {
        try {
            WebUtils.retrieveGrailsWebRequest()
            return true

        } catch (Exception ignore) {
            return false
        }
    }

    GrailsWebRequest getGrailsWebRequest() {
        return WebUtils.retrieveGrailsWebRequest()
    }

    /**
     * Returns the current request HttpServletRequest object
     * @return The current request HttpServletRequest object
     */
    HttpServletRequest getRequest() {
        return getGrailsWebRequest().currentRequest
    }

    /**
     * Returns the current request HttpServletResponse object
     * @return The current request HttpServletResponse object
     */
    HttpServletResponse getResponse() {
        return getGrailsWebRequest().currentResponse
    }

    /**
     * Returns the current request params
     * @return The current request params
     */
    GrailsParameterMap getRequestParams() {
        return getGrailsWebRequest().params
    }

    /**
     * Returns whether the current request is marked as coming from a mobile device.
     * @return true if the current request is marked as coming from a mobile device.
     */
    Boolean isMobileDevice() {
        GrailsParameterMap params = getRequestParams()
        return params.mobile || params._21IsMobileDevice ? true : false
    }

    /**
     * Returns the current request controller name
     * @return The current request controller name
     */
    String getControllerName() {
        return getGrailsWebRequest().controllerName
    }

    /**
     * Returns the current request action name
     * @return The current request action name
     */
    String getActionName() {
        return getGrailsWebRequest().actionName
    }

    /**
     * Returns the current request session object (Grails session)
     * @return The current request session object
     */
    GrailsHttpSession getSession() {
        return getGrailsWebRequest().session
    }

    /**
     * Returns the current request locale
     * @return The current request locale
     */
    Locale getLocale() {
        return getGrailsWebRequest().locale
    }

    /**
     * Sets the specified locale to the current session
     *
     * @param locale A locale object
     */
    void setLocale(Locale locale) {
        WebUtils.setSessionAttribute(
                request,
                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
                locale
        )
    }

    String getLocaleFlagCode() {
        return LocaleUtils.getFlagCode(currentLanguage)
    }

    /**
     * A scoped session (Map) accessible only from a specific action
     * @return A scoped session (Map) accessible only from a specific action
     */
    Map getActionSession() {
        return getNamedSession(controllerName + '_' + actionName)
    }

    /**
     * A scoped session (Map) accessible from all actions of a specific controller
     * @return A scoped session (Map) accessible from all actions of a specific controller
     */
    Map getControllerSession() {
        return getNamedSession(controllerName)
    }

    /**
     * A scoped session (Map) accessible with a chosen name (scope)
     * @return A scoped session (Map) accessible with a chosen name (scope)
     */
    Map getNamedSession(String name) {
        if (!session[name]) {
            session[name] = [:]
        }
        return (Map) session[name]
    }

    Boolean hasReturnPoint() {
        return returnPointController || returnPointAction
    }

    /**
     * Sets a 'return point' used by navigation buttons to get back there
     *
     * @param args Named parameters. A 'controller', 'action' or 'params' can be specified to define the return point.
     *             If no args are specified the current controller, action and params will be used to define the return point.
     */
    void setReturnPoint(Map args = [:]) {
        String controller = args.controller ?: controllerName
        String action = args.action ?: actionName

        Map params = (Map) args.params ?: requestParams
        returnPointController = controller
        returnPointAction = action
        returnPointParams = cleanupsParams(params)
    }

    /**
     * Returns the last defined 'return point', use with the Grails 'render' method or the Elements 'display' method
     * @return The 'return point'
     */
    Map returnPoint(Map params = [:]) {
        Map returnParams = returnPointParams + params
        return [
                controller: returnPointController,
                action    : returnPointAction,
                params    : cleanupsParams(returnParams),
        ]
    }

    private Map cleanupsParams(Map params) {
        params.remove('_21TransitionRendered')
        params.remove('controller')
        params.remove('action')
        return params
    }

    /**
     * Sets a controller to which return when using {@link WebRequestAware#setReturnPoint(java.util.Map)}
     *
     * @param controller The controller to set as 'return point'
     */
    void setReturnPointController(String controller) {
        if (session) {
            session['_21ReturnPointController'] = controller
        }
    }

    /**
     * Sets an action to which return when using {@link WebRequestAware#setReturnPoint(java.util.Map)}
     *
     * @param action The action to set as 'return point'
     */
    void setReturnPointAction(String action) {
        if (session) {
            session['_21ReturnPointAction'] = action
        }
    }

    /**
     * Sets the params to pass when using {@link WebRequestAware#setReturnPoint(java.util.Map)}
     *
     * @param params The params to pass to the 'return point'
     */
    void setReturnPointParams(Map params) {
        if (session) {
            session['_21ReturnPointParams'] = params
        }
    }

    /**
     * Returns the controller to which return when using
     * {@link WebRequestAware#setReturnPoint(java.util.Map }
            * @ return the ' return point ' controller
     */
    String getReturnPointController() {
        return session ? (String) session['_21ReturnPointController'] : null
    }

    /**
     * Returns the action to which return when using
     * {@link WebRequestAware#setReturnPoint(java.util.Map)}
     * @return the 'return point' action
     */
    String getReturnPointAction() {
        return session ? (String) session['_21ReturnPointAction'] : null
    }

    /**
     * Returns the params to pass when using
     * {@link WebRequestAware#setReturnPoint(java.util.Map)}
     * @return the 'return point' params
     */
    Map getReturnPointParams() {
        if (!session) {
            return [:]
        }

        return session['_21ReturnPointParams'] as Map ?: [:]
    }

    /**
     * Used in controllers/actions to make sure a required parameter exists in 'params' or a copy of it is stored in
     * the session. Useful to avoid managing params back and forth a page and its actions
     *
     * @param paramName the name of the required param
     * @param defaultValue value to return in case param is null
     *
     * @return the param value
     */
    Object requireParam(String paramName, Object defaultValue = null) {
        Object paramValue = requestParams[paramName]
        Object savedValue = controllerSession[paramName]
        Object result

        if (paramValue != null) {
            result = paramValue
            controllerSession[paramName] = paramValue

        } else if (savedValue != null) {
            result = savedValue

        } else if (defaultValue != null) {
            result = defaultValue
        }

        if (result == null) {
            throw new ElementsException("The required parameter '$paramName' has not been passed to the request and " +
                    "no value was stored in the 'controllerSession': params = ${requestParams}, " +
                    "controllerSession = ${controllerSession}")
        }

        return result
    }

    /**
     * Returns the localised string associated with the passed code and args as found in /i18n/messages.properties.
     *
     * @param code (mandatory) the code to lookup into /i18n/messages.properties
     * @param args (optional) a list of variables that can be used inside /i18n/messages.properties with the following notation: {0} first item in list, {1} second item, etc.
     *
     * @return the localised string associated with the passed 'code'
     */
    String message(String code, List args = []) {
        return PrettyPrinter.message(locale, code, args)
    }

    String message(String code, Object[] args) {
        return PrettyPrinter.message(locale, code, args as List)
    }

    String message(ObjectError error) {
        return PrettyPrinter.message(locale, error)
    }

    String messageOrBlank(String code, List args = []) {
        return PrettyPrinter.messageOrBlank(locale, code, args)
    }

    /**
     * Returns a pretty printed string of the specified value object using the specified properties and the user
     * session properties
     *
     * @param value The value to print
     * @param properties The properties for the PrettyPrinter
     *
     * @return
     */
    String prettyPrint(Object value, PrettyPrinterProperties properties = new PrettyPrinterProperties()) {
        if (properties.locale == null) properties.locale = locale
        if (properties.decimalFormat == null) properties.decimalFormat = decimalFormat
        if (properties.prefixedUnit == null) properties.prefixedUnit = prefixedUnit
        if (properties.symbolicCurrency == null) properties.symbolicCurrency = symbolicCurrency
        if (properties.symbolicQuantity == null) properties.symbolicQuantity = symbolicQuantity
        if (properties.invertedMonth == null) properties.invertedMonth = invertedMonth
        if (properties.twelveHours == null) properties.twelveHours = twelveHours
        if (properties.firstDaySunday == null) properties.firstDaySunday = firstDaySunday

        return PrettyPrinter.print(value, properties)
    }

    String prettyPrint(Object value, String prettyPrinter, PrettyPrinterProperties properties = new PrettyPrinterProperties()) {
        properties.prettyPrinter = prettyPrinter
        return prettyPrint(value, properties)
    }

    Object transform(String transformerName, Object value) {
        return Transformer.transform(transformerName, value)
    }

    String getCurrentLanguage() {
        locale.language
    }

    void setCurrentLanguage(String language) {
        if (!language) {
            return
        }

        locale = new Locale(language)
    }

    /**
     * Sets the 'decimal symbol' format configured by the current user in its user profile.
     * Can be one of the following:
     * <ul>
     *     <li>'ISO_COM' -> 0 000,00</li>
     *     <li>'ISO_DOT' -> 0 000.00</li>
     * </ul>
     *
     * @param value The decimal symbol
     */
    void setDecimalFormat(String value) {
        session['_21DecimalFormat'] = value
    }

    /**
     * Returns the 'decimal symbol' format configured by the current user in its user profile.
     * @return The 'decimal symbol' format configured by the current user in its user profile
     */
    String getDecimalFormat() {
        return session['_21DecimalFormat'] ?: PrettyPrinterDecimalFormat.ISO_COM as String
    }

    /**
     * Sets the 'unit of measure' format configured by the current user in its user profile
     *
     * @param value The 'unit of measure' format configured by the current user in its user profile
     */
    void setPrefixedUnit(Boolean value) {
        session['_21PrefixedUnit'] = value
    }

    /**
     * Returns the 'unit of measure' format configured by the current user in its user profile
     * @return The 'unit of measure' format configured by the current user in its user profile
     */
    Boolean getPrefixedUnit() {
        Boolean prefixedUnit = session['_21PrefixedUnit']
        return prefixedUnit == null ? false : prefixedUnit
    }

    /**
     * Sets the 'currency format' configured by the current user in its user profile
     *
     * @param value The 'currency format' configured by the current user in its user profile
     */
    void setSymbolicCurrency(Boolean value) {
        session['_21SymbolicCurrency'] = value
    }

    /**
     * Returns the 'currency format' configured by the current user in its user profile
     * @return The 'currency format' configured by the current user in its user profile
     */
    Boolean getSymbolicCurrency() {
        Boolean symbolicCurrency = session['_21SymbolicCurrency']
        return symbolicCurrency == null ? true : symbolicCurrency
    }

    /**
     * Sets the 'quantity format' configured by the current user in its user profile
     *
     * @param value The 'quantity format' configured by the current user in its user profile
     */
    void setSymbolicQuantity(Boolean value) {
        session['_21SymbolicQuantity'] = value
    }

    /**
     * Returns the 'quantity format' configured by the current user in its user profile
     * @return The 'quantity format' configured by the current user in its user profile
     */
    Boolean getSymbolicQuantity() {
        Boolean symbolicQuantity = session['_21SymbolicQuantity']
        return symbolicQuantity == null ? true : symbolicQuantity
    }

    /**
     * Sets the 'month date' format configured by the current user in its user profile
     *
     * @param value The 'month date' format configured by the current user in its user profile
     */
    void setInvertedMonth(Boolean value) {
        session['_21InvertedMonth'] = value
    }

    /**
     * Returns the 'month date' format configured by the current user in its user profile
     * @return The 'month date' format configured by the current user in its user profile
     */
    Boolean getInvertedMonth() {
        Boolean invertedMonth = session['_21InvertedMonth']
        return invertedMonth == null ? false : invertedMonth
    }

    /**
     * Sets the 'hour' format configured by the current user in its user profile
     *
     * @param value The 'hour' format configured by the current user in its user profile
     */
    void setTwelveHours(Boolean value) {
        session['_21TwelveHours'] = value
    }

    /**
     * Returns the 'hour' format configured by the current user in its user profile
     * @return The 'hour' format configured by the current user in its user profile
     */
    Boolean getTwelveHours() {
        Boolean twelveHours = session['_21TwelveHours']
        return twelveHours == null ? false : twelveHours
    }

    /**
     * Sets the 'week' format configured by the current user in its user profile
     *
     * @param value The 'week' format configured by the current user in its user profile
     */
    void setFirstDaySunday(Boolean value) {
        session['_21FirstDaySunday'] = value
    }

    /**
     * Returns the 'week' format configured by the current user in its user profile
     * @return the 'week' format configured by the current user in its user profile
     */
    Boolean getFirstDaySunday() {
        Boolean firstDaySunday = session['_21FirstDaySunday']
        return firstDaySunday == null ? false : firstDaySunday
    }

    /**
     * Sets the 'fontSize' parameter configured by the current user in its user profile
     * @param value The 'fontSize' parameter configured by the current user in its user profile
     */
    void setFontSize(Integer value) {
        session['_21FontSize'] = value
    }

    /**
     * Returns the 'fontSize' parameter configured by the current user in its user profile
     * @return the 'fontSize' parameter configured by the current user in its user profile
     */
    Integer getFontSize() {
        Integer fontSize = session['_21FontSize'] as Integer
        return fontSize ?: 14
    }

    /**
     * Sets the 'guiStyle' parameter configured by the current user in its user profile
     * @param value The 'guiStyle' parameter configured by the current user in its user profile
     */
    void setGuiStyle(String value) {
        session['_21GuiStyle'] = value
    }

    /**
     * Returns the 'guiStyle' parameter configured by the current user in its user profile
     * @return the 'guiStyle' parameter configured by the current user in its user profile
     */
    String getGuiStyle() {
        return session['_21GuiStyle'] ?: GuiStyle.ROUNDED as String
    }

    /**
     * Sets the 'animations' parameter configured by the current user in its user profile
     *
     * @param value The 'animations' parameter configured by the current user in its user profile
     */
    void setAnimations(Boolean value) {
        session['_21Animations'] = value
    }

    /**
     * Returns the 'animations' parameter configured by the current user in its user profile
     * @return the 'animations' parameter configured by the current user in its user profile
     */
    Boolean getAnimations() {
        Boolean animations = session['_21Animations']
        return animations == null ? true : animations
    }

    /**
     * Defines whether or not to display hints for the developers (eg. name of the fields, etc.).
     * It's always 'false' when in TEST and PROD environments.
     * Can be set to 'false' to demo the application in DEV environment.
     *
     * @param switchOn true to switch the feature on
     */
    void setDevDisplayHints(Boolean switchOn) {
        try {
            session['_21DevDisplayHints'] = switchOn

        } catch (Exception ignore) {
            //no-op
        }
    }

    /**
     * Returns whether or not to display hints for the developers
     * @return whether or not to display hints for the developers
     */
    Boolean getDevDisplayHints() {
        try {
            Boolean result = session['_21DevDisplayHints'] ?: false
            return result

        } catch (Exception ignore) {
            return false
        }
    }
}
