/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.autoconfigure.security;

import org.eclipse.hawkbit.im.authentication.MultitenancyIndicator;
import org.eclipse.hawkbit.im.authentication.StaticAuthenticationProvider;
import org.eclipse.hawkbit.im.authentication.TenantAwareUserProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Autoconfiguration for the in-memory-user-management.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({ TenantAwareUserProperties.class })
public class InMemoryUserManagementAutoConfiguration extends GlobalAuthenticationConfigurerAdapter {

    private final StaticAuthenticationProvider authenticationProvider;

    InMemoryUserManagementAutoConfiguration(final SecurityProperties securityProperties,
            final TenantAwareUserProperties tenantAwareUserProperties,
            final Optional<PasswordEncoder> passwordEncoder) {
        authenticationProvider = new StaticAuthenticationProvider(tenantAwareUserProperties, securityProperties,
                passwordEncoder.orElse(null));
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    /**
     * @return the multi-tenancy indicator to disallow multi-tenancy
     */
    @Bean
    @ConditionalOnMissingBean
    MultitenancyIndicator multiTenancyIndicator() {
        return () -> false;
    }
}