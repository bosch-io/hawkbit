/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Software module table layout. (Upload Management)
 */
public class SoftwareModuleGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;
    private final transient SoftwareModuleToProxyMapper softwareModuleToProxyMapper;

    private final SoftwareModuleGridHeader softwareModuleGridHeader;
    private final SoftwareModuleGrid softwareModuleGrid;
    private final SoftwareModuleDetailsHeader softwareModuleDetailsHeader;
    private final SoftwareModuleDetails softwareModuleDetails;

    private final SoftwareModuleGridLayoutUiState smGridLayoutUiState;

    private final transient SoftwareModuleGridLayoutEventListener eventListener;

    public SoftwareModuleGridLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UINotification uiNotification, final UIEventBus eventBus,
            final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory,
            final ArtifactUploadState artifactUploadState, final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState,
            final SoftwareModuleGridLayoutUiState smGridLayoutUiState) {
        this.softwareModuleManagement = softwareModuleManagement;
        this.softwareModuleToProxyMapper = new SoftwareModuleToProxyMapper();
        this.smGridLayoutUiState = smGridLayoutUiState;

        final SmWindowBuilder smWindowBuilder = new SmWindowBuilder(i18n, entityFactory, eventBus, uiNotification,
                softwareModuleManagement, softwareModuleTypeManagement);
        final SmMetaDataWindowBuilder smMetaDataWindowBuilder = new SmMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, uiNotification, permChecker, softwareModuleManagement);

        this.softwareModuleGridHeader = new SoftwareModuleGridHeader(i18n, permChecker, eventBus,
                smTypeFilterLayoutUiState, smGridLayoutUiState, smWindowBuilder);
        this.softwareModuleGrid = new SoftwareModuleGrid(eventBus, i18n, permChecker, uiNotification,
                artifactUploadState, smTypeFilterLayoutUiState, smGridLayoutUiState, softwareModuleManagement,
                softwareModuleToProxyMapper);

        this.softwareModuleDetailsHeader = new SoftwareModuleDetailsHeader(i18n, permChecker, eventBus, uiNotification,
                smWindowBuilder, smMetaDataWindowBuilder);
        this.softwareModuleDetails = new SoftwareModuleDetails(i18n, eventBus, softwareModuleManagement,
                smMetaDataWindowBuilder);

        this.eventListener = new SoftwareModuleGridLayoutEventListener(this, eventBus);

        buildLayout(softwareModuleGridHeader, softwareModuleGrid, softwareModuleDetailsHeader, softwareModuleDetails);
    }

    public void restoreState() {
        softwareModuleGridHeader.restoreState();
        softwareModuleGrid.restoreState();

        restoreGridSelection();
    }

    private void restoreGridSelection() {
        final Long lastSelectedEntityId = smGridLayoutUiState.getSelectedSmId();

        if (lastSelectedEntityId != null && softwareModuleGrid.hasSelectionSupport()) {
            selectEntityById(lastSelectedEntityId);
        } else {
            softwareModuleGrid.getSelectionSupport().selectFirstRow();
        }
    }

    // TODO: extract to parent abstract #selectEntityById?
    public void selectEntityById(final Long entityId) {
        if (!softwareModuleGrid.getSelectedItems().isEmpty()) {
            softwareModuleGrid.deselectAll();
        }

        mapIdToProxyEntity(entityId).ifPresent(softwareModuleGrid::select);
    }

    // TODO: extract to parent abstract #mapIdToProxyEntity?
    private Optional<ProxySoftwareModule> mapIdToProxyEntity(final Long entityId) {
        return softwareModuleManagement.get(entityId).map(softwareModuleToProxyMapper::map);
    }

    // TODO: extract to parent #onMasterEntityChanged?
    public void onSmChanged(final ProxySoftwareModule sm) {
        softwareModuleDetailsHeader.masterEntityChanged(sm);
        softwareModuleDetails.masterEntityChanged(sm);
    }

    // TODO: extract to parent #onMasterEntityUpdated?
    public void onSmUpdated(final Collection<Long> entityIds) {
        if (softwareModuleGrid.getSelectedItems().size() == 1) {
            final Long selectedEntityId = softwareModuleGrid.getSelectedItems().iterator().next().getId();

            entityIds.stream().filter(entityId -> entityId.equals(selectedEntityId)).findAny()
                    .ifPresent(updatedEntityId -> mapIdToProxyEntity(updatedEntityId).ifPresent(
                            updatedEntity -> softwareModuleGrid.getSelectionSupport().sendSelectionChangedEvent(
                                    SelectionChangedEventType.ENTITY_SELECTED, updatedEntity)));
        }
    }

    public void showSmTypeHeaderIcon() {
        softwareModuleGridHeader.showSmTypeIcon();
    }

    public void hideSmTypeHeaderIcon() {
        softwareModuleGridHeader.hideSmTypeIcon();
    }

    public void filterGridBySearch(final String searchFilter) {
        softwareModuleGrid.updateSearchFilter(searchFilter);
        softwareModuleGrid.deselectAll();
    }

    public void filterGridByType(final SoftwareModuleType typeFilter) {
        softwareModuleGrid.updateTypeFilter(typeFilter);
        softwareModuleGrid.deselectAll();
    }

    public void maximize() {
        softwareModuleGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        softwareModuleGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void refreshGrid() {
        softwareModuleGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }

    public Layout getLayout() {
        return Layout.SM_LIST;
    }
}