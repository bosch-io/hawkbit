/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Support for assigning the {@link ProxyTarget} items to {@link ProxyTag}.
 *
 */
public class TargetsToTargetTypeAssignmentSupport extends ToTargetTypeAssignmentSupport<ProxyTarget, Target> {
    private final TargetManagement targetManagement;
    private final UIEventBus eventBus;
    private final SpPermissionChecker permChecker;

    /**
     * Constructor for TargetsToTagAssignmentSupport
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
    protected AbstractAssignmentResult<Target> toggleTargetTypeAssignment(final List<ProxyTarget> sourceItems,
                                                                          final Long typeId) {
        final Collection<String> controllerIdsToAssign = sourceItems.stream().map(ProxyTarget::getControllerId)
                .collect(Collectors.toList());

        return targetManagement.toggleTargetTypeAssignment(controllerIdsToAssign, typeId);
    }

    @Override
    protected String getAssignedEntityTypeMsgKey() {
        return "caption.target";
    }

    @Override
    protected void publishTypeAssignmentEvent(final List<ProxyTarget> sourceItemsToAssign) {
        final List<Long> assignedTargetIds = sourceItemsToAssign.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxyTarget.class, assignedTargetIds));
    }
}
