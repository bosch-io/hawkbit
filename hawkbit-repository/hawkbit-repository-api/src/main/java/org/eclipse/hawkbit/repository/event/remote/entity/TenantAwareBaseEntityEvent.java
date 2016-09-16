/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.event.remote.entity;

import org.eclipse.hawkbit.repository.event.EntityEvent;
import org.eclipse.hawkbit.repository.model.TenantAwareBaseEntity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An base definition class for {@link EntityEvent} for some object.
 *
 * @param <E>
 *            the type of the entity
 */
public class TenantAwareBaseEntityEvent<E extends TenantAwareBaseEntity> extends BaseEntityIdEvent
        implements EntityEvent {

    private static final long serialVersionUID = 1L;

    private String entityClassName;

    @JsonIgnore
    private transient E entity;

    /**
     * Constructor for json serialization.
     * 
     * @param tenant
     *            the tenant
     * @param entityId
     *            the entity id
     * @param entityClassName
     *            the entity entityClassName
     * @param applicationId
     *            the origin application id
     */
    @JsonCreator
    protected TenantAwareBaseEntityEvent(@JsonProperty("tenant") final String tenant,
            @JsonProperty("entityId") final Long entityId,
            @JsonProperty("entityClassName") final String entityClassName,
            @JsonProperty("originService") final String applicationId) {
        super(entityId, tenant, applicationId);
        this.entityClassName = entityClassName;
    }

    /**
     * Constructor.
     * 
     * @param baseEntity
     *            the base entity
     * @param applicationId
     *            the origin application id
     */
    protected TenantAwareBaseEntityEvent(final E baseEntity, final String applicationId) {
        this(baseEntity.getTenant(), baseEntity.getId(), baseEntity.getClass().getName(), applicationId);
        this.entity = baseEntity;
    }

    @Override
    public E getEntity() {
        if (entity == null) {
            // TODO: Events überprüfen vielleicht eins ohne entität dabei wird
            // falsch aufgerufen
            System.out.println(this);
            System.out.println(getEntityId());
            System.out.println(entityClassName);
            System.out.println(getTenant());
            // Idee entity manager zum laden verwenden entitySource.getId +
            // entitySource.getTenant
        }
        return entity;
    }

    /**
     * @return the entityClassName
     */
    public String getEntityClassName() {
        return entityClassName;
    }

    @Override
    @JsonIgnore
    public <T> T getEntity(final Class<T> entityClass) {
        return entityClass.cast(entity);
    }

}
