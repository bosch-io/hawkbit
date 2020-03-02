/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.proxies;

import java.net.URI;

import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;

/**
 * Proxy for {@link Target}.
 */
public class ProxyTarget extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;

    private String controllerId;

    private URI address;

    private Long lastTargetQuery;

    private Long installationDate;

    private TargetUpdateStatus updateStatus = TargetUpdateStatus.UNKNOWN;

    private String pollStatusToolTip;

    private Status status;

    private String securityToken;

    private boolean isRequestAttributes;

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(final String controllerId) {
        this.controllerId = controllerId;
    }

    public URI getAddress() {
        return address;
    }

    public void setAddress(final URI address) {
        this.address = address;
    }

    public Long getLastTargetQuery() {
        return lastTargetQuery;
    }

    public void setLastTargetQuery(final Long lastTargetQuery) {
        this.lastTargetQuery = lastTargetQuery;
    }

    public Long getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(final Long installationDate) {
        this.installationDate = installationDate;
    }

    public TargetUpdateStatus getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(final TargetUpdateStatus updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getPollStatusToolTip() {
        return pollStatusToolTip;
    }

    public void setPollStatusToolTip(final String pollStatusToolTip) {
        this.pollStatusToolTip = pollStatusToolTip;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(final String securityToken) {
        this.securityToken = securityToken;
    }

    public boolean isRequestAttributes() {
        return isRequestAttributes;
    }

    public void setRequestAttributes(final boolean isRequestAttributes) {
        this.isRequestAttributes = isRequestAttributes;
    }
}