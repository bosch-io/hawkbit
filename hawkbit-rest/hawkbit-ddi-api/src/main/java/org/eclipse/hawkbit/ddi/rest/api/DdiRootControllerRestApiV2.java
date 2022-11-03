/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ddi.rest.api;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.eclipse.hawkbit.ddi.json.model.DdiActivateAutoConfirmation;
import org.eclipse.hawkbit.ddi.json.model.DdiAutoConfirmationState;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * REST resource handling for root controller CRUD operations.
 */
@RequestMapping(DdiRestConstants.BASE_V2_REQUEST_MAPPING)
public interface DdiRootControllerRestApiV2 {

    /**
     * Returns the current auto-confirmation state for a given controllerId.
     * 
     * @param tenant
     *            the controllerId is corresponding too
     * @param controllerId
     *            to check the state for
     * @return the state as {@link DdiAutoConfirmationState}
     */
    @GetMapping(value = "/{controllerId}/" + DdiRestConstants.AUTO_CONFIRMATION, produces = { MediaTypes.HAL_JSON_VALUE,
            MediaType.APPLICATION_JSON_VALUE, DdiRestConstants.MEDIA_TYPE_CBOR })
    ResponseEntity<DdiAutoConfirmationState> getAutoConfirmationState(@PathVariable("tenant") final String tenant,
            @PathVariable("controllerId") @NotEmpty final String controllerId);

    /**
     * Activate auto confirmation for a given controllerId. Will use the provided
     * initiator and remark field from the provided
     * {@link DdiActivateAutoConfirmation}. If not present, the values will be
     * prefilled with a default remark and the CONTROLLER as initiator.
     * 
     * @param tenant
     *            the controllerId is corresponding too
     * @param controllerId
     *            to activate auto-confirmation for
     * @param body
     *            as {@link DdiActivateAutoConfirmation}
     * @return {@link org.springframework.http.HttpStatus#OK} if successful or
     *         {@link org.springframework.http.HttpStatus#CONFLICT} in case
     *         auto-confirmation was active already.
     */
    @PostMapping(value = "/{controllerId}/" + DdiRestConstants.AUTO_CONFIRMATION + "/activate", produces = {
            MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE, DdiRestConstants.MEDIA_TYPE_CBOR })
    ResponseEntity<Void> activateAutoConfirmation(@PathVariable("tenant") final String tenant,
            @PathVariable("controllerId") @NotEmpty final String controllerId,
            @Valid @RequestBody(required = false) final DdiActivateAutoConfirmation body);

    /**
     * Disable auto confirmation for a given controller id.
     * 
     * @param tenant
     *            the controllerId is corresponding too
     * @param controllerId
     *            to disable auto-confirmation for
     * @return {@link org.springframework.http.HttpStatus#OK} if successfully
     *         executed
     */
    @PostMapping(value = "/{controllerId}/" + DdiRestConstants.AUTO_CONFIRMATION + "/disable", produces = {
            MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE, DdiRestConstants.MEDIA_TYPE_CBOR })
    ResponseEntity<Void> disableAutoConfirmation(@PathVariable("tenant") final String tenant,
            @PathVariable("controllerId") @NotEmpty final String controllerId);
}
