/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.mgmt.rest.resource.mapper;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.hawkbit.mgmt.json.model.MgmtMaintenanceWindow;
import org.eclipse.hawkbit.mgmt.json.model.MgmtMetadata;
import org.eclipse.hawkbit.mgmt.json.model.MgmtPollStatus;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtAction;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtActionStatus;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTarget;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTargetAutoConfirm;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTargetRequestBody;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtDistributionSetRestApi;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRestConstants;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRolloutRestApi;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtTargetRestApi;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtTargetTypeRestApi;
import org.eclipse.hawkbit.mgmt.rest.api.SortDirection;
import org.eclipse.hawkbit.repository.ActionFields;
import org.eclipse.hawkbit.repository.ActionStatusFields;
import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.builder.TargetCreate;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.AutoConfirmationStatus;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.PollStatus;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.rest.json.model.ResponseList;
import org.eclipse.hawkbit.util.IpUtil;
import org.eclipse.hawkbit.utils.TenantConfigHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.ObjectUtils;

/**
 * A mapper which maps repository model to RESTful model representation and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MgmtTargetMapper {

    /**
     * Add links to a target response.
     *
     * @param response the target response
     */
    public static void addTargetLinks(final MgmtTarget response) {
        response.add(linkTo(methodOn(MgmtTargetRestApi.class).getAssignedDistributionSet(response.getControllerId()))
                .withRel(MgmtRestConstants.TARGET_V1_ASSIGNED_DISTRIBUTION_SET).expand());
        response.add(linkTo(methodOn(MgmtTargetRestApi.class).getInstalledDistributionSet(response.getControllerId()))
                .withRel(MgmtRestConstants.TARGET_V1_INSTALLED_DISTRIBUTION_SET).expand());
        response.add(linkTo(methodOn(MgmtTargetRestApi.class).getAttributes(response.getControllerId()))
                .withRel(MgmtRestConstants.TARGET_V1_ATTRIBUTES).expand());
        response.add(linkTo(methodOn(MgmtTargetRestApi.class).getActionHistory(response.getControllerId(), null, 0,
                MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT_VALUE,
                ActionFields.ID.getJpaEntityFieldName() + ":" + SortDirection.DESC))
                .withRel(MgmtRestConstants.TARGET_V1_ACTIONS).expand());
        response.add(linkTo(methodOn(MgmtTargetRestApi.class).getMetadata(response.getControllerId()))
                .withRel("metadata").expand());
        if (response.getTargetType() != null) {
            response.add(linkTo(methodOn(MgmtTargetTypeRestApi.class).getTargetType(response.getTargetType()))
                    .withRel(MgmtRestConstants.TARGET_V1_ASSIGNED_TARGET_TYPE).expand());
        }
        if (response.getAutoConfirmActive() != null) {
            response.add(linkTo(methodOn(MgmtTargetRestApi.class).getAutoConfirmStatus(response.getControllerId()))
                    .withRel(MgmtRestConstants.TARGET_V1_AUTO_CONFIRM).expand());
        }
    }

    public static MgmtTargetAutoConfirm getTargetAutoConfirmResponse(final Target target) {
        final AutoConfirmationStatus status = target.getAutoConfirmationStatus();
        final MgmtTargetAutoConfirm response;
        if (status != null) {
            response = MgmtTargetAutoConfirm.active(status.getActivatedAt());
            response.setInitiator(status.getInitiator());
            response.setRemark(status.getRemark());
            response.add(linkTo(methodOn(MgmtTargetRestApi.class).deactivateAutoConfirm(target.getControllerId()))
                    .withRel(MgmtRestConstants.TARGET_V1_DEACTIVATE_AUTO_CONFIRM).expand());
        } else {
            response = MgmtTargetAutoConfirm.disabled();
            response.add(linkTo(methodOn(MgmtTargetRestApi.class).activateAutoConfirm(target.getControllerId(), null))
                    .withRel(MgmtRestConstants.TARGET_V1_ACTIVATE_AUTO_CONFIRM).expand());
        }
        return response;
    }

    /**
     * Create a response for targets.
     *
     * @param targets list of targets
     * @return the response
     */
    public static List<MgmtTarget> toResponse(final Collection<Target> targets, final TenantConfigHelper configHelper) {
        if (targets == null) {
            return Collections.emptyList();
        }

        final Function<Target, PollStatus> pollStatusResolver = configHelper.pollStatusResolver();
        return new ResponseList<>(
                targets.stream().map(target -> toResponse(target, configHelper, pollStatusResolver)).toList());
    }

    /**
     * Create a response for target.
     *
     * @param target the target
     * @return the response
     */
    public static MgmtTarget toResponse(
            final Target target, final TenantConfigHelper configHelper, final Function<Target, PollStatus> pollStatusResolver) {
        if (target == null) {
            return null;
        }
        final MgmtTarget targetRest = new MgmtTarget();
        targetRest.setControllerId(target.getControllerId());
        targetRest.setDescription(target.getDescription());
        targetRest.setName(target.getName());
        targetRest.setUpdateStatus(target.getUpdateStatus().name().toLowerCase());
        targetRest.setGroup(target.getGroup());

        final URI address = target.getAddress();
        if (address != null) {
            if (IpUtil.isIpAddresKnown(address)) {
                targetRest.setIpAddress(address.getHost());
            }
            targetRest.setAddress(address.toString());
        }

        targetRest.setCreatedBy(target.getCreatedBy());
        targetRest.setLastModifiedBy(target.getLastModifiedBy());

        targetRest.setCreatedAt(target.getCreatedAt());
        targetRest.setLastModifiedAt(target.getLastModifiedAt());

        targetRest.setSecurityToken(target.getSecurityToken());
        targetRest.setRequestAttributes(target.isRequestControllerAttributes());

        // last target query is the last controller request date
        final Long lastTargetQuery = target.getLastTargetQuery();
        final Long installationDate = target.getInstallationDate();

        if (lastTargetQuery != null) {
            targetRest.setLastControllerRequestAt(lastTargetQuery);
        }
        if (installationDate != null) {
            targetRest.setInstalledAt(installationDate);
        }
        if (target.getTargetType() != null) {
            targetRest.setTargetType(target.getTargetType().getId());
            targetRest.setTargetTypeName(target.getTargetType().getName());
        }
        if (configHelper.isConfirmationFlowEnabled()) {
            targetRest.setAutoConfirmActive(target.getAutoConfirmationStatus() != null);
        }

        targetRest.add(linkTo(methodOn(MgmtTargetRestApi.class).getTarget(target.getControllerId())).withSelfRel().expand());

        addPollStatus(target, targetRest, pollStatusResolver == null ? configHelper.pollStatusResolver() : pollStatusResolver);

        return targetRest;
    }

    public static List<TargetCreate> fromRequest(final EntityFactory entityFactory, final Collection<MgmtTargetRequestBody> targetsRest) {
        if (targetsRest == null) {
            return Collections.emptyList();
        }

        return targetsRest.stream().map(targetRest -> fromRequest(entityFactory, targetRest)).toList();
    }

    public static Map<String, String> fromRequestMetadata(final List<MgmtMetadata> metadata) {
        return metadata == null
                ? Collections.emptyMap()
                : metadata.stream().collect(Collectors.toMap(MgmtMetadata::getKey, MgmtMetadata::getValue));
    }

    public static List<MgmtActionStatus> toActionStatusRestResponse(
            final Collection<ActionStatus> actionStatus, final DeploymentManagement deploymentManagement) {
        if (actionStatus == null) {
            return Collections.emptyList();
        }

        return actionStatus.stream()
                .map(status -> toResponse(status,
                        deploymentManagement.findMessagesByActionStatusId(
                                status.getId(), PageRequest.of(0, MgmtRestConstants.REQUEST_PARAMETER_PAGING_MAX_LIMIT))
                                .getContent()))
                .toList();
    }

    public static MgmtAction toResponse(final String targetId, final Action action) {
        final MgmtAction result = new MgmtAction();

        result.setId(action.getId());
        result.setType(getType(action));
        if (ActionType.TIMEFORCED == action.getActionType()) {
            result.setForceTime(action.getForcedTime());
        }
        action.getWeight().ifPresent(result::setWeight);
        result.setForceType(MgmtRestModelMapper.convertActionType(action.getActionType()));

        if (action.isActive()) {
            result.setStatus(MgmtAction.ACTION_PENDING);
        } else {
            result.setStatus(MgmtAction.ACTION_FINISHED);
        }

        result.setDetailStatus(action.getStatus().toString().toLowerCase());

        action.getLastActionStatusCode().ifPresent(result::setLastStatusCode);

        final Rollout rollout = action.getRollout();
        if (rollout != null) {
            result.setRollout(rollout.getId());
            result.setRolloutName(rollout.getName());
        }

        if (action.hasMaintenanceSchedule()) {
            final MgmtMaintenanceWindow maintenanceWindow = new MgmtMaintenanceWindow();
            maintenanceWindow.setSchedule(action.getMaintenanceWindowSchedule());
            maintenanceWindow.setDuration(action.getMaintenanceWindowDuration());
            maintenanceWindow.setTimezone(action.getMaintenanceWindowTimeZone());
            action.getMaintenanceWindowStartTime()
                    .ifPresent(nextStart -> maintenanceWindow.setNextStartAt(nextStart.toInstant().toEpochMilli()));
            result.setMaintenanceWindow(maintenanceWindow);
        }

        final String externalRef = action.getExternalRef();
        if (!ObjectUtils.isEmpty(externalRef)) {
            result.setExternalRef(externalRef);
        }

        MgmtRestModelMapper.mapBaseToBase(result, action);

        result.add(linkTo(methodOn(MgmtTargetRestApi.class).getAction(targetId, action.getId())).withSelfRel().expand());

        return result;
    }

    public static MgmtAction toResponseWithLinks(final String controllerId, final Action action) {
        final MgmtAction result = toResponse(controllerId, action);

        if (action.isCancelingOrCanceled()) {
            result.add(linkTo(methodOn(MgmtTargetRestApi.class).getAction(controllerId, action.getId()))
                    .withRel(MgmtRestConstants.TARGET_V1_CANCELED_ACTION).expand());
        }

        result.add(linkTo(methodOn(MgmtTargetRestApi.class).getTarget(controllerId)).withRel("target")
                .withName(action.getTarget().getName()).expand());

        final DistributionSet distributionSet = action.getDistributionSet();
        result.add(linkTo(methodOn(MgmtDistributionSetRestApi.class).getDistributionSet(distributionSet.getId()))
                .withRel("distributionset").withName(distributionSet.getName() + ":" + distributionSet.getVersion())
                .expand());

        result.add(linkTo(methodOn(MgmtTargetRestApi.class).getActionStatusList(controllerId, action.getId(), 0,
                MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT_VALUE,
                ActionStatusFields.ID.getJpaEntityFieldName() + ":" + SortDirection.DESC))
                .withRel(MgmtRestConstants.TARGET_V1_ACTION_STATUS).expand());

        final Rollout rollout = action.getRollout();
        if (rollout != null) {
            result.add(linkTo(methodOn(MgmtRolloutRestApi.class).getRollout(rollout.getId()))
                    .withRel(MgmtRestConstants.TARGET_V1_ROLLOUT).withName(rollout.getName()).expand());
        }

        return result;
    }

    public static List<MgmtAction> toResponse(final String targetId, final Collection<Action> actions) {
        if (actions == null) {
            return Collections.emptyList();
        }

        return actions.stream().map(action -> toResponse(targetId, action)).toList();
    }

    public static MgmtMetadata toResponseMetadata(final String key, final String value) {
        final MgmtMetadata metadataRest = new MgmtMetadata();
        metadataRest.setKey(key);
        metadataRest.setValue(value);
        return metadataRest;
    }

    public static List<MgmtMetadata> toResponseMetadata(final Map<String, String> metadata) {
        return metadata.entrySet().stream().map(e -> toResponseMetadata(e.getKey(), e.getValue())).toList();
    }

    private static void addPollStatus(final Target target, final MgmtTarget targetRest, final Function<Target, PollStatus> pollStatusResolver) {
        final PollStatus pollStatus = pollStatusResolver == null ? target.getPollStatus() : pollStatusResolver.apply(target);
        if (pollStatus != null) {
            final MgmtPollStatus pollStatusRest = new MgmtPollStatus();
            pollStatusRest.setLastRequestAt(
                    Date.from(pollStatus.getLastPollDate().atZone(ZoneId.systemDefault()).toInstant()).getTime());
            pollStatusRest.setNextExpectedRequestAt(
                    Date.from(pollStatus.getNextPollDate().atZone(ZoneId.systemDefault()).toInstant()).getTime());
            pollStatusRest.setOverdue(pollStatus.isOverdue());
            targetRest.setPollStatus(pollStatusRest);
        }
    }

    private static TargetCreate fromRequest(final EntityFactory entityFactory, final MgmtTargetRequestBody targetRest) {
        return entityFactory.target().create().controllerId(targetRest.getControllerId()).name(targetRest.getName())
                .description(targetRest.getDescription()).securityToken(targetRest.getSecurityToken())
                .address(targetRest.getAddress()).targetType(targetRest.getTargetType()).group(targetRest.getGroup());
    }

    private static String getType(final Action action) {
        if (!action.isCancelingOrCanceled()) {
            return MgmtAction.ACTION_UPDATE;
        } else if (action.isCancelingOrCanceled()) {
            return MgmtAction.ACTION_CANCEL;
        }

        return null;
    }

    public static MgmtActionStatus toResponse(final ActionStatus actionStatus, final List<String> messages) {
        final MgmtActionStatus result = new MgmtActionStatus();

        result.setMessages(messages);
        result.setReportedAt(actionStatus.getCreatedAt());
        result.setTimestamp(actionStatus.getOccurredAt());
        result.setId(actionStatus.getId());
        result.setType(MgmtActionStatus.Type.forValue(actionStatus.getStatus().name()));
        actionStatus.getCode().ifPresent(result::setCode);

        return result;
    }
}