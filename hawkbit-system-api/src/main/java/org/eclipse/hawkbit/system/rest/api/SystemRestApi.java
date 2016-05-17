/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.system.rest.api;

import java.util.Map;

import org.eclipse.hawkbit.system.json.model.system.SystemTenantConfigurationValue;
import org.eclipse.hawkbit.system.json.model.system.SystemTenantConfigurationValueRequest;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * REST Resource handling tenant specific configuration operations.
 *
 *
 */
@RequestMapping(SystemRestConstant.SYSTEM_V1_REQUEST_MAPPING)
public interface SystemRestApi {

    @RequestMapping(method = RequestMethod.GET, produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity<ResourceSupport> getSystem();

    /**
     * @return a Map of all configuration values.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/configs", produces = { "application/hal+json",
            MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity<Map<String, SystemTenantConfigurationValue>> getSystemConfiguration();

    /**
     * Handles the DELETE request of deleting a tenant specific configuration
     * value within SP.
     *
     * @param keyName
     *            the Name of the configuration key
     * @return If the given configuration value exists and could be deleted Http
     *         OK. In any failure the JsonResponseExceptionHandler is handling
     *         the response.
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/configs/{keyName}", produces = { "application/hal+json",
            MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity<Void> deleteConfigurationValue(@PathVariable("keyName") final String keyName);

    /**
     * Handles the GET request of deleting a tenant specific configuration value
     * within SP.
     *
     * @param keyName
     *            the Name of the configuration key
     * @return If the given configuration value exists and could be get Http OK.
     *         In any failure the JsonResponseExceptionHandler is handling the
     *         response.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/configs/{keyName}", produces = { "application/hal+json",
            MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity<SystemTenantConfigurationValue> getConfigurationValue(@PathVariable("keyName") final String keyName);

    /**
     * Handles the GET request of deleting a tenant specific configuration value
     * within SP.
     *
     * @param keyName
     *            the Name of the configuration key
     * @param configurationValueRest
     *            the new value for the configuration
     * @return If the given configuration value exists and could be get Http OK.
     *         In any failure the JsonResponseExceptionHandler is handling the
     *         response.
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/configs/{keyName}", consumes = { "application/hal+json",
            MediaType.APPLICATION_JSON_VALUE }, produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
    ResponseEntity<SystemTenantConfigurationValue> updateConfigurationValue(
            @PathVariable("keyName") final String keyName,
            @RequestBody final SystemTenantConfigurationValueRequest configurationValueRest);

}