/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import java.util.Arrays;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterButtonsActionsChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CrudMenuHeaderSupport;
import org.eclipse.hawkbit.ui.distributions.disttype.CreateDistributionSetTypeLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar.Command;

/**
 * Distribution Set Type filter buttons header.
 */
// TODO: remove duplication with other FilterHeader classes
public class DSTypeFilterHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState;

    private final transient UINotification uiNotification;
    private final transient EntityFactory entityFactory;
    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;
    private final transient DistributionSetTypeManagement distributionSetTypeManagement;

    private final transient CrudMenuHeaderSupport crudMenuHeaderSupport;
    private final transient CloseHeaderSupport closeHeaderSupport;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param eventBus
     *            UIEventBus
     * @param manageDistUIState
     *            ManageDistUIState
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     * @param distributionSetTypeManagement
     *            DistributionSetTypeManagement
     * @param dSTypeFilterButtons
     *            DSTypeFilterButtons
     */
    DSTypeFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState) {
        super(i18n, permChecker, eventBus);

        this.uiNotification = uiNotification;
        this.entityFactory = entityFactory;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
        this.dSTypeFilterLayoutUiState = dSTypeFilterLayoutUiState;

        this.crudMenuHeaderSupport = new CrudMenuHeaderSupport(i18n, UIComponentIdProvider.DIST_TAG_MENU_BAR_ID,
                permChecker.hasCreateTargetPermission(), permChecker.hasUpdateTargetPermission(),
                permChecker.hasDeleteRepositoryPermission(), getAddButtonCommand(), getUpdateButtonCommand(),
                getDeleteButtonCommand(), getCloseButtonCommand());
        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.HIDE_FILTER_DIST_TYPE,
                this::hideFilterButtonLayout);
        addHeaderSupports(Arrays.asList(crudMenuHeaderSupport, closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage(UIMessageIdProvider.CAPTION_FILTER_BY_TYPE)).buildCaptionLabel();
    }

    private Command getAddButtonCommand() {
        return command -> new CreateDistributionSetTypeLayout(i18n, entityFactory, eventBus, permChecker,
                uiNotification, softwareModuleTypeManagement, distributionSetTypeManagement);
    }

    private Command getUpdateButtonCommand() {
        return command -> {
            eventBus.publish(EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED, this,
                    FilterButtonsActionsChangedEventPayload.SHOW_EDIT);
            crudMenuHeaderSupport.activateEditMode();
        };
    }

    private Command getDeleteButtonCommand() {
        return command -> {
            eventBus.publish(EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED, this,
                    FilterButtonsActionsChangedEventPayload.SHOW_DELETE);
            crudMenuHeaderSupport.activateEditMode();
        };
    }

    private Command getCloseButtonCommand() {
        return command -> {
            eventBus.publish(EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED, this,
                    FilterButtonsActionsChangedEventPayload.HIDE_ALL);
            crudMenuHeaderSupport.activateSelectMode();
        };
    }

    private void hideFilterButtonLayout() {
        eventBus.publish(EventTopics.LAYOUT_VISIBILITY_CHANGED, this,
                LayoutVisibilityChangedEventPayload.LAYOUT_HIDDEN);

        dSTypeFilterLayoutUiState.setHidden(true);
    }
}
