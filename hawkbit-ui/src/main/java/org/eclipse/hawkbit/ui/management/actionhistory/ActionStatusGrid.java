/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionStatusToProxyActionStatusMapper;
import org.eclipse.hawkbit.ui.common.data.providers.ActionStatusDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.management.DeploymentView;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Label;

/**
 * This grid presents the action states for a selected action.
 */
public class ActionStatusGrid extends AbstractGrid<ProxyActionStatus, Long> {
    private static final long serialVersionUID = 1L;

    private static final String STATUS_ID = "status";
    private static final String CREATED_AT_ID = "createdAt";

    private final ManagementUIState managementUIState;

    private final Map<Status, FontIcon> statusIconMap = new EnumMap<>(Status.class);

    private final ConfigurableFilterDataProvider<ProxyActionStatus, Void, Long> actionStatusDataProvider;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param deploymentManagement
     */
    protected ActionStatusGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final DeploymentManagement deploymentManagement, final ManagementUIState managementUIState) {
        super(i18n, eventBus, null);

        this.managementUIState = managementUIState;

        this.actionStatusDataProvider = new ActionStatusDataProvider(deploymentManagement,
                new ActionStatusToProxyActionStatusMapper()).withConfigurableFilter();

        setSelectionSupport(new SelectionSupport<ProxyActionStatus>(this, eventBus, DeploymentView.VIEW_NAME,
                this::updateLastSelectedActionStatusUiState));
        getSelectionSupport().enableSingleSelection();

        initStatusIconMap();

        init();
    }

    private void updateLastSelectedActionStatusUiState(final ProxyActionStatus selectedActionStatus) {
        if (selectedActionStatus.getId().equals(managementUIState.getLastSelectedActionStatusId().orElse(null))) {
            managementUIState.setLastSelectedActionStatusId(null);
        } else {
            managementUIState.setLastSelectedActionStatusId(selectedActionStatus.getId());
        }
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyActionStatus, Void, Long> getFilterDataProvider() {
        return actionStatusDataProvider;
    }

    private void initStatusIconMap() {
        statusIconMap.put(Status.FINISHED, new FontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.FINISHED)));
        statusIconMap.put(Status.SCHEDULED, new FontIcon(VaadinIcons.HOURGLASS_EMPTY,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.SCHEDULED)));
        statusIconMap.put(Status.RUNNING, new FontIcon(VaadinIcons.ADJUST, SPUIStyleDefinitions.STATUS_ICON_PENDING,
                getStatusDescription(Status.RUNNING)));
        statusIconMap.put(Status.RETRIEVED, new FontIcon(VaadinIcons.CHECK_CIRCLE_O,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.RETRIEVED)));
        statusIconMap.put(Status.WARNING, new FontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_ORANGE, getStatusDescription(Status.WARNING)));
        statusIconMap.put(Status.DOWNLOAD, new FontIcon(VaadinIcons.CLOUD_DOWNLOAD,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.DOWNLOAD)));
        statusIconMap.put(Status.DOWNLOADED, new FontIcon(VaadinIcons.CLOUD_DOWNLOAD,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.DOWNLOADED)));
        statusIconMap.put(Status.CANCELING, new FontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.CANCELING)));
        statusIconMap.put(Status.CANCELED, new FontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.CANCELED)));
        statusIconMap.put(Status.ERROR, new FontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getStatusDescription(Status.ERROR)));
    }

    private String getStatusDescription(final Status actionStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_ACTION_STATUS_PREFIX + actionStatus.toString().toLowerCase());
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ACTION_HISTORY_DETAILS_GRID_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildStatusIcon).setId(STATUS_ID).setCaption(i18n.getMessage("header.status"))
                .setMinimumWidth(53d).setMaximumWidth(53d).setHidable(false).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addColumn(actionStatus -> SPDateTimeUtil.getFormattedDate(actionStatus.getCreatedAt(),
                SPUIDefinitions.LAST_QUERY_DATE_FORMAT_SHORT)).setId(CREATED_AT_ID)
                        .setCaption(i18n.getMessage("header.rolloutgroup.target.date")).setMinimumWidth(100d)
                        .setMaximumWidth(400d).setHidable(false).setHidden(false);
    }

    private Label buildStatusIcon(final ProxyActionStatus actionStatus) {
        final FontIcon statusFontIcon = Optional.ofNullable(statusIconMap.get(actionStatus.getStatus()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String statusId = new StringBuilder(UIComponentIdProvider.ACTION_STATUS_GRID_STATUS_LABEL_ID).append(".")
                .append(actionStatus.getId()).toString();

        return buildLabelIcon(statusFontIcon, statusId);
    }
}
