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
import org.eclipse.hawkbit.repository.event.remote.TargetAttributesRequestedEvent;

/**
 * Service event for {@link TargetAttributesRequestedEvent}. Event that needs single replica processing
 */
public class TargetAttributesRequestedServiceEvent extends AbstractServiceRemoteEvent<TargetAttributesRequestedEvent> {

    @Serial
    private static final long serialVersionUID = 1L;


    @JsonCreator
    public TargetAttributesRequestedServiceEvent(@JsonProperty("payload") final TargetAttributesRequestedEvent remoteEvent) {
        super(remoteEvent);
    }
}