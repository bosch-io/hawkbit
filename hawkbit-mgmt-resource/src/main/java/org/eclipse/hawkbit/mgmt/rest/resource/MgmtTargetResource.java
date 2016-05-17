/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.mgmt.rest.resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.hawkbit.mgmt.json.model.PagedList;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtAction;
import org.eclipse.hawkbit.mgmt.json.model.action.MgmtActionStatus;
import org.eclipse.hawkbit.mgmt.json.model.distributionset.MgmtDistributionSet;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtDistributionSetAssigment;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTarget;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTargetAttributes;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTargetRequestBody;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtDistributionSetRestApi;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRestConstants;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtTargetRestApi;
import org.eclipse.hawkbit.repository.ActionFields;
import org.eclipse.hawkbit.repository.ActionStatusFields;
import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.TargetFields;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.rsql.RSQLUtility;
import org.eclipse.hawkbit.rest.data.SortDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Resource handling target CRUD operations.
 */
@RestController
public class MgmtTargetResource implements MgmtTargetRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(MgmtTargetResource.class);

    @Autowired
    private TargetManagement targetManagement;

    @Autowired
    private DeploymentManagement deploymentManagement;

    @Override
    public ResponseEntity<MgmtTarget> getTarget(@PathVariable("targetId") final String targetId) {
        final Target findTarget = findTargetWithExceptionIfNotFound(targetId);
        // to single response include poll status
        final MgmtTarget response = MgmtTargetMapper.toResponse(findTarget);
        MgmtTargetMapper.addPollStatus(findTarget, response);
        MgmtTargetMapper.addTargetLinks(response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PagedList<MgmtTarget>> getTargets(
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeTargetSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);
        final Slice<Target> findTargetsAll;
        final Long countTargetsAll;
        if (rsqlParam != null) {
            final Page<Target> findTargetPage = this.targetManagement
                    .findTargetsAll(RSQLUtility.parse(rsqlParam, TargetFields.class), pageable);
            countTargetsAll = findTargetPage.getTotalElements();
            findTargetsAll = findTargetPage;
        } else {
            findTargetsAll = this.targetManagement.findTargetsAll(pageable);
            countTargetsAll = this.targetManagement.countTargetsAll();
        }

        final List<MgmtTarget> rest = MgmtTargetMapper.toResponse(findTargetsAll.getContent());
        return new ResponseEntity<>(new PagedList<MgmtTarget>(rest, countTargetsAll), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<MgmtTarget>> createTargets(@RequestBody final List<MgmtTargetRequestBody> targets) {
        LOG.debug("creating {} targets", targets.size());
        final Iterable<Target> createdTargets = this.targetManagement
                .createTargets(MgmtTargetMapper.fromRequest(targets));
        LOG.debug("{} targets created, return status {}", targets.size(), HttpStatus.CREATED);
        return new ResponseEntity<>(MgmtTargetMapper.toResponse(createdTargets), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<MgmtTarget> updateTarget(@PathVariable("targetId") final String targetId,
            @RequestBody final MgmtTargetRequestBody targetRest) {
        final Target existingTarget = findTargetWithExceptionIfNotFound(targetId);
        LOG.debug("updating target {}", existingTarget.getId());
        if (targetRest.getDescription() != null) {
            existingTarget.setDescription(targetRest.getDescription());
        }
        if (targetRest.getName() != null) {
            existingTarget.setName(targetRest.getName());
        }
        final Target updateTarget = this.targetManagement.updateTarget(existingTarget);

        return new ResponseEntity<>(MgmtTargetMapper.toResponse(updateTarget), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteTarget(@PathVariable("targetId") final String targetId) {
        final Target target = findTargetWithExceptionIfNotFound(targetId);
        this.targetManagement.deleteTargets(target.getId());
        LOG.debug("{} target deleted, return status {}", targetId, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtTargetAttributes> getAttributes(@PathVariable("targetId") final String targetId) {
        final Target foundTarget = findTargetWithExceptionIfNotFound(targetId);
        final Map<String, String> controllerAttributes = foundTarget.getTargetInfo().getControllerAttributes();
        if (controllerAttributes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        final MgmtTargetAttributes result = new MgmtTargetAttributes();
        result.putAll(controllerAttributes);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PagedList<MgmtAction>> getActionHistory(@PathVariable("targetId") final String targetId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SEARCH, required = false) final String rsqlParam) {

        final Target foundTarget = findTargetWithExceptionIfNotFound(targetId);

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeActionSortParam(sortParam);
        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);

        final Slice<Action> activeActions;
        final Long totalActionCount;
        if (rsqlParam != null) {
            final Specification<Action> parse = RSQLUtility.parse(rsqlParam, ActionFields.class);
            activeActions = this.deploymentManagement.findActionsByTarget(parse, foundTarget, pageable);
            totalActionCount = this.deploymentManagement.countActionsByTarget(parse, foundTarget);
        } else {
            activeActions = this.deploymentManagement.findActionsByTarget(foundTarget, pageable);
            totalActionCount = this.deploymentManagement.countActionsByTarget(foundTarget);
        }

        return new ResponseEntity<>(
                new PagedList<>(MgmtTargetMapper.toResponse(targetId, activeActions.getContent()), totalActionCount),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MgmtAction> getAction(@PathVariable("targetId") final String targetId,
            @PathVariable("actionId") final Long actionId) {
        final Target target = findTargetWithExceptionIfNotFound(targetId);

        final Action action = findActionWithExceptionIfNotFound(actionId);
        if (!action.getTarget().getId().equals(target.getId())) {
            LOG.warn("given action ({}) is not assigned to given target ({}).", action.getId(), target.getId());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final MgmtAction result = MgmtTargetMapper.toResponse(targetId, action, action.isActive());

        if (!action.isCancelingOrCanceled()) {
            result.add(linkTo(
                    methodOn(MgmtDistributionSetRestApi.class).getDistributionSet(action.getDistributionSet().getId()))
                            .withRel("distributionset"));
        } else if (action.isCancelingOrCanceled()) {
            result.add(linkTo(methodOn(MgmtTargetRestApi.class).getAction(targetId, action.getId()))
                    .withRel(MgmtRestConstants.TARGET_V1_CANCELED_ACTION));
        }

        result.add(linkTo(methodOn(MgmtTargetRestApi.class).getActionStatusList(targetId, action.getId(), 0,
                MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT_VALUE,
                ActionStatusFields.ID.getFieldName() + ":" + SortDirection.DESC))
                        .withRel(MgmtRestConstants.TARGET_V1_ACTION_STATUS));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> cancelAction(@PathVariable("targetId") final String targetId,
            @PathVariable("actionId") final Long actionId,
            @RequestParam(value = "force", required = false, defaultValue = "false") final boolean force) {
        final Target target = findTargetWithExceptionIfNotFound(targetId);
        final Action action = findActionWithExceptionIfNotFound(actionId);

        if (force) {
            this.deploymentManagement.forceQuitAction(action);
        } else {
            this.deploymentManagement.cancelAction(action, target);
        }
        // both functions will throw an exception, when action is in wrong
        // state, which is mapped by MgmtResponseExceptionHandler.

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<PagedList<MgmtActionStatus>> getActionStatusList(
            @PathVariable("targetId") final String targetId, @PathVariable("actionId") final Long actionId,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_OFFSET, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_OFFSET) final int pagingOffsetParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_PAGING_LIMIT, defaultValue = MgmtRestConstants.REQUEST_PARAMETER_PAGING_DEFAULT_LIMIT) final int pagingLimitParam,
            @RequestParam(value = MgmtRestConstants.REQUEST_PARAMETER_SORTING, required = false) final String sortParam) {

        final Target target = findTargetWithExceptionIfNotFound(targetId);

        final Action action = findActionWithExceptionIfNotFound(actionId);
        if (!action.getTarget().getId().equals(target.getId())) {
            LOG.warn("given action ({}) is not assigned to given target ({}).", action.getId(), target.getId());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeActionStatusSortParam(sortParam);

        final Page<ActionStatus> statusList = this.deploymentManagement.findActionStatusByAction(
                new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting), action, true);

        return new ResponseEntity<>(
                new PagedList<>(MgmtTargetMapper.toActionStatusRestResponse(statusList.getContent()),
                        statusList.getTotalElements()),
                HttpStatus.OK);

    }

    @Override
    public ResponseEntity<MgmtDistributionSet> getAssignedDistributionSet(
            @PathVariable("targetId") final String targetId) {
        final Target findTarget = findTargetWithExceptionIfNotFound(targetId);
        final MgmtDistributionSet distributionSetRest = MgmtDistributionSetMapper
                .toResponse(findTarget.getAssignedDistributionSet());
        final HttpStatus retStatus;
        if (distributionSetRest == null) {
            retStatus = HttpStatus.NO_CONTENT;
        } else {
            retStatus = HttpStatus.OK;
        }
        return new ResponseEntity<>(distributionSetRest, retStatus);
    }

    @Override
    public ResponseEntity<Void> postAssignedDistributionSet(@PathVariable("targetId") final String targetId,
            @RequestBody final MgmtDistributionSetAssigment dsId) {

        findTargetWithExceptionIfNotFound(targetId);
        final ActionType type = (dsId.getType() != null) ? MgmtRestModelMapper.convertActionType(dsId.getType())
                : ActionType.FORCED;
        final Iterator<Target> changed = this.deploymentManagement
                .assignDistributionSet(dsId.getId(), type, dsId.getForcetime(), targetId).getAssignedEntity()
                .iterator();
        if (changed.hasNext()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        LOG.error("Target update (ds {} assigment to target {}) failed! Returnd target list is empty.", dsId.getId(),
                targetId);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Override
    public ResponseEntity<MgmtDistributionSet> getInstalledDistributionSet(
            @PathVariable("targetId") final String targetId) {
        final Target findTarget = findTargetWithExceptionIfNotFound(targetId);
        final MgmtDistributionSet distributionSetRest = MgmtDistributionSetMapper
                .toResponse(findTarget.getTargetInfo().getInstalledDistributionSet());
        final HttpStatus retStatus;
        if (distributionSetRest == null) {
            retStatus = HttpStatus.NO_CONTENT;
        } else {
            retStatus = HttpStatus.OK;
        }
        return new ResponseEntity<>(distributionSetRest, retStatus);
    }

    private Target findTargetWithExceptionIfNotFound(final String targetId) {
        final Target findTarget = this.targetManagement.findTargetByControllerID(targetId);
        if (findTarget == null) {
            throw new EntityNotFoundException("Target with Id {" + targetId + "} does not exist");
        }
        return findTarget;
    }

    private Action findActionWithExceptionIfNotFound(final Long actionId) {
        final Action findAction = this.deploymentManagement.findAction(actionId);
        if (findAction == null) {
            throw new EntityNotFoundException("Action with Id {" + actionId + "} does not exist");
        }
        return findAction;
    }

}
