/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class DistributionSetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final DistributionSetGridHeader distributionSetGridHeader;
    private final DistributionSetGrid distributionSetGrid;
    private final DistributionSetDetailsHeader distributionSetDetailsHeader;
    private final DistributionSetDetails distributionSetDetails;

    public DistributionSetGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManagementUIState managementUIState,
            final ManageDistUIState manageDistUIState, final DistributionSetManagement distributionSetManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final DistributionSetTagManagement distributionSetTagManagement, final SystemManagement systemManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext) {
        super(i18n, eventBus);

        final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout = new DistributionAddUpdateWindowLayout(
                i18n, uiNotification, eventBus, distributionSetManagement, distributionSetTypeManagement,
                systemManagement, entityFactory, configManagement, systemSecurityContext);

        this.distributionSetGridHeader = new DistributionSetGridHeader(i18n, permissionChecker, eventBus,
                manageDistUIState, distributionAddUpdateWindowLayout);
        this.distributionSetGrid = new DistributionSetGrid(eventBus, i18n, permissionChecker, uiNotification,
                manageDistUIState, targetManagement, distributionSetManagement);

        this.distributionSetDetailsHeader = new DistributionSetDetailsHeader(i18n, permissionChecker, eventBus,
                uiNotification, entityFactory, distributionSetManagement, distributionAddUpdateWindowLayout);
        this.distributionSetDetails = new DistributionSetDetails(i18n, eventBus, permissionChecker, manageDistUIState,
                managementUIState, distributionSetManagement, uiNotification, distributionSetTagManagement,
                configManagement, systemSecurityContext, entityFactory);

        buildLayout(distributionSetGridHeader, distributionSetGrid, distributionSetDetailsHeader,
                distributionSetDetails);
    }

    public DistributionSetGrid getDistributionSetGrid() {
        return distributionSetGrid;
    }
}
