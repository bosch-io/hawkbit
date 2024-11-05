/**
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.event.remote.entity;

import org.eclipse.hawkbit.repository.event.entity.EntityUpdatedEvent;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;

/**
 * Defines the remote event for updating a {@link SoftwareModuleType}.
 */
public class DistributionSetTypeUpdatedEvent extends RemoteEntityEvent<DistributionSetType>
        implements EntityUpdatedEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public DistributionSetTypeUpdatedEvent() {
        // for serialization libs like jackson
    }

    /**
     * Constructor.
     *
     * @param baseEntity DistributionSetType
     * @param applicationId the origin application id
     */
    public DistributionSetTypeUpdatedEvent(final DistributionSetType baseEntity, final String applicationId) {
        super(baseEntity, applicationId);
    }

}
