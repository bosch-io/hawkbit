/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter.DistributionSetFilterBuilder;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.filters.DsDistributionsFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link DistributionSet}, which dynamically loads a batch of
 * {@link DistributionSet} entities from backend and maps them to corresponding
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetDistributionsStateDataProvider
        extends ProxyDataProvider<ProxyDistributionSet, DistributionSet, DsDistributionsFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DistributionSetTypeManagement distributionSetTypeManagement;

    public DistributionSetDistributionsStateDataProvider(final DistributionSetManagement distributionSetManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetToProxyDistributionMapper entityMapper) {
        super(entityMapper);

        this.distributionSetManagement = distributionSetManagement;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
    }

    @Override
    protected Optional<Slice<DistributionSet>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<DsDistributionsFilterParams> filter) {
        return Optional.of(
                distributionSetManagement.findByDistributionSetFilter(pageRequest, getDistributionSetFilter(filter)));
    }

    private DistributionSetFilter getDistributionSetFilter(final Optional<DsDistributionsFilterParams> filter) {
        return filter.map(filterParams -> {
            final DistributionSetType type = filterParams.getDsTypeId() == null ? null
                    : distributionSetTypeManagement.get(filterParams.getDsTypeId()).orElse(null);

            return new DistributionSetFilterBuilder().setIsDeleted(false).setSearchText(filterParams.getSearchText())
                    .setSelectDSWithNoTag(false).setType(type);
        }).orElse(new DistributionSetFilterBuilder().setIsDeleted(false)).build();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<DsDistributionsFilterParams> filter) {
        return distributionSetManagement.findByDistributionSetFilter(pageRequest, getDistributionSetFilter(filter))
                .getTotalElements();
    }

}