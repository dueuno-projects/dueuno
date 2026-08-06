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


import groovy.transform.CompileStatic

/**
 * An application is subdivided in Features, each one of them contains specific functionalities.
 * You can think of a Feature as an "applet" inside your application.
 *
 * A Feature can contains sub-features.
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class Feature extends LinkDefinition {

    private static Integer idCounter = 0
    private Integer id

    /** Text label & icon to display */
    String text
    String icon
    String tooltip
    String image

    /** Logical order from the first (0) to the last (MAX_INT) */
    Integer order

    /** Parent feature. If specified a sub-feature will be created. */
    Feature parent

    /** If true the feature will be displayed as a home element */
    Boolean favourite

    /**
     * Display the feature only to the users granted with the specified authorities
     * (eg: <code>['ROLE_CAN_EDIT_ORDERS', 'ROLE_CAN_EDIT_PRODUCTS']</code>).
     * A Feature is always visible for <code>ROLE_ADMIN</code>
     */
    // SEE: https://stackoverflow.com/questions/13755592/concurrent-modification-exception-while-iterating-over-arraylist
    List<String> authorities = [].asSynchronized() as List<String>

    private List<Feature> features = []

    private Integer generateId() {
        return idCounter++
    }

    Feature(Map args) {
        super(args)

        id = generateId()
        text = args.text
        icon = args.icon
        tooltip = args.tooltip
        image = args.image
        loading = args.loading

        parent = args.parent as Feature
        if (parent) {
            order = parent.order + id * 10
        } else {
            order = (Integer) args.order ?: id * 1000
        }

        favourite = args.favourite

        // ROLE_USER is the default role
        List<String> configuredAuthorities = (List<String>) args.authorities ?: []
        if (!configuredAuthorities) configuredAuthorities.add('ROLE_USER')
        authorities.addAll(configuredAuthorities)
    }

    /**
     * Returns the name of the Feature
     * @return The name of the Feature
     */
    String getId() {
        return buildId('.')
    }

    private String buildId(String separator) {
        String n = namespace ? namespace + separator : ''
        String a = action ? separator + action : ''
        String result = "${n}${controller}${a}"
        return result
    }

    /**
     * Adds a sub-feature
     *
     * @param args The feature properties
     *
     * @return The newly added feature
     */
    Feature add(Map args) {
        Feature feature = new Feature(args)
        add(feature)
        return feature
    }

    /**
     * Adds a sub-feature
     *
     * @param args The feature to add
     *
     * @return The newly added feature
     */
    Feature add(Feature feature) {
        features.add(feature)
        return feature
    }

    /**
     * Returns the list of sub-features
     * @return The list of sub-features
     */
    List<Feature> getFeatures() {
        return features.sort { it['order'] } as List<Feature>
    }

    /**
     * Returns the list of favourite sub-features
     * @return The list of favourite sub-features
     */
    List<Feature> getFavouriteFeatures() {
        return features
            .findAll { it['favourite'] == true }
            .sort { it['order'] } as List<Feature>
    }

    private List<Feature> listFeatureRecursive() {
        List<Feature> results = []
        for (feature in features) {
            if (feature.favourite) {
                results.add(feature)
                results.addAll(feature.listFeatureRecursive())
            }
        }
        return results
    }

    /**
     * Returns the feature registered for the specified controller
     *
     * @param controllerName A controller name
     *
     * @return The feature registered for the specified controller
     */
    Feature byController(String controllerName) {
        Feature result = (Feature) features.find { it['controller'] == controllerName }
        if (result)
            return result

        features.any { feature ->
            result = feature.byController(controllerName)
            if (result) return true
        }

        return result
    }
}
