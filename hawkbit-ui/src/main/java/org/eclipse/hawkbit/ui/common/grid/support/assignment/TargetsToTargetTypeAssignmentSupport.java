/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.AbstractAssignmentResult;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.CommonUiDependencies;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning the {@link ProxyTarget} items to {@link ProxyTargetType}.
 *
 */
public class TargetsToTargetTypeAssignmentSupport extends AssignmentSupport<ProxyTarget, ProxyTargetType> {
    private final TargetManagement targetManagement;
    private final UIEventBus eventBus;
    private final SpPermissionChecker permChecker;

    private static final String CAPTION_TYPE = "caption.type";
    private static final String CAPTION_TARGET = "caption.target";
    private static final String CAPTION_TARGETS = "caption.targets";
    
    /**
     * Constructor for TargetsToTargetTypeAssignmentSupport
     *
     * @param uiDependencies
     *            {@link CommonUiDependencies}
     * @param targetManagement
     *            TargetManagement
     */
    public TargetsToTargetTypeAssignmentSupport(final CommonUiDependencies uiDependencies, final TargetManagement targetManagement) {
        super(uiDependencies.getUiNotification(), uiDependencies.getI18n());

        this.eventBus = uiDependencies.getEventBus();
        this.permChecker = uiDependencies.getPermChecker();
        this.targetManagement = targetManagement;
    }

    @Override
    public List<String> getMissingPermissionsForDrop() {
        return permChecker.hasUpdateTargetPermission() ? Collections.emptyList()
                : Collections.singletonList(SpPermission.UPDATE_TARGET);
    }

    @Override
    protected List<ProxyTarget> getFilteredSourceItems(List<ProxyTarget> sourceItemsToAssign, ProxyTargetType targetItem) {
        if (!isAssignmentValid(sourceItemsToAssign, targetItem)) {
            return Collections.emptyList();
        }

        return sourceItemsToAssign;
    }

    /**
     *
     * @param sourceItemsToAssign
     * @param targetItem
     * @return false if some targets already have a type assigned
     */
    private boolean isAssignmentValid(List<ProxyTarget> sourceItemsToAssign, ProxyTargetType targetItem) {
        if(sourceItemsToAssign.size() > 1) {
            List<ProxyTarget> targetsWithDifferentType = sourceItemsToAssign.stream().filter(
                            target -> target.getTargetType() != null && !target.getTargetType().getId().equals(targetItem.getId()))
                    .collect(Collectors.toList());

            if (!targetsWithDifferentType.isEmpty()) {
                notification.displayValidationError(i18n.getMessage(UIMessageIdProvider.MESSAGE_TARGET_TARGETTYPE_ASSIGNED));
                return false;
            }
        }
        return true;
    }

    @Override
    protected void performAssignment(final List<ProxyTarget> sourceItemsToAssign, final ProxyTargetType targetItem) {
        final Long typeId = targetItem.getId();

        final AbstractAssignmentResult<Target> typesAssignmentResult = initiateTargetTypeAssignment(sourceItemsToAssign,
                typeId);

        final String assignmentMsg = createAssignmentMessage(typesAssignmentResult,
                i18n.getMessage(sourceItemsToAssign.size() > 1 ? CAPTION_TARGETS : CAPTION_TARGET),
                i18n.getMessage(CAPTION_TYPE), targetItem.getName());
        notification.displaySuccess(assignmentMsg);

        publishTypeAssignmentEvent(sourceItemsToAssign);
    }

    protected AbstractAssignmentResult<Target> initiateTargetTypeAssignment(final List<ProxyTarget> sourceItems,
                                                                          final Long typeId) {
        final Collection<String> controllerIdsToAssign = sourceItems.stream().map(ProxyTarget::getControllerId)
                .collect(Collectors.toList());

        return targetManagement.assignTargetType(controllerIdsToAssign, typeId);
    }

    protected void publishTypeAssignmentEvent(final List<ProxyTarget> sourceItemsToAssign) {
        final List<Long> assignedTargetIds = sourceItemsToAssign.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxyTarget.class, assignedTargetIds));
    }
}
