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

import dueuno.elements.PageRenderProperties
import groovy.transform.CompileStatic

/**
 * Base class that holds all the properties needed to define a navigable link or
 * server-side action invocation within the Elements framework.
 * <p>
 * A {@code LinkDefinition} can describe a target in one of two ways:
 * </p>
 * <ul>
 *     <li>As a Grails controller/action pair (optionally namespaced), with request parameters.</li>
 *     <li>As an explicit URL, which is always treated as a direct link by default.</li>
 * </ul>
 * <p>
 * Additional properties control how the response is rendered ({@code direct},
 * {@code target}, {@code renderProperties}), which form data is submitted
 * ({@code submit}), and optional UI feedback shown before or instead of
 * executing the link ({@code loading}, {@code infoMessage}, {@code confirmMessage}).
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class LinkDefinition implements Serializable {

    /** The namespace of the controller to use in the link. */
    String namespace

    /** The name of the controller to link to; if not specified the current controller is used. */
    String controller

    /** The name of the action to link to; if not specified the default action is used. */
    String action

    /** A map of request parameters to include in the link. */
    Map params

    /** The URL fragment (anchor) to append to the link (e.g. {@code "#section"}). */
    String fragment

    /** An absolute path (starting with {@code '/'}) or relative path to use as the link target. */
    String path

    /** An explicit URL target. When set it takes precedence over the controller/action pair and {@code direct} defaults to {@code true}. */
    String url

    /** When {@code true}, the link response is rendered as a full HTML page or raw HTTP body instead of a {@link dueuno.elements.Transition}. */
    Boolean direct

    /** Names of the components whose data should be submitted with the request. If empty the closest container component is submitted. */
    List<String> submit

    /** The browsing context (e.g. {@code "_blank"}, {@code "_self"}) in which to open the link. */
    String target

    /** Page render properties that control how the linked page is displayed. */
    PageRenderProperties renderProperties

    /** When {@code true}, a loading indicator is shown while the request is in flight. */
    Boolean loading

    /** When set, an informational pop-up with this message is displayed and the link is never executed. */
    String infoMessage

    /** Optional interpolation arguments for {@link #infoMessage}. */
    List infoMessageArgs

    /** When set, a confirmation pop-up with this message is displayed, giving the user a chance to cancel before the link is executed. */
    String confirmMessage

    /** Optional interpolation arguments for {@link #confirmMessage}. */
    List confirmMessageArgs

    /** Optional map defining the action to invoke when the user confirms the {@link #confirmMessage} dialog. */
    Map confirmMessageOnConfirm

    /**
     * Creates a {@code LinkDefinition} from a map of arguments.
     * Supported keys mirror the field names of this class. If {@code url} is provided
     * and {@code direct} is not explicitly set, {@code direct} defaults to {@code true}.
     * {@code renderProperties} can be passed as a {@link PageRenderProperties} instance
     * or as individual keys that will be forwarded to the {@link PageRenderProperties} constructor.
     *
     * @param args map of link properties
     */
    LinkDefinition(Map args = [:]) {
        namespace = args.namespace ?: ''
        controller = args.controller ?: ''
        action = args.action ?: ''
        fragment = args.fragment ?: ''
        path = args.path ?: ''
        url = args.url ?: ''
        if (url) {
            // URLs are handled as direct links by default
            direct = args.direct == null ? true : args.direct as Boolean
        } else {
            direct = args.direct
        }

        params = args.params as Map ?: [:]

        args.submit in List
                ? setSubmit(args.submit as List<String>)
                : setSubmit(args.submit as String)

        target = args.target
        targetNew = args.targetNew
        loading = args.loading

        infoMessage = args.infoMessage
        infoMessageArgs = args.infoMessageArgs as List
        confirmMessage = args.confirmMessage
        confirmMessageArgs = args.confirmMessageArgs as List
        confirmMessageOnConfirm = args.confirmMessageOnClick as Map

        if (args.renderProperties)
            renderProperties = (PageRenderProperties) args.renderProperties
        else
            renderProperties = new PageRenderProperties(args)
    }

    /**
     * Returns {@code true} when the link is configured to open in a new browser tab
     * (i.e. {@link #target} equals {@code "_blank"}).
     *
     * @return {@code true} if the link opens in a new tab
     */
    Boolean getTargetNew() {
        return target == '_blank'
    }

    /**
     * Convenience setter that configures the link to open in a new browser tab.
     * When {@code value} is {@code true}, sets {@link #direct} to {@code true}
     * and {@link #target} to {@code "_blank"}.
     *
     * @param value {@code true} to open the link in a new tab
     */
    void setTargetNew(Boolean value) {
        if (value) {
            direct = true
            target = '_blank'
        }
    }

    /**
     * Sets the browsing context for the link. When {@code value} is {@code "_self"},
     * {@link #direct} is also set to {@code true} so that the response replaces the
     * current page rather than being processed as a {@link dueuno.elements.Transition}.
     *
     * @param value the target browsing context (e.g. {@code "_self"})
     */
    void setTarget(String value) {
        if (value && value == '_self') {
            direct = true
            target = value
        }
    }

    /**
     * Sets {@link #submit} to a single-element list containing the given component name.
     * Does nothing if {@code value} is blank.
     *
     * @param value the name of the component to submit
     */
    void setSubmit(String value) {
        if (!value) {
            return
        }

        submit = [value]
    }

    /**
     * Sets {@link #submit} to the provided list of component names.
     * Does nothing if {@code value} is empty or {@code null}.
     *
     * @param value the list of component names to submit
     */
    void setSubmit(List<String> value) {
        if (!value) {
            return
        }

        submit = value
    }

    /**
     * Serialises the link definition to a map suitable for JSON encoding.
     * Only the properties relevant to the frontend are included; interpolation
     * argument lists and path/fragment fields are omitted.
     *
     * @return a map representation of this link definition
     */
    Map asMap() {
        return [
                namespace: namespace,
                controller: controller,
                action: action,
                url: url,
                params: params,
                submit: submit,
                direct: direct,
                target: target,
                loading: loading,
                infoMessage: infoMessage,
                confirmMessage: confirmMessage,
                renderProperties: renderProperties.asMap(),
        ]
    }
}
