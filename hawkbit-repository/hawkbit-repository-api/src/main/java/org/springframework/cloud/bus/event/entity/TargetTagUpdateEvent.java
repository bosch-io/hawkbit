/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.springframework.cloud.bus.event.entity;

import org.eclipse.hawkbit.repository.model.TargetTag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the {@link AbstractBaseEntityEvent} for update a {@link TargetTag}.
 *
 */
public class TargetTagUpdateEvent extends TenantAwareBaseEntityEvent<TargetTag> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for json serialization
     * 
     * @param entitySource
     *            the json infos
     */
    @JsonCreator
    protected TargetTagUpdateEvent(@JsonProperty("entitySource") final GenericEventEntity<Long> entitySource) {
        super(entitySource);
    }

    /**
     * Constructor.
     * 
     * @param tag
     *            the tag which is updated
     */
    public TargetTagUpdateEvent(final TargetTag tag, final String originService) {
        super(tag, originService);
    }
}
