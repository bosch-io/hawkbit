/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ddi.rest.resource;

import org.eclipse.hawkbit.ddi.json.model.DdiActivateAutoConfirmation;
import org.eclipse.hawkbit.ddi.json.model.DdiAutoConfirmationState;
import org.eclipse.hawkbit.ddi.rest.api.DdiRestConstants;
import org.eclipse.hawkbit.ddi.rest.api.DdiRootControllerRestApiV2;
import org.eclipse.hawkbit.repository.ConfirmationManagement;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

/**
 * The {@link DdiRootControllerV2} of the hawkBit server DDI API that is queried
 * by the hawkBit controller. In the current state the v2 is introducing a new
 * confirmation API, to control the auto-confirmation state.
 */
@RestController
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class DdiRootControllerV2 implements DdiRootControllerRestApiV2 {

    private static final Logger LOG = LoggerFactory.getLogger(DdiRootControllerV2.class);
    private static final String FALLBACK_REMARK = "Initiated using the Device Direct Integration API without providing a remark.";

    @Autowired
    private TenantAware tenantAware;

    @Autowired
    private ConfirmationManagement confirmationManagement;

    @Override
    public ResponseEntity<DdiAutoConfirmationState> getAutoConfirmationState(final String tenant,
            final String controllerId) {
        final DdiAutoConfirmationState result = confirmationManagement.getStatus(controllerId).map(status -> {
            final DdiAutoConfirmationState state = DdiAutoConfirmationState.active(status.getActivatedAt());
            state.add(WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(DdiRootControllerV2.class, tenantAware.getCurrentTenant())
                            .disableAutoConfirmation(tenantAware.getCurrentTenant(), controllerId))
                    .withRel(DdiRestConstants.AUTO_CONFIRMATION_DISABLE));
            LOG.trace("Returning state auto-conf state active [initiator='{}' | activatedAt={}] for device {}",
                    controllerId, status.getInitiator(), status.getActivatedAt());
            return state;
        }).orElseGet(() -> {
            final DdiAutoConfirmationState state = DdiAutoConfirmationState.disabled();
            state.add(WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(DdiRootControllerV2.class, tenantAware.getCurrentTenant())
                            .activateAutoConfirmation(tenantAware.getCurrentTenant(), controllerId, null))
                    .withRel(DdiRestConstants.AUTO_CONFIRMATION_ACTIVATE));
            LOG.trace("Returning state auto-conf state disabled for device {}", controllerId);
            return state;
        });
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> activateAutoConfirmation(final String tenant, final String controllerId,
            final DdiActivateAutoConfirmation body) {
        final String initiator = body == null ? null : body.getInitiator();
        final String remark = body == null ? FALLBACK_REMARK : body.getRemark();
        LOG.debug("Activate auto-confirmation request for device '{}' with payload: [initiator='{}' | remark='{}'",
                controllerId, initiator, remark);
        confirmationManagement.activateAutoConfirmation(controllerId, initiator, remark);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> disableAutoConfirmation(final String tenant, final String controllerId) {
        LOG.debug("Deactivate auto-confirmation request for device ‘{}‘", controllerId);
        confirmationManagement.disableAutoConfirmation(controllerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
