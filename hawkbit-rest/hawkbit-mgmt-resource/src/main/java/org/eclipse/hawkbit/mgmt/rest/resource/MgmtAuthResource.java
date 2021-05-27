/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.mgmt.rest.resource;

import org.eclipse.hawkbit.mgmt.json.model.auth.MgmtUserInfo;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtAuthRestApi;
import org.eclipse.hawkbit.security.SecurityContextTenantAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Resource handling basic auth validation.
 */
@RestController
public class MgmtAuthResource implements MgmtAuthRestApi {

    private final SecurityContextTenantAware securityContextTenantAware;

    /**
     * Default constructor
     *
     * @param securityContextTenantAware
     *          securityContextTenantAware
     */
    public MgmtAuthResource(SecurityContextTenantAware securityContextTenantAware) {
        this.securityContextTenantAware = securityContextTenantAware;
    }

    @Override
    public ResponseEntity<MgmtUserInfo> validateBasicAuth() {
        MgmtUserInfo userInfo = new MgmtUserInfo();
        userInfo.setUsername(securityContextTenantAware.getCurrentUsername());
        userInfo.setTenant(securityContextTenantAware.getCurrentTenant());
        return ResponseEntity.ok(userInfo);
    }
}