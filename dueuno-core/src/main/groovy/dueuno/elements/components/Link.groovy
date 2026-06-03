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
package dueuno.elements.components

import dueuno.core.LinkDefinition
import dueuno.elements.ComponentEvent
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class Link extends Label {

    /** The link */
    LinkDefinition linkDefinition
    String eventName

    Link(Map args = [:]) {
        super(args)

        linkDefinition = new LinkDefinition(args)
        linkDefinition.action = args.action ?: 'index'

        tag = args.tag == null ? false : args.tag

        html = args.html
        containerSpecs.label = ''
        containerSpecs.help = ''

        eventName = 'click'
        setOnEvent(args.onClick as String)
    }

    void setOnEvent(String onEvent = null) {
        on(linkDefinition.properties + [
                event: eventName,
                action: onEvent ?: linkDefinition.action,
                infoMessage: message(linkDefinition.infoMessage, linkDefinition.infoMessageArgs),
                confirmMessage: message(linkDefinition.confirmMessage, linkDefinition.confirmMessageArgs),
        ])
    }

    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                loading: loading,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    String toString() {
        String ns = linkDefinition.namespace ?: ''
        String path = "${ns}/${linkDefinition.controller}/${linkDefinition.action}"
        String queryParams = linkDefinition.params.collect { "${it.key}=${it.value}" }.join('&')
        String queryString = queryParams ? "?${queryParams}" : ''
        return "Link: ${path}${queryString}"
    }

    String getDevUrl() {
        if (url) return url
        if (controller && action) return "/${controller}/${action}"
        return null
    }

    String getTarget() {
        return linkDefinition.target
    }

    void setTarget(String value) {
        linkDefinition.target = value
    }

    String getNamespace() {
        return linkDefinition.namespace
    }

    void setNamespace(String value) {
        linkDefinition.namespace = value
        setOnEvent()
    }

    String getController() {
        return linkDefinition.controller
    }

    void setController(String value) {
        linkDefinition.controller = value
        setOnEvent()
    }

    String getAction() {
        return linkDefinition.action
    }

    void setAction(String value) {
        linkDefinition.action = value
        setOnEvent()
    }

    Map getParams() {
        return linkDefinition.params
    }

    void setParams(Map value) {
        linkDefinition.params = value
        setOnEvent()
    }

    String getFragment() {
        return linkDefinition.fragment
    }

    void setFragment(String value) {
        linkDefinition.fragment = value
        setOnEvent()
    }

    String getPath() {
        return linkDefinition.path
    }

    void setPath(String value) {
        linkDefinition.path = value
        setOnEvent()
    }

    String getUrl() {
        return linkDefinition.url
    }

    void setUrl(String value) {
        linkDefinition.url = value
        setOnEvent()
    }

    List<String> getSubmit() {
        return linkDefinition.submit
    }

    void setSubmit(String value) {
        setSubmit([value])
    }

    void setSubmit(List<String> value) {
        linkDefinition.submit = value
        setOnEvent()
    }

    Boolean getDirect() {
        return linkDefinition.direct
    }

    void setDirect(Boolean value) {
        linkDefinition.direct = value
    }

    Boolean getModal() {
        return linkDefinition.renderProperties.modal
    }

    void setModal(Boolean value) {
        linkDefinition.renderProperties.modal = value
        setOnEvent()
    }

    Boolean getSmall() {
        return linkDefinition.renderProperties.small
    }

    void setSmall(Boolean value) {
        linkDefinition.renderProperties.small = value
        setOnEvent()
    }

    Boolean getLarge() {
        return linkDefinition.renderProperties.large
    }

    void setLarge(Boolean value) {
        linkDefinition.renderProperties.large = value
        setOnEvent()
    }

    String getAnimate() {
        return linkDefinition.renderProperties.animate
    }

    void setAnimate(String value) {
        linkDefinition.renderProperties.animate = value
        setOnEvent()
    }

    Boolean getCloseButton() {
        return linkDefinition.renderProperties.closeButton
    }

    void setCloseButton(Boolean value) {
        linkDefinition.renderProperties.closeButton = value
        setOnEvent()
    }

    String getScroll() {
        return linkDefinition.renderProperties.scroll
    }

    void setScroll(String value) {
        linkDefinition.renderProperties.scroll = value
        setOnEvent()
    }

    Boolean getTargetNew() {
        return linkDefinition.targetNew
    }

    void setTargetNew(Boolean value) {
        linkDefinition.targetNew = value
        setOnEvent()
    }

    Boolean getLoading() {
        return linkDefinition.loading
    }

    void setLoading(Boolean value) {
        linkDefinition.loading = value
        setOnEvent()
    }

    String getInfoMessage() {
        return linkDefinition.infoMessage
    }

    void setInfoMessage(String value) {
        linkDefinition.infoMessage = value
        setOnEvent()
    }

    void setInfoMessageArgs(List value) {
        linkDefinition.infoMessageArgs = value
        setOnEvent()
    }

    String getConfirmMessage() {
        return linkDefinition.confirmMessage
    }

    void setConfirmMessage(String value) {
        linkDefinition.confirmMessage = value
        setOnEvent()
    }

    void setConfirmMessageArgs(List value) {
        linkDefinition.confirmMessageArgs = value
        setOnEvent()
    }

    void setConfirmMessageOnConfirm(ComponentEvent value) {
        linkDefinition.confirmMessageOnConfirm = value.asMap()
        setOnEvent()
    }
}
