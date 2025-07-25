/**
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.autoconfigure.ddi;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.hawkbit.ddi.rest.api.DdiRestConstants;
import org.eclipse.hawkbit.repository.ControllerManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.rest.SecurityManagedConfiguration;
import org.eclipse.hawkbit.rest.security.DosFilter;
import org.eclipse.hawkbit.security.DdiSecurityProperties;
import org.eclipse.hawkbit.security.HawkbitSecurityProperties;
import org.eclipse.hawkbit.security.MdcHandler;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.security.controller.AuthenticationFilters;
import org.eclipse.hawkbit.security.controller.GatewayTokenAuthenticator;
import org.eclipse.hawkbit.security.controller.SecurityHeaderAuthenticator;
import org.eclipse.hawkbit.security.controller.SecurityTokenAuthenticator;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Security configuration for the hawkBit server DDI interface.
 */
@Slf4j
@Configuration
@EnableWebSecurity
class ControllerSecurityConfiguration {

    private static final String[] DDI_ANT_MATCHERS = { DdiRestConstants.BASE_V1_REQUEST_MAPPING + "/**" };

    private final ControllerManagement controllerManagement;
    private final TenantConfigurationManagement tenantConfigurationManagement;
    private final TenantAware tenantAware;
    private final DdiSecurityProperties ddiSecurityConfiguration;
    private final HawkbitSecurityProperties securityProperties;
    private final SystemSecurityContext systemSecurityContext;

    @Autowired
    ControllerSecurityConfiguration(final ControllerManagement controllerManagement,
            final TenantConfigurationManagement tenantConfigurationManagement, final TenantAware tenantAware,
            final DdiSecurityProperties ddiSecurityConfiguration,
            final HawkbitSecurityProperties securityProperties, final SystemSecurityContext systemSecurityContext) {
        this.controllerManagement = controllerManagement;
        this.tenantConfigurationManagement = tenantConfigurationManagement;
        this.tenantAware = tenantAware;
        this.ddiSecurityConfiguration = ddiSecurityConfiguration;
        this.securityProperties = securityProperties;
        this.systemSecurityContext = systemSecurityContext;
    }

    /**
     * Filter to protect the hawkBit server DDI interface against too many requests.
     *
     * @param securityProperties for filter configuration
     * @return the spring filter registration bean for registering a denial of service protection filter in the filter chain
     */
    @Bean
    @ConditionalOnProperty(prefix = "hawkbit.server.security.dos.filter", name = "enabled", matchIfMissing = true)
    protected FilterRegistrationBean<DosFilter> dosFilterDDI(final HawkbitSecurityProperties securityProperties) {
        final FilterRegistrationBean<DosFilter> filterRegBean =
                SecurityManagedConfiguration.dosFilter(List.of(DDI_ANT_MATCHERS),
                        securityProperties.getDos().getFilter(), securityProperties.getClients());
        filterRegBean.setOrder(SecurityManagedConfiguration.DOS_FILTER_ORDER);
        filterRegBean.setName("dosDDiFilter");

        return filterRegBean;
    }


    @Bean
    @Order(301)
    protected SecurityFilterChain filterChainDDI(
            final HttpSecurity http,
            @Value("${hawkbit.server.security.cors.disable-for-ddi-api:false}") final boolean disableCorsForDdiApi) throws Exception {
        http
                .securityMatcher(DDI_ANT_MATCHERS)
                .authorizeHttpRequests(amrmRegistry -> amrmRegistry.anyRequest().authenticated())
                .anonymous(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new AuthenticationFilters.SecurityHeaderAuthenticationFilter(
                        new SecurityHeaderAuthenticator(
                                tenantConfigurationManagement, tenantAware,
                                systemSecurityContext, ddiSecurityConfiguration.getRp().getCnHeader(), ddiSecurityConfiguration.getRp().getSslIssuerHashHeader()
                        ), ddiSecurityConfiguration), AuthorizationFilter.class)
                .addFilterBefore(new AuthenticationFilters.SecurityTokenAuthenticationFilter(
                        new SecurityTokenAuthenticator(
                                tenantConfigurationManagement, tenantAware,
                                systemSecurityContext, controllerManagement), ddiSecurityConfiguration), AuthorizationFilter.class)
                .addFilterBefore(new AuthenticationFilters.GatewayTokenAuthenticationFilter(
                        new GatewayTokenAuthenticator(
                                tenantConfigurationManagement, tenantAware,
                                systemSecurityContext), ddiSecurityConfiguration), AuthorizationFilter.class)
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(
                        (request, response, authException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        if (securityProperties.getCors().isEnabled() && !disableCorsForDdiApi) {
            http.cors(configurer -> configurer.configurationSource(securityProperties.getCors().toCorsConfigurationSource()));
        }

        if (securityProperties.isRequireSsl()) {
            http.requiresChannel(crmRegistry -> crmRegistry.anyRequest().requiresSecure());
        }

        MdcHandler.Filter.addMdcFilter(http);

        return http.build();
    }
}