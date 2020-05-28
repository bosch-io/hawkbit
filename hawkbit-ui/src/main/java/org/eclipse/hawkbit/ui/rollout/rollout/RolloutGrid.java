/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus.Status;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.StatusIconBuilder.ActionTypeIconSupplier;
import org.eclipse.hawkbit.ui.common.builder.StatusIconBuilder.RolloutStatusIconSupplier;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutToProxyRolloutMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.rollout.DistributionBarHelper;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.cronutils.utils.StringUtils;
import com.google.common.base.Predicates;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * Rollout list grid component.
 */
public class RolloutGrid extends AbstractGrid<ProxyRollout, String> {
    private static final long serialVersionUID = 1L;

    private static final String ROLLOUT_CAPTION_MSG_KEY = "caption.rollout";

    private static final String ROLLOUT_LINK_ID = "rollout";
    private static final String DIST_NAME_VERSION_ID = "distNameVersion";
    private static final String STATUS_ID = "status";
    private static final String TOTAL_TARGETS_COUNT_STATUS_ID = "totalTargetsCountStatus";
    private static final String NUMBER_OF_GROUPS_ID = "numberOfGroups";
    private static final String TOTAL_TARGETS_ID = "totalTargets";
    private static final String CREATED_DATE_ID = "createdDate";
    private static final String CREATED_USER_ID = "createdUser";
    private static final String MODIFIED_DATE_ID = "modifiedDate";
    private static final String MODIFIED_BY_ID = "modifiedBy";
    private static final String APPROVAL_DECIDED_BY_ID = "approvalDecidedBy";
    private static final String APPROVAL_REMARK_ID = "approvalRemark";
    private static final String DESC_ID = "description";
    private static final String ACTION_TYPE_ID = "actionType";

    private static final String APPROVE_BUTTON_ID = "approve";
    private static final String RUN_BUTTON_ID = "run";
    private static final String PAUSE_BUTTON_ID = "pause";
    private static final String UPDATE_BUTTON_ID = "update";
    private static final String COPY_BUTTON_ID = "copy";
    private static final String DELETE_BUTTON_ID = "delete";

    private final RolloutStatusIconSupplier<ProxyRollout> rolloutStatusIconSupplier;
    private final ActionTypeIconSupplier<ProxyRollout> actionTypeIconSupplier;

    private final RolloutManagementUIState rolloutManagementUIState;

    private final transient RolloutManagement rolloutManagement;
    private final transient RolloutToProxyRolloutMapper rolloutMapper;
    private final transient RolloutGroupManagement rolloutGroupManagement;
    private final transient TenantConfigurationManagement tenantConfigManagement;
    private final transient SystemSecurityContext systemSecurityContext;

    private final transient RolloutWindowBuilder rolloutWindowBuilder;
    private final UINotification uiNotification;

    private final transient DeleteSupport<ProxyRollout> rolloutDeleteSupport;

    RolloutGrid(final VaadinMessageSource i18n, final UIEventBus eventBus, final RolloutManagement rolloutManagement,
            final RolloutGroupManagement rolloutGroupManagement, final UINotification uiNotification,
            final RolloutManagementUIState rolloutManagementUIState, final SpPermissionChecker permissionChecker,
            final TenantConfigurationManagement tenantConfigManagement, final RolloutWindowBuilder rolloutWindowBuilder,
            final SystemSecurityContext systemSecurityContext) {
        super(i18n, eventBus, permissionChecker);

        this.rolloutManagementUIState = rolloutManagementUIState;
        this.rolloutManagement = rolloutManagement;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.tenantConfigManagement = tenantConfigManagement;
        this.systemSecurityContext = systemSecurityContext;
        this.uiNotification = uiNotification;
        this.rolloutWindowBuilder = rolloutWindowBuilder;
        this.rolloutMapper = new RolloutToProxyRolloutMapper();

        setSelectionSupport(new SelectionSupport<>(this, eventBus, EventLayout.ROLLOUT_LIST, EventView.ROLLOUT,
                this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState, this::setSelectedEntityIdToUiState));
        getSelectionSupport().disableSelection();

        this.rolloutDeleteSupport = new DeleteSupport<>(this, i18n, uiNotification,
                i18n.getMessage(ROLLOUT_CAPTION_MSG_KEY), ProxyRollout::getName, this::deleteRollout,
                UIComponentIdProvider.ROLLOUT_DELETE_CONFIRMATION_DIALOG);
        this.rolloutDeleteSupport.setConfirmationQuestionDetailsGenerator(this::getDeletionDetails);

        setFilterSupport(new FilterSupport<>(new RolloutDataProvider(rolloutManagement, rolloutMapper)));
        initFilterMappings();

        rolloutStatusIconSupplier = new RolloutStatusIconSupplier<>(i18n, ProxyRollout::getStatus,
                UIComponentIdProvider.ROLLOUT_STATUS_LABEL_ID);
        actionTypeIconSupplier = new ActionTypeIconSupplier<>(i18n, ProxyRollout::getActionType,
                UIComponentIdProvider.ROLLOUT_ACTION_TYPE_LABEL_ID);
        init();
    }

