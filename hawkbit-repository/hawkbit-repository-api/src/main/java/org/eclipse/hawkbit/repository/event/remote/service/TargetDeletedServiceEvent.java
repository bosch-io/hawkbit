/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.event.remote.service;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.hawkbit.repository.event.remote.TargetDeletedEvent;

/**
 * Service event for {@link TargetDeletedEvent}. Event that needs single replica processing
 */
public class TargetDeletedServiceEvent extends AbstractServiceRemoteEvent<TargetDeletedEvent> {

    @Serial
    private static final long serialVersionUID = 1L;


    @JsonCreator
    public TargetDeletedServiceEvent(@JsonProperty("payload") final TargetDeletedEvent remoteEvent) {
        super(remoteEvent);
    }
}