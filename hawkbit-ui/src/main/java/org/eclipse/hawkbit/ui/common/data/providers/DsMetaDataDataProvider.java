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
import org.eclipse.hawkbit.repository.model.DistributionSetMetadata;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Data provider for {@link DistributionSetMetadata}, which dynamically loads a
 * batch of {@link DistributionSetMetadata} entities from backend and maps them
 * to corresponding {@link ProxyMetaData} entities.
 */
public class DsMetaDataDataProvider extends AbstractMetaDataDataProvider<DistributionSetMetadata, Long> {
    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;

    public DsMetaDataDataProvider(final DistributionSetManagement distributionSetManagement) {
        this.distributionSetManagement = distributionSetManagement;
    }

    @Override
    protected Optional<Page<DistributionSetMetadata>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Long> currentlySelectedDsId) {
        return currentlySelectedDsId
                .map(id -> distributionSetManagement.findMetaDataByDistributionSetId(pageRequest, id));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Long> currentlySelectedDsId) {
        return currentlySelectedDsId.map(
                id -> distributionSetManagement.findMetaDataByDistributionSetId(pageRequest, id).getTotalElements())
                .orElse(0L);
    }
}