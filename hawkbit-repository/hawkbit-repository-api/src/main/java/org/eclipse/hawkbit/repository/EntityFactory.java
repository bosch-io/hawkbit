/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository;

import org.eclipse.hawkbit.repository.builder.ActionStatusBuilder;
import org.eclipse.hawkbit.repository.builder.RolloutBuilder;
import org.eclipse.hawkbit.repository.builder.RolloutGroupBuilder;
import org.eclipse.hawkbit.repository.builder.TargetBuilder;
import org.eclipse.hawkbit.repository.builder.TargetFilterQueryBuilder;
import org.eclipse.hawkbit.repository.model.BaseEntity;

/**
 * Central {@link BaseEntity} generation service. Objects are created but not persisted.
 */
public interface EntityFactory {

    /**
     * @return {@link ActionStatusBuilder} object
     */
    ActionStatusBuilder actionStatus();

    /**
     * @return {@link RolloutGroupBuilder} object
     */
    RolloutGroupBuilder rolloutGroup();

    /**
     * @return {@link RolloutBuilder} object
     */
    RolloutBuilder rollout();

    /**
     * @return {@link TargetBuilder} object
     */
    TargetBuilder target();

    /**
     * @return {@link TargetFilterQueryBuilder} object
     */
    TargetFilterQueryBuilder targetFilterQuery();
}