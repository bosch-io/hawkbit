package org.eclipse.hawkbit.mgmt.rest.resource;


import org.eclipse.hawkbit.mgmt.json.model.PagedList;
import org.eclipse.hawkbit.mgmt.json.model.rollout.MgmtRolloutResponseBody;
import org.eclipse.hawkbit.mgmt.json.model.target.MgmtTarget;
import org.eclipse.hawkbit.mgmt.rest.api.SpringFilterExperimenting;
import org.eclipse.hawkbit.mgmt.rest.resource.util.PagingUtility;
import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.utils.TenantConfigHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SpringFilterExperimentingResource implements SpringFilterExperimenting {
    private final RolloutManagement rolloutManagement;
    private final TargetManagement targetManagement;
    private final SystemSecurityContext systemSecurityContext;
    private final TenantConfigurationManagement tenantConfigurationManagement;

    public SpringFilterExperimentingResource(final RolloutManagement rolloutManagement, final TargetManagement targetManagement, final SystemSecurityContext systemSecurityContext, final TenantConfigurationManagement tenantConfigurationManagement) {
        this.rolloutManagement = rolloutManagement;
        this.targetManagement = targetManagement;
        this.systemSecurityContext = systemSecurityContext;
        this.tenantConfigurationManagement = tenantConfigurationManagement;
    }

    @Override
    public ResponseEntity<PagedList<MgmtRolloutResponseBody>> getRollouts(int pagingOffsetParam, int pagingLimitParam, String sortParam, String filter, String representationModeParam) {
        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeRolloutSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);
        final Page<Rollout> rollouts = rolloutManagement.findAll(pageable, filter);

        final long totalElements = rollouts.getTotalElements();
        final List<MgmtRolloutResponseBody> rest = MgmtRolloutMapper.toResponseRollout(rollouts.getContent(), false);

        return ResponseEntity.ok(new PagedList<>(rest, totalElements));
    }

    @Override
    public ResponseEntity<PagedList<MgmtTarget>> getTargets(int pagingOffsetParam, int pagingLimitParam, String sortParam, String springFilter) {
        final int sanitizedOffsetParam = PagingUtility.sanitizeOffsetParam(pagingOffsetParam);
        final int sanitizedLimitParam = PagingUtility.sanitizePageLimitParam(pagingLimitParam);
        final Sort sorting = PagingUtility.sanitizeRolloutSortParam(sortParam);

        final Pageable pageable = new OffsetBasedPageRequest(sanitizedOffsetParam, sanitizedLimitParam, sorting);

        final Page<Target> targets = targetManagement.findAll(pageable, springFilter);
        final long totalElements = targets.getTotalElements();
        TenantConfigHelper tenantConfigHelper = TenantConfigHelper.usingContext(systemSecurityContext, tenantConfigurationManagement);
        final List<MgmtTarget> rest = MgmtTargetMapper.toResponse(targets.getContent(), tenantConfigHelper);

        return ResponseEntity.ok(new PagedList<>(rest, totalElements));
    }

}
