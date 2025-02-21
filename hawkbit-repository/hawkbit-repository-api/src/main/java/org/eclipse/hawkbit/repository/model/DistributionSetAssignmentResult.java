/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.model;

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A bean which holds a complex result of a service operation to combine the information of an assignment and how much of the assignment
 * has been done and how much of the assignments had already been existed.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DistributionSetAssignmentResult extends AbstractAssignmentResult<Action> {

    private final DistributionSet distributionSet;

    /**
     * Constructor.
     *
     * @param distributionSet that has been assigned
     * @param alreadyAssigned the count of already assigned targets
     * @param assigned the assigned actions
     */
    public DistributionSetAssignmentResult(final DistributionSet distributionSet, final int alreadyAssigned,
            final List<? extends Action> assigned) {
        super(alreadyAssigned, assigned, Collections.emptyList());
        this.distributionSet = distributionSet;
    }
}