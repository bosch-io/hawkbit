/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedSelectionAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.MasterEntityChangedListener;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-history-grid and the corresponding header.
 */
public class ActionHistoryGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ActionHistoryGridHeader actionHistoryHeader;
    private final ActionHistoryGrid actionHistoryGrid;

    private final transient MasterEntityChangedListener<ProxyTarget> masterEntityChangedListener;
    private final transient EntityModifiedListener<ProxyAction> entityModifiedListener;

    /**
     * Constructor.
     *
     * @param i18n
     * @param deploymentManagement
     * @param eventBus
     * @param notification
     * @param permChecker
     */
    public ActionHistoryGridLayout(final VaadinMessageSource i18n, final DeploymentManagement deploymentManagement,
            final UIEventBus eventBus, final UINotification notification, final SpPermissionChecker permChecker,
            final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState) {
        this.actionHistoryHeader = new ActionHistoryGridHeader(i18n, eventBus, actionHistoryGridLayoutUiState);
        this.actionHistoryGrid = new ActionHistoryGrid(i18n, deploymentManagement, eventBus, notification, permChecker,
                actionHistoryGridLayoutUiState);

        this.masterEntityChangedListener = new MasterEntityChangedListener<>(eventBus, getMasterEntityAwareComponents(),
                getView(), Layout.TARGET_LIST);
        this.entityModifiedListener = new EntityModifiedListener<>(eventBus, actionHistoryGrid::refreshContainer,
                getEntityModifiedAwareSupports(), ProxyAction.class, ProxyTarget.class, this::getMasterEntityId);

        buildLayout(actionHistoryHeader, actionHistoryGrid);
    }

    private List<MasterEntityAwareComponent<ProxyTarget>> getMasterEntityAwareComponents() {
        return Arrays.asList(actionHistoryHeader, actionHistoryGrid);
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Collections.singletonList(EntityModifiedSelectionAwareSupport.of(actionHistoryGrid.getSelectionSupport(),
                actionHistoryGrid::mapIdToProxyEntity));
    }

    private Optional<Long> getMasterEntityId() {
        return Optional.ofNullable(actionHistoryGrid.getMasterEntityId());
    }

    public void restoreState() {
        actionHistoryHeader.restoreState();
    }

    public void maximize() {
        actionHistoryGrid.createMaximizedContent();
        actionHistoryGrid.getSelectionSupport().selectFirstRow();
    }

    public void minimize() {
        actionHistoryGrid.createMinimizedContent();
    }

    public void unsubscribeListener() {
        entityModifiedListener.unsubscribe();
        masterEntityChangedListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.ACTION_HISTORY_LIST;
    }

    public View getView() {
        return View.DEPLOYMENT;
    }
}
