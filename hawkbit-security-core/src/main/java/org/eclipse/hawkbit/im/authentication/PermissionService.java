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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

/**
 * Service to check permissions.
 *
 */
public class PermissionService {

    private final RoleHierarchy roleHierarchy;

    public PermissionService(final RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    /**
     * Checks if the given {@code permission} contains in the. In case no
     * {@code context} is available {@code false} will be returned.
     *
     * @param permission
     *            the permission to check against the
     * @return {@code true} if a is available and contains the given
     *         {@code permission}, otherwise {@code false}.
     * @see SpPermission
     */
    public boolean hasPermission(final String permission) {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication authentication = context.getAuthentication();
            if (authentication != null) {
                Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
                if (!ObjectUtils.isEmpty(grantedAuthorities)) {
                    if (roleHierarchy != null) {
                        grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(grantedAuthorities);
                    }
                    for (final GrantedAuthority authority : grantedAuthorities) {
                        if (authority.getAuthority().equals(permission)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if at least on permission of the given {@code permissions}
     * contains in the . In case no {@code context} is available {@code false}
     * will be returned.
     *
     * @param permissions
     *            the permissions to check against the
     * @return {@code true} if a is available and contains the given
     *         {@code permission}, otherwise {@code false}.
     * @see SpPermission
     */
    public boolean hasAtLeastOnePermission(final List<String> permissions) {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication authentication = context.getAuthentication();
            if (authentication != null) {
                Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
                if (!ObjectUtils.isEmpty(grantedAuthorities)) {
                    if (roleHierarchy != null) {
                        grantedAuthorities = roleHierarchy.getReachableGrantedAuthorities(grantedAuthorities);
                    }
                    for (final GrantedAuthority authority : grantedAuthorities) {
                        for (final String permission : permissions) {
                            if (authority.getAuthority().equals(permission)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}
