/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.springframework.cloud.bus.event.entity;

import java.util.Map;

import org.eclipse.hawkbit.repository.model.TenantAwareBaseEntity;
import org.springframework.cloud.bus.event.entity.GenericEventEntity.PropertyChange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Property change event.
 *
 * @param <E>
 */
public class BasePropertyChangeEvent<E extends TenantAwareBaseEntity> extends TenantAwareBaseEntityEvent<E> {

    private static final long serialVersionUID = -3671601415138242311L;

    /**
     * Constructor for json serialization
     * 
     * @param entitySource
     *            the json infos
     */
    @JsonCreator
    protected BasePropertyChangeEvent(@JsonProperty("entitySource") final GenericEventEntity<Long> entitySource) {
        super(entitySource);
    }

    protected BasePropertyChangeEvent(final E entity, final Map<String, PropertyChange> changeSetValues,
            final String originService) {
        super(entity, originService);
        getEntitySource().setChangeSetValues(changeSetValues);
    }

}
