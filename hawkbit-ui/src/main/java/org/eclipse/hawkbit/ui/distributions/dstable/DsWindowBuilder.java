/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

public class DsWindowBuilder extends AbstractEntityWindowBuilder<ProxyDistributionSet> {
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;
    private final SystemManagement systemManagement;
    private final SystemSecurityContext systemSecurityContext;
    private final TenantConfigurationManagement tenantConfigurationManagement;

    private final DistributionSetManagement dsManagement;
    private final DistributionSetTypeManagement dsTypeManagement;

    public DsWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory, final UIEventBus eventBus,
            final UINotification uiNotification, final SystemManagement systemManagement,
            final SystemSecurityContext systemSecurityContext,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final DistributionSetManagement dsManagement, final DistributionSetTypeManagement dsTypeManagement) {
        super(i18n);

        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;
        this.systemManagement = systemManagement;
        this.systemSecurityContext = systemSecurityContext;
        this.tenantConfigurationManagement = tenantConfigurationManagement;

        this.dsManagement = dsManagement;
        this.dsTypeManagement = dsTypeManagement;
    }

    @Override
    protected String getWindowId() {
        return UIComponentIdProvider.CREATE_POPUP_ID;
    }

    public Window getWindowForAddDs() {
        return getWindowForNewEntity(new AddDsWindowController(i18n, entityFactory, eventBus, uiNotification,
                systemManagement, dsManagement,
                new DsWindowLayout(i18n, systemSecurityContext, tenantConfigurationManagement, dsTypeManagement)));

    }

    public Window getWindowForUpdateDs(final ProxyDistributionSet proxyDs) {
        return getWindowForEntity(proxyDs, new UpdateDsWindowController(i18n, entityFactory, eventBus, uiNotification,
                dsManagement,
                new DsWindowLayout(i18n, systemSecurityContext, tenantConfigurationManagement, dsTypeManagement)));
    }
}