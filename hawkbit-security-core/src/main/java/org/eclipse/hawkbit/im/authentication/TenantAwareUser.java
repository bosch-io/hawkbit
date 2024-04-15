/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.im.authentication;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;

/**
 * A software provisioning user principal definition stored in the
 * {@link SecurityContext} which contains the user specific attributes.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantAwareUser extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String firstname;
    private final String lastname;
    private final String loginname;
    private final String tenant;
    private final String email;

    /**
     * @param username the username of the user
     * @param password the password of the user
     * @param authorities the authorities which the user has
     * @param tenant the tenant of the user
     */
    public TenantAwareUser(final String username, final String password,
            final Collection<? extends GrantedAuthority> authorities, final String tenant) {
        this(username, password, username, null, username, null, tenant, authorities);
    }

    /**
     * Create user without password and any credentials. For test purposes only.
     *
     * @param username the username of the user
     * @param tenant the tenant of the user
     */
    public TenantAwareUser(final String username, String tenant) {
        this(username, "***", null, tenant);
    }

    @SuppressWarnings("squid:S00107")
    public TenantAwareUser(final String username, final String password, final String firstname, final String lastname,
            final String loginname, final String email, final String tenant,
            final Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities == null ? Collections.emptyList() : authorities);
        this.firstname = firstname;
        this.lastname = lastname;
        this.loginname = loginname;
        this.tenant = tenant;
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getLoginname() {
        return loginname;
    }

    public String getTenant() {
        return tenant;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}