/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype.filter;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractTypeFilterButtons;
import org.eclipse.hawkbit.ui.common.state.TypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

/**
 * Software module type filter buttons.
 *
 */
public class SMTypeFilterButtons extends AbstractTypeFilterButtons {
    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient SmTypeWindowBuilder smTypeWindowBuilder;

    private final EventView view;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     * @param smTypeWindowBuilder
     *            SmTypeWindowBuilder
     */
    public SMTypeFilterButtons(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final SpPermissionChecker permChecker,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final SmTypeWindowBuilder smTypeWindowBuilder, final TypeFilterLayoutUiState typeFilterLayoutUiState,
            final EventView view) {
        super(eventBus, i18n, uiNotification, permChecker, typeFilterLayoutUiState);

        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.smTypeWindowBuilder = smTypeWindowBuilder;
        this.view = view;

        init();
        setDataProvider(
                new SoftwareModuleTypeDataProvider(softwareModuleTypeManagement, new TypeToProxyTypeMapper<>()));
    }

    /**
     * Gets id of the software module type grid.
     *
     * @return id of the grid
     */
    @Override
    public String getGridId() {
        return UIComponentIdProvider.SW_MODULE_TYPE_TABLE_ID;
    }

    @Override
    protected String getFilterButtonsType() {
        return i18n.getMessage("caption.entity.software.module.type");
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return UIComponentIdProvider.SOFTWARE_MODULE_TYPE_ID_PREFIXS;
    }

    @Override
    protected boolean isDefaultType(final ProxyType type) {
        // We do not have default type for software module
        return false;
    }

    @Override
    protected Class<? extends ProxyIdentifiableEntity> getFilterMasterEntityType() {
        return ProxySoftwareModule.class;
    }

    @Override
    protected EventView getView() {
        return view;
    }

    @Override
    protected void deleteType(final ProxyType typeToDelete) {
        softwareModuleTypeManagement.delete(typeToDelete.getId());
    }

    @Override
    protected Window getUpdateWindow(final ProxyType clickedFilter) {
        return smTypeWindowBuilder.getWindowForUpdate(clickedFilter);
    }
}
