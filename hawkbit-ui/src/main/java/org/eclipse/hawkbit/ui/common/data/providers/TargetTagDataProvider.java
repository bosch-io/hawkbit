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

import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetTagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link TargetTag}, which dynamically loads a batch of
 * {@link TargetTag} entities from backend and maps them to corresponding
 * {@link ProxyTag} entities.
 */
public class TargetTagDataProvider extends ProxyDataProvider<ProxyTag, TargetTag, String> {

    // TODO: override sortOrders: new Sort(Direction.ASC, "name");

    private static final long serialVersionUID = 1L;

    private final transient TargetTagManagement tagManagementService;

    public TargetTagDataProvider(final TargetTagManagement tagManagementService,
            final TargetTagToProxyTagMapper mapper) {
        super(mapper);
        this.tagManagementService = tagManagementService;
    }

    @Override
    protected Optional<Slice<TargetTag>> loadBeans(final PageRequest pageRequest, final String filter) {
        return Optional.of(tagManagementService.findAll(pageRequest));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return tagManagementService.findAll(pageRequest).getTotalElements();
    }

}
