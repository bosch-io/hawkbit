/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.event.remote.entity;

import org.eclipse.hawkbit.repository.model.Target;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * Defines the remote event of deleting a {@link Target}.
 */
public class TargetDeletedEvent extends BaseEntityIdEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for json serialization.
     * 
     * @param tenant
     *            the tenant
     * @param entityId
     *            the entity id
     * @param applicationId
     *            the origin application id
     */
    @JsonCreator
    public TargetDeletedEvent(@JsonProperty("tenant") final String tenant,
            @JsonProperty("entityId") final Long entityId, @JsonProperty("originService") final String applicationId) {
        super(entityId, tenant, applicationId);
    }

}
