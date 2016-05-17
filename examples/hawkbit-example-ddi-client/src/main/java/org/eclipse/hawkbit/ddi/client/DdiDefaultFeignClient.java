/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ddi.client;

import org.apache.commons.lang.Validate;
import org.eclipse.hawkbit.ddi.client.authenctication.AuthenticationInterceptor;
import org.eclipse.hawkbit.ddi.client.resource.RootControllerResourceClient;
import org.eclipse.hawkbit.ddi.client.resource.RootControllerResourceClientConstants;
import org.eclipse.hawkbit.feign.core.client.ApplicationJsonRequestHeaderInterceptor;
import org.eclipse.hawkbit.feign.core.client.IgnoreMultipleConsumersProducersSpringMvcContract;

import feign.Feign;
import feign.Feign.Builder;
import feign.Logger;
import feign.Logger.Level;
import feign.jackson.JacksonEncoder;

/**
 * Default implementation of DDI client.
 */
public class DdiDefaultFeignClient {

    private RootControllerResourceClient rootControllerResourceClient;
    private final Builder feignBuilder;
    private final String baseUrl;
    private final String tenant;

    /**
     * Constructor for default DDI feign client with no authentication.
     * 
     * @param baseUrl
     *            the base url of the client
     * @param tenant
     *            the tenant
     */
    public DdiDefaultFeignClient(final String baseUrl, final String tenant) {
        this(baseUrl, tenant, null);
    }

    /**
     * Constructor for default DDI feign client with authentication.
     * 
     * @param baseUrl
     *            the base url of the client
     * @param tenant
     *            the tenant
     * @param authenticationInterceptor
     */
    public DdiDefaultFeignClient(final String baseUrl, final String tenant,
            final AuthenticationInterceptor authenticationInterceptor) {
        feignBuilder = Feign.builder().contract(new IgnoreMultipleConsumersProducersSpringMvcContract())
                .requestInterceptor(new ApplicationJsonRequestHeaderInterceptor()).logLevel(Level.FULL)
                .logger(new Logger.ErrorLogger()).encoder(new JacksonEncoder()).decoder(new DdiDecoder());
        if (authenticationInterceptor != null) {
            feignBuilder.requestInterceptor(authenticationInterceptor);
        }
        Validate.notNull(baseUrl, "A baseUrl has to be set");
        Validate.notNull(tenant, "A tenant has to be set");
        this.baseUrl = baseUrl;
        this.tenant = tenant;
    }

    /**
     * Get the feign builder.
     * 
     * @return the feign builder
     */
    public Builder getFeignBuilder() {
        return feignBuilder;
    }

    /**
     * Get the rootController resource client.
     * 
     * @return the rootController resource client
     */
    public RootControllerResourceClient getRootControllerResourceClient() {

        if (rootControllerResourceClient == null) {
            String rootControllerResourcePath = this.baseUrl + RootControllerResourceClientConstants.PATH;
            rootControllerResourcePath = rootControllerResourcePath.replace("{tenant}", tenant);
            rootControllerResourceClient = feignBuilder.target(RootControllerResourceClient.class,
                    rootControllerResourcePath);
        }
        return rootControllerResourceClient;
    }

}
