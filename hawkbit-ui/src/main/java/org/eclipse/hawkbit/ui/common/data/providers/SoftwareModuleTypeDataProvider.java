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

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link SoftwareModuleType}, which dynamically loads a batch
 * of {@link SoftwareModuleType} entities from backend and maps them to
 * corresponding {@link ProxyType} entities.
 */
public class SoftwareModuleTypeDataProvider extends ProxyDataProvider<ProxyType, SoftwareModuleType, String> {

    // TODO: override sortOrders: new Sort(Direction.ASC, "name");

    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;

    public SoftwareModuleTypeDataProvider(final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final TypeToProxyTypeTagMapper<SoftwareModuleType> mapper) {
        super(mapper);
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    @Override
    protected Optional<Slice<SoftwareModuleType>> loadBeans(final PageRequest pageRequest, final String filter) {
        return Optional.of(softwareModuleTypeManagement.findAll(pageRequest));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return softwareModuleTypeManagement.count();
    }

}
