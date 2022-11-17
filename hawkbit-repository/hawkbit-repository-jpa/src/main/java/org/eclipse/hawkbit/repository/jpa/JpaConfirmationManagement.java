/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.ConfirmationManagement;
import org.eclipse.hawkbit.repository.ControllerManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.RepositoryProperties;
import org.eclipse.hawkbit.repository.builder.ActionStatusCreate;
import org.eclipse.hawkbit.repository.exception.AutoConfirmationAlreadyActiveException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.jpa.model.JpaAutoConfirmationStatus;
import org.eclipse.hawkbit.repository.jpa.model.JpaTarget;
import org.eclipse.hawkbit.repository.jpa.specifications.TargetSpecifications;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.AutoConfirmationStatus;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * JPA implementation for {@link ConfirmationManagement}.
 *
 */
@Transactional(readOnly = true)
@Validated
public class JpaConfirmationManagement extends JpaActionManagement implements ConfirmationManagement {

    private static final Logger LOG = LoggerFactory.getLogger(JpaConfirmationManagement.class);

    private final ControllerManagement controllerManagement;
    private final EntityFactory entityFactory;
    private final TargetRepository targetRepository;
    private final SystemSecurityContext securityContext;

    /**
     * Constructor
     */
    protected JpaConfirmationManagement(final TargetRepository targetRepository,
            final ActionRepository actionRepository, final RepositoryProperties repositoryProperties,
            final ControllerManagement controllerManagement, final EntityFactory entityFactory,
            final SystemSecurityContext securityContext) {
        super(actionRepository, repositoryProperties);
        this.targetRepository = targetRepository;
        this.controllerManagement = controllerManagement;
        this.entityFactory = entityFactory;
        this.securityContext = securityContext;
    }

    @Override
    public List<Action> findActiveActionsWaitingConfirmation(final String controllerId) {
        return findActiveActionsHavingStatus(controllerId, Status.WAIT_FOR_CONFIRMATION);
    }

    @Override
    public AutoConfirmationStatus activateAutoConfirmation(final String controllerId, final String initiator,
            final String remark) {
        LOG.trace("'activateAutoConfirmation' was called with values: controllerId={}; initiator={}; remark={}",
                controllerId, initiator, remark);
        final JpaTarget target = getByControllerIdAndThrowIfNotFound(controllerId);
        if (target.getAutoConfirmationStatus() != null) {
            LOG.debug("'activateAutoConfirmation' was called for an controller id {} with active auto confirmation.",
                    controllerId);
            throw new AutoConfirmationAlreadyActiveException(controllerId);
        }
        final JpaAutoConfirmationStatus confirmationStatus = new JpaAutoConfirmationStatus(initiator, remark, target);
        target.setAutoConfirmationStatus(confirmationStatus);
        final JpaTarget updatedTarget = targetRepository.save(target);
        final AutoConfirmationStatus autoConfStatus = updatedTarget.getAutoConfirmationStatus();
        if (autoConfStatus == null) {
            final String message = String.format("Persisted auto confirmation status is null. "
                    + "Cannot proceed with giving confirmations for active actions for device %s with initiator %s.",
                    controllerId, initiator);
            LOG.error("message");
            throw new IllegalStateException(message);
        }
        giveConfirmationForActiveActions(autoConfStatus);
        return autoConfStatus;
    }

    @Override
    public Optional<AutoConfirmationStatus> getStatus(final String controllerId) {
        return Optional.of(getByControllerIdAndThrowIfNotFound(controllerId)).map(JpaTarget::getAutoConfirmationStatus);
    }

    @Override
    public List<Action> autoConfirmActiveActions(final String controllerId) {
        final JpaTarget target = getByControllerIdAndThrowIfNotFound(controllerId);
        if (target.getAutoConfirmationStatus() == null) {
            // auto-confirmation is not active
            return Collections.emptyList();
        }
        return giveConfirmationForActiveActions(target.getAutoConfirmationStatus());
    }

    private List<Action> giveConfirmationForActiveActions(final AutoConfirmationStatus autoConfirmationStatus) {
        final Target target = autoConfirmationStatus.getTarget();
        return findActiveActionsHavingStatus(target.getControllerId(), Status.WAIT_FOR_CONFIRMATION).stream()
                .map(action -> autoConfirmAction(action.getId(), autoConfirmationStatus)).collect(Collectors.toList());
    }

    private Action autoConfirmAction(final long actionId, final AutoConfirmationStatus autoConfirmationStatus) {
        final ActionStatusCreate actionStatus = entityFactory.actionStatus().create(actionId).status(Status.RUNNING)
                .messages(Collections.singletonList(autoConfirmationStatus.constructActionMessage()));
        LOG.debug(
                "Automatically confirm actionId '{}' due to active auto-confirmation initiated by '{}' and rollouts system user '{}'",
                actionId, autoConfirmationStatus.getInitiator(), autoConfirmationStatus.getCreatedBy());
        return securityContext.runAsControllerAsTenant(autoConfirmationStatus.getTarget().getTenant(),
                () -> controllerManagement.addUpdateActionStatus(actionStatus));
    }

    @Override
    public void deactivateAutoConfirmation(String controllerId) {
        LOG.debug("Deactivate auto confirmation for controllerId '{}'", controllerId);
        final JpaTarget target = getByControllerIdAndThrowIfNotFound(controllerId);
        target.setAutoConfirmationStatus(null);
        targetRepository.save(target);
    }

    private JpaTarget getByControllerIdAndThrowIfNotFound(final String controllerId) {
        return targetRepository.findOne(TargetSpecifications.hasControllerId(controllerId))
                .orElseThrow(() -> new EntityNotFoundException(Target.class, controllerId));
    }
}