    private void initFilterMappings() {
        getFilterSupport().<String> addMapping(FilterType.SEARCH, (filter, searchText) -> setSearchFilter(searchText),
                rolloutManagementUIState.getSearchText().orElse(null));
    }

    private void setSearchFilter(final String searchText) {
        getFilterSupport().setFilter(!StringUtils.isEmpty(searchText) ? String.format("%%%s%%", searchText) : null);
    }

    public Optional<ProxyRollout> mapIdToProxyEntity(final long entityId) {
        return rolloutManagement.get(entityId).map(rolloutMapper::map);
    }

    private Long getSelectedEntityIdFromUiState() {
        return rolloutManagementUIState.getSelectedRolloutId();
    }

    private void setSelectedEntityIdToUiState(final Long entityId) {
        rolloutManagementUIState.setSelectedRolloutId(entityId);
    }

    private boolean deleteRollout(final Collection<ProxyRollout> rolloutsToBeDeleted) {
        final Collection<Long> rolloutToBeDeletedIds = rolloutsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        rolloutToBeDeletedIds.forEach(rolloutManagement::delete);

        // Rollout is not deleted straight away, but updated to
        // deleting state
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxyRollout.class, rolloutToBeDeletedIds));

        return true;
    }

    private String getDeletionDetails(final ProxyRollout rollout) {
        final Map<Status, Long> statusTotalCount = rollout.getStatusTotalCountMap();

        Long scheduledActions = statusTotalCount.get(Status.SCHEDULED);
        if (scheduledActions == null) {
            scheduledActions = 0L;
        }
        Long runningActions = statusTotalCount.get(Status.RUNNING);
        if (runningActions == null) {
            runningActions = 0L;
        }

        if ((scheduledActions > 0) || (runningActions > 0)) {
            return i18n.getMessage("message.delete.rollout.details", runningActions, scheduledActions);
        }

        return "";
    }

    private static boolean isDeletionAllowed(final RolloutStatus status) {
        final List<RolloutStatus> statesThatAllowDeletion = Arrays.asList(RolloutStatus.CREATING, RolloutStatus.PAUSED,
                RolloutStatus.READY, RolloutStatus.RUNNING, RolloutStatus.STARTING, RolloutStatus.STOPPED,
                RolloutStatus.FINISHED, RolloutStatus.WAITING_FOR_APPROVAL, RolloutStatus.APPROVAL_DENIED);
        return statesThatAllowDeletion.contains(status);
    }

    private static boolean isCopyingAllowed(final RolloutStatus status) {
        return isDeletionAllowed(status);
    }

    private static boolean isEditingAllowed(final RolloutStatus status) {
        final List<RolloutStatus> statesThatAllowEditing = Arrays.asList(RolloutStatus.CREATING, RolloutStatus.PAUSED,
                RolloutStatus.READY, RolloutStatus.RUNNING, RolloutStatus.STARTING, RolloutStatus.STOPPED);
        return statesThatAllowEditing.contains(status);
    }

    private static boolean isPausingAllowed(final RolloutStatus status) {
        return RolloutStatus.RUNNING == status;
    }

    private static boolean isApprovingAllowed(final RolloutStatus status) {
        return RolloutStatus.WAITING_FOR_APPROVAL == status;
    }

    private static boolean isStartingAndResumingAllowed(final RolloutStatus status) {
        final List<RolloutStatus> statesThatAllowStartingAndResuming = Arrays.asList(RolloutStatus.READY,
                RolloutStatus.PAUSED);
        return statesThatAllowStartingAndResuming.contains(status);
    }

    public void updateGridItems(final Collection<Long> ids) {
        ids.stream().filter(Predicates.notNull()).map(rolloutManagement::getWithDetailedStatus)
                .forEach(rollout -> rollout.ifPresent(this::updateGridItem));
    }

    private void updateGridItem(final Rollout rollout) {
        final ProxyRollout proxyRollout = RolloutToProxyRolloutMapper.mapRollout(rollout);

        if (rollout.getRolloutGroupsCreated() == 0) {
            final Long groupsCount = rolloutGroupManagement.countByRollout(rollout.getId());
            proxyRollout.setNumberOfGroups(groupsCount.intValue());
        }

        getDataProvider().refreshItem(proxyRollout);
    }

    private final void hideColumnsDueToInsufficientPermissions() {
        if (!permissionChecker.hasRolloutUpdatePermission()) {
            removeColumn(UPDATE_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutCreatePermission()) {
            removeColumn(COPY_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutApprovalPermission() || !isRolloutApprovalEnabled()) {
            removeColumn(APPROVE_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutDeletePermission()) {
            removeColumn(DELETE_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutHandlePermission()) {
            removeColumn(PAUSE_BUTTON_ID);
            removeColumn(RUN_BUTTON_ID);
        }
    }

    private boolean isRolloutApprovalEnabled() {
        return systemSecurityContext.runAsSystem(() -> tenantConfigManagement
                .getConfigurationValue(TenantConfigurationKey.ROLLOUT_APPROVAL_ENABLED, Boolean.class).getValue());
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ROLLOUT_LIST_GRID_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildRolloutLink).setId(ROLLOUT_LINK_ID).setCaption(i18n.getMessage("header.name"))
                .setHidable(true).setExpandRatio(12);

        addColumn(ProxyRollout::getDistributionSetNameVersion).setId(DIST_NAME_VERSION_ID)
                .setCaption(i18n.getMessage("header.distributionset")).setHidable(true).setExpandRatio(12);

        addComponentColumn(rolloutStatusIconSupplier::getLabel).setId(STATUS_ID)
                .setCaption(i18n.getMessage("header.status")).setHidable(true).setExpandRatio(2);

        addComponentColumn(actionTypeIconSupplier::getLabel).setId(ACTION_TYPE_ID)
                .setCaption(i18n.getMessage("header.type")).setExpandRatio(2).setHidable(true).setHidden(true);

        addColumn(rollout -> DistributionBarHelper.getDistributionBarAsHTMLString(rollout.getStatusTotalCountMap()),
                new HtmlRenderer()).setId(TOTAL_TARGETS_COUNT_STATUS_ID)
                        .setCaption(i18n.getMessage("header.detail.status"))
                        .setDescriptionGenerator(
                                rollout -> DistributionBarHelper.getTooltip(rollout.getStatusTotalCountMap(), i18n),
                                ContentMode.HTML)
                        .setHidable(true).setExpandRatio(60);

        addColumn(ProxyRollout::getNumberOfGroups).setId(NUMBER_OF_GROUPS_ID)
                .setCaption(i18n.getMessage("header.numberofgroups")).setHidable(true).setExpandRatio(2);

        addColumn(ProxyRollout::getTotalTargets).setId(TOTAL_TARGETS_ID)
                .setCaption(i18n.getMessage("header.total.targets")).setHidable(true).setExpandRatio(2);

        addActionColumns();

        addColumn(ProxyRollout::getCreatedDate).setId(CREATED_DATE_ID).setCaption(i18n.getMessage("header.createdDate"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getCreatedBy).setId(CREATED_USER_ID).setCaption(i18n.getMessage("header.createdBy"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getModifiedDate).setId(MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getLastModifiedBy).setId(MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getApprovalDecidedBy).setId(APPROVAL_DECIDED_BY_ID)
                .setCaption(i18n.getMessage("header.approvalDecidedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getApprovalDecidedBy).setId(APPROVAL_REMARK_ID)
                .setCaption(i18n.getMessage("header.approvalRemark")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getDescription).setId(DESC_ID).setCaption(i18n.getMessage("header.description"))
                .setHidable(true).setHidden(true);

        hideColumnsDueToInsufficientPermissions();
    }

    private void addActionColumns() {
        addComponentColumn(rollout -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> startOrResumeRollout(rollout.getId(), rollout.getName(), rollout.getStatus()),
                VaadinIcons.PLAY, UIMessageIdProvider.TOOLTIP_ROLLOUT_RUN, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ROLLOUT_RUN_BUTTON_ID, isStartingAndResumingAllowed(rollout.getStatus())))
                        .setId(RUN_BUTTON_ID).setCaption(i18n.getMessage("header.action.run")).setHidable(false)
                        .setExpandRatio(1);

        addComponentColumn(rollout -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> approveRollout(rollout), VaadinIcons.HANDSHAKE,
                UIMessageIdProvider.TOOLTIP_ROLLOUT_APPROVE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ROLLOUT_APPROVAL_BUTTON_ID, isApprovingAllowed(rollout.getStatus())))
                        .setId(APPROVE_BUTTON_ID).setCaption(i18n.getMessage("header.action.approve")).setHidable(false)
                        .setExpandRatio(1);

        addComponentColumn(rollout -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> pauseRollout(rollout.getId(), rollout.getName(), rollout.getStatus()), VaadinIcons.PAUSE,
                UIMessageIdProvider.TOOLTIP_ROLLOUT_PAUSE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ROLLOUT_PAUSE_BUTTON_ID, isPausingAllowed(rollout.getStatus())))
                        .setId(PAUSE_BUTTON_ID).setCaption(i18n.getMessage("header.action.pause")).setHidable(false)
                        .setExpandRatio(1);

        addComponentColumn(rollout -> GridComponentBuilder.buildActionButton(i18n, clickEvent -> updateRollout(rollout),
                VaadinIcons.EDIT, UIMessageIdProvider.TOOLTIP_ROLLOUT_UPDATE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ROLLOUT_UPDATE_BUTTON_ID, isEditingAllowed(rollout.getStatus())))
                        .setId(UPDATE_BUTTON_ID).setCaption(i18n.getMessage("header.action.update")).setHidable(false)
                        .setExpandRatio(1);

        addComponentColumn(rollout -> GridComponentBuilder.buildActionButton(i18n, clickEvent -> copyRollout(rollout),
                VaadinIcons.COPY, UIMessageIdProvider.TOOLTIP_ROLLOUT_COPY, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ROLLOUT_COPY_BUTTON_ID, isCopyingAllowed(rollout.getStatus())))
                        .setId(COPY_BUTTON_ID).setCaption(i18n.getMessage("header.action.copy")).setHidable(false)
                        .setExpandRatio(1);

        addComponentColumn(rollout -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> rolloutDeleteSupport.openConfirmationWindowDeleteAction(rollout), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ROLLOUT_DELETE_BUTTON_ID, isDeletionAllowed(rollout.getStatus())))
                        .setId(DELETE_BUTTON_ID).setCaption(i18n.getMessage("header.action.delete")).setHidable(false)
                        .setExpandRatio(1);

        getDefaultHeaderRow().join(RUN_BUTTON_ID, APPROVE_BUTTON_ID, PAUSE_BUTTON_ID, UPDATE_BUTTON_ID, COPY_BUTTON_ID,
                DELETE_BUTTON_ID).setText(i18n.getMessage("header.action"));
    }

    private Button buildRolloutLink(final ProxyRollout rollout) {
        final Button rolloutLink = new Button();

        rolloutLink.addClickListener(clickEvent -> onClickOfRolloutName(rollout));
        rolloutLink.setId(new StringBuilder("rollout.link.").append(rollout.getId()).toString());
        // TODO remove duplication of defining the named link with other grid
        // classes by extracting to GridComponentBuilder/SPUIComponentProvider
        rolloutLink.addStyleName("borderless");
        rolloutLink.addStyleName("small");
        rolloutLink.addStyleName("on-focus-no-border");
        rolloutLink.addStyleName("link");
        rolloutLink.setCaption(rollout.getName());
        // this is to allow the button to disappear, if the text is null
        rolloutLink.setVisible(!StringUtils.isEmpty(rollout.getName()));

        if (RolloutStatus.CREATING == rollout.getStatus()) {
            rolloutLink.addStyleName("boldhide");
            rolloutLink.setEnabled(false);
        } else {
            rolloutLink.setEnabled(true);
        }

        return rolloutLink;
    }

    private void onClickOfRolloutName(final ProxyRollout rollout) {
        getSelectionSupport().sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_SELECTED, rollout);
        rolloutManagementUIState.setSelectedRolloutName(rollout.getName());

        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                VisibilityType.SHOW, EventLayout.ROLLOUT_GROUP_LIST, EventView.ROLLOUT));
    }

    private void pauseRollout(final Long rolloutId, final String rolloutName, final RolloutStatus rolloutStatus) {
        if (RolloutStatus.RUNNING != rolloutStatus) {
            return;
        }

        rolloutManagement.pauseRollout(rolloutId);
        uiNotification.displaySuccess(i18n.getMessage("message.rollout.paused", rolloutName));
    }

    private void startOrResumeRollout(final Long rolloutId, final String rolloutName,
            final RolloutStatus rolloutStatus) {
        switch (rolloutStatus) {
        case READY:
            rolloutManagement.start(rolloutId);
            uiNotification.displaySuccess(i18n.getMessage("message.rollout.started", rolloutName));
            break;
        case PAUSED:
            rolloutManagement.resumeRollout(rolloutId);
            uiNotification.displaySuccess(i18n.getMessage("message.rollout.resumed", rolloutName));
            break;
        default:
            break;
        }
    }

    private void approveRollout(final ProxyRollout rollout) {
        final Window approveWindow = rolloutWindowBuilder.getWindowForApproveRollout(rollout);

        approveWindow.setCaption(i18n.getMessage("caption.approve", i18n.getMessage(ROLLOUT_CAPTION_MSG_KEY)));
        UI.getCurrent().addWindow(approveWindow);
        approveWindow.setVisible(Boolean.TRUE);
    }

    private void updateRollout(final ProxyRollout rollout) {
        final Window updateWindow = rolloutWindowBuilder.getWindowForUpdate(rollout);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage(ROLLOUT_CAPTION_MSG_KEY)));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    private void copyRollout(final ProxyRollout rollout) {
        final Window copyWindow = rolloutWindowBuilder.getWindowForCopyRollout(rollout);

        copyWindow.setCaption(i18n.getMessage("caption.copy", i18n.getMessage(ROLLOUT_CAPTION_MSG_KEY)));
        UI.getCurrent().addWindow(copyWindow);
        copyWindow.setVisible(Boolean.TRUE);
    }

    /**
     * Used to show the Rollouts List View in case the currently selected
     * Rollout was deleted.
     * 
     * @param deletedSelectedRolloutId
     *            id of the deleted Rollout that is currently selected
     */
    public void onSelectedRolloutDeleted(final long deletedSelectedRolloutId) {
        uiNotification.displayWarning(
                i18n.getMessage("rollout.not.exists", rolloutManagementUIState.getSelectedRolloutName()));

        showRolloutListLayout();
    }

    private void showRolloutListLayout() {
        if (rolloutManagementUIState.getCurrentLayout().map(currentLayout -> currentLayout != EventLayout.ROLLOUT_LIST)
                .orElse(true)) {
            eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                    new LayoutVisibilityEventPayload(VisibilityType.SHOW, EventLayout.ROLLOUT_LIST, EventView.ROLLOUT));
        }
    }
}
