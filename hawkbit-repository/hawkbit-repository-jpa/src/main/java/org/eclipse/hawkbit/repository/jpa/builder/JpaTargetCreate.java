/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.jpa.builder;

import org.eclipse.hawkbit.repository.TargetTypeManagement;
import org.eclipse.hawkbit.repository.builder.AbstractTargetUpdateCreate;
import org.eclipse.hawkbit.repository.builder.TargetCreate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.jpa.model.JpaTarget;
import org.eclipse.hawkbit.repository.model.TargetType;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.springframework.util.ObjectUtils;

/**
 * Create/build implementation.
 */
public class JpaTargetCreate extends AbstractTargetUpdateCreate<TargetCreate> implements TargetCreate {

    private final TargetTypeManagement<? extends TargetType> targetTypeManagement;

    JpaTargetCreate(final TargetTypeManagement<? extends TargetType> targetTypeManagement) {
        super(null);
        this.targetTypeManagement = targetTypeManagement;
    }

    @Override
    public JpaTarget build() {
        final JpaTarget target = new JpaTarget(controllerId, securityToken);

        if (!ObjectUtils.isEmpty(name)) {
            target.setName(name);
        }

        if (targetTypeId != null) {
            final TargetType targetType = targetTypeManagement.get(targetTypeId)
                    .orElseThrow(() -> new EntityNotFoundException(TargetType.class, targetTypeId));
            target.setTargetType(targetType);
        }

        target.setDescription(description);
        target.setAddress(address);
        target.setUpdateStatus(getStatus().orElse(TargetUpdateStatus.UNKNOWN));
        getLastTargetQuery().ifPresent(target::setLastTargetQuery);
        target.setGroup(getGroup().orElse(null));

        return target;
    }
}