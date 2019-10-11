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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.CancelTargetAssignmentEvent;
import org.eclipse.hawkbit.repository.exception.CancelActionNotAllowedException;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionToProxyActionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.ActionDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction.IsActiveDecoration;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.DeploymentView;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.push.CancelTargetAssignmentEventContainer;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * This grid presents the action history for a selected target.
 */
public class ActionHistoryGrid extends AbstractGrid<ProxyAction, String> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ActionHistoryGrid.class);
    private static final double FIXED_PIX_MIN = 25;
    private static final double FIXED_PIX_MAX = 32;

    private static final String ACTION_ID = "id";
    private static final String DS_NAME_VERSION_ID = "dsNameVersion";
    private static final String ROLLOUT_NAME_ID = "rolloutName";
    private static final String MAINTENANCE_WINDOW_ID = "maintenanceWindow";
    private static final String LAST_MODIFIED_AT_ID = "lastModifiedAt";
    private static final String STATUS_ID = "status";
    private static final String ACTIVE_STATUS_ID = "isActiveDecoration";
    private static final String TYPE_ID = "type";
    private static final String TIME_FORCED_ID = "timeForced";

    private static final String CANCEL_BUTTON_ID = "cancel-action";
    private static final String FORCE_BUTTON_ID = "force-action";
    private static final String FORCE_QUIT_BUTTON_ID = "force-quit-action";

    private final transient DeploymentManagement deploymentManagement;
    private final UINotification notification;
    private final ManagementUIState managementUIState;

    private final Map<Status, FontIcon> statusIconMap = new EnumMap<>(Status.class);
    private final Map<IsActiveDecoration, FontIcon> activeStatusIconMap = new EnumMap<>(IsActiveDecoration.class);
    private final Map<ActionType, FontIcon> actionTypeIconMap = new EnumMap<>(ActionType.class);

    private ProxyTarget selectedMasterTarget;

    private final ConfigurableFilterDataProvider<ProxyAction, Void, String> actionDataProvider;

    ActionHistoryGrid(final VaadinMessageSource i18n, final DeploymentManagement deploymentManagement,
            final UIEventBus eventBus, final UINotification notification, final ManagementUIState managementUIState,
            final SpPermissionChecker permissionChecker) {
        super(i18n, eventBus, permissionChecker);

        this.deploymentManagement = deploymentManagement;
        this.notification = notification;
        this.managementUIState = managementUIState;
        this.actionDataProvider = new ActionDataProvider(deploymentManagement, new ActionToProxyActionMapper())
                .withConfigurableFilter();

        setResizeSupport(new ActionHistoryResizeSupport());
        setSelectionSupport(new SelectionSupport<ProxyAction>(this, eventBus, DeploymentView.VIEW_NAME,
                this::updateLastSelectedActionUiState));
        if (managementUIState.isActionHistoryMaximized()) {
            getSelectionSupport().enableSingleSelection();
        } else {
            getSelectionSupport().disableSelection();
        }

        initStatusIconMap();
        initActiveStatusIconMap();
        initActionTypeIconMap();

        init();
    }

    private void updateLastSelectedActionUiState(final ProxyAction selectedAction) {
        if (selectedAction.getId().equals(managementUIState.getLastSelectedActionId().orElse(null))) {
            managementUIState.setLastSelectedActionId(null);
        } else {
            managementUIState.setLastSelectedActionId(selectedAction.getId());
        }
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyAction, Void, String> getFilterDataProvider() {
        return actionDataProvider;
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

    private void initActiveStatusIconMap() {
        activeStatusIconMap.put(IsActiveDecoration.ACTIVE, new FontIcon(null, SPUIStyleDefinitions.STATUS_ICON_ACTIVE,
                getActiveStatusDescription(IsActiveDecoration.ACTIVE)));
        activeStatusIconMap.put(IsActiveDecoration.SCHEDULED, new FontIcon(VaadinIcons.HOURGLASS_EMPTY,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getActiveStatusDescription(IsActiveDecoration.SCHEDULED)));
        activeStatusIconMap.put(IsActiveDecoration.IN_ACTIVE, new FontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_NEUTRAL, getActiveStatusDescription(IsActiveDecoration.IN_ACTIVE)));
        activeStatusIconMap.put(IsActiveDecoration.IN_ACTIVE_ERROR, new FontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getActiveStatusDescription(IsActiveDecoration.IN_ACTIVE_ERROR)));
    }

    private String getActiveStatusDescription(final IsActiveDecoration activeActionStatus) {
        return i18n.getMessage(
                UIMessageIdProvider.TOOLTIP_ACTIVE_ACTION_STATUS_PREFIX + activeActionStatus.toString().toLowerCase());
    }

    private void initActionTypeIconMap() {
        actionTypeIconMap.put(ActionType.FORCED, new FontIcon(VaadinIcons.BOLT, SPUIStyleDefinitions.STATUS_ICON_FORCED,
                i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED)));
        actionTypeIconMap.put(ActionType.TIMEFORCED,
                new FontIcon(VaadinIcons.TIMER, SPUIStyleDefinitions.STATUS_ICON_TIME_FORCED,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_TIME_FORCED)));
        actionTypeIconMap.put(ActionType.SOFT, new FontIcon(VaadinIcons.STEP_FORWARD,
                SPUIStyleDefinitions.STATUS_ICON_SOFT, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_SOFT)));
        actionTypeIconMap.put(ActionType.DOWNLOAD_ONLY,
                new FontIcon(VaadinIcons.DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_DOWNLOAD_ONLY,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_DOWNLOAD_ONLY)));
    }

    @Override
    protected void init() {
        super.init();
        restorePreviousState();
    }

    /**
     * Restores the maximized state if the action history was left in
     * maximized-state and is now re-entered.
     */
    private void restorePreviousState() {
        if (managementUIState.isActionHistoryMaximized()) {
            createMaximizedContent();
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onCancelTargetAssignmentEvents(final CancelTargetAssignmentEventContainer eventContainer) {
        final List<Long> actionIds = eventContainer.getEvents().stream().filter(
                event -> event.getEntity() != null && event.getEntity().getId().equals(selectedMasterTarget.getId()))
                .map(CancelTargetAssignmentEvent::getActionId).collect(Collectors.toList());

        if (!actionIds.isEmpty()) {
            // TODO: Consider updating only corresponding actions with
            // dataProvider.refreshItem() based on
            // action ids instead of full refresh (evaluate
            // getDataCommunicator().getKeyMapper())
            refreshContainer();
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent mgmtUIEvent) {
        if (mgmtUIEvent == ManagementUIEvent.MAX_ACTION_HISTORY) {
            UI.getCurrent().access(this::createMaximizedContent);
        }
        if (mgmtUIEvent == ManagementUIEvent.MIN_ACTION_HISTORY) {
            UI.getCurrent().access(this::createMinimizedContent);
        }
    }

    /**
     * Creates the grid content for maximized-state.
     */
    private void createMaximizedContent() {
        getSelectionSupport().enableSingleSelection();
        // TODO: check if it is needed
        // getDetailsSupport().populateSelection();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    private void createMinimizedContent() {
        getSelectionSupport().disableSelection();
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ACTION_HISTORY_GRID_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildActiveStatusIcon).setId(ACTIVE_STATUS_ID)
                .setCaption(i18n.getMessage("label.active")).setMinimumWidth(50d).setMaximumWidth(50d).setHidable(true)
                .setHidden(false).setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addColumn(ProxyAction::getDsNameVersion).setId(DS_NAME_VERSION_ID)
                .setCaption(i18n.getMessage("distribution.details.header")).setMinimumWidth(107d).setMaximumWidth(500d)
                .setHidable(true).setHidden(false);

        addColumn(action -> SPDateTimeUtil.getFormattedDate(action.getLastModifiedAt(),
                SPUIDefinitions.LAST_QUERY_DATE_FORMAT_SHORT)).setId(LAST_MODIFIED_AT_ID)
                        .setCaption(i18n.getMessage("header.rolloutgroup.target.date")).setMinimumWidth(100d)
                        .setMaximumWidth(130d).setHidable(true).setHidden(false);

        addComponentColumn(this::buildStatusIcon).setId(STATUS_ID).setCaption(i18n.getMessage("header.status"))
                .setMinimumWidth(53d).setMaximumWidth(55d).setHidable(true).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addColumn(ProxyAction::getMaintenanceWindow).setId(MAINTENANCE_WINDOW_ID)
                .setCaption(i18n.getMessage("header.maintenancewindow")).setMinimumWidth(150d).setMaximumWidth(200d)
                .setHidable(true).setHidden(true);

        addComponentColumn(this::buildTypeIcon).setId(TYPE_ID).setMinimumWidth(FIXED_PIX_MIN)
                .setMaximumWidth(FIXED_PIX_MAX).setHidable(false).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addComponentColumn(this::buildTimeforcedIcon).setId(TIME_FORCED_ID).setMinimumWidth(FIXED_PIX_MIN)
                .setMaximumWidth(FIXED_PIX_MAX).setHidable(false).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        getDefaultHeaderRow().join(TYPE_ID, TIME_FORCED_ID).setText(i18n.getMessage("label.action.type"));

        addActionColumns();

        addColumn(ProxyAction::getId).setId(ACTION_ID).setCaption(i18n.getMessage("label.action.id"))
                .setMinimumWidth(FIXED_PIX_MIN).setMaximumWidth(100d).setHidable(true).setHidden(true)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addColumn(ProxyAction::getDsNameVersion).setId(ROLLOUT_NAME_ID)
                .setCaption(i18n.getMessage("caption.rollout.name")).setMinimumWidth(FIXED_PIX_MIN)
                .setMaximumWidth(500d).setHidable(true).setHidden(true);
    }

    private Label buildStatusIcon(final ProxyAction action) {
        final FontIcon statusFontIcon = Optional.ofNullable(statusIconMap.get(action.getStatus()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String statusId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_STATUS_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return buildLabelIcon(statusFontIcon, statusId);
    }

    private Label buildActiveStatusIcon(final ProxyAction action) {
        final FontIcon activeStatusFontIcon = Optional
                .ofNullable(activeStatusIconMap.get(action.getIsActiveDecoration()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String activeStatusId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_ACTIVESTATE_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return buildLabelIcon(activeStatusFontIcon, activeStatusId);
    }

    private Label buildTypeIcon(final ProxyAction action) {
        final FontIcon actionTypeFontIcon = Optional.ofNullable(actionTypeIconMap.get(action.getActionType()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String actionTypeId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_TYPE_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return buildLabelIcon(actionTypeFontIcon, actionTypeId);
    }

    private Label buildTimeforcedIcon(final ProxyAction action) {
        if (ActionType.TIMEFORCED != action.getActionType()) {
            return null;
        }

        final long currentTimeMillis = System.currentTimeMillis();
        String style;
        String description;
        if (action.isHitAutoForceTime(currentTimeMillis)) {
            style = SPUIStyleDefinitions.STATUS_ICON_GREEN;
            final String duration = SPDateTimeUtil.getDurationFormattedString(action.getForcedTime(), currentTimeMillis,
                    i18n);
            description = i18n.getMessage(UIMessageIdProvider.TOOLTIP_TIMEFORCED_FORCED_SINCE, duration);
        } else {
            style = SPUIStyleDefinitions.STATUS_ICON_PENDING;
            final String duration = SPDateTimeUtil.getDurationFormattedString(currentTimeMillis, action.getForcedTime(),
                    i18n);
            description = i18n.getMessage(UIMessageIdProvider.TOOLTIP_TIMEFORCED_FORCED_IN, duration);
        }

        final FontIcon timeforcedFontIcon = new FontIcon(VaadinIcons.TIMER, style, description);

        final String actionTypeId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_TIMEFORCED_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return buildLabelIcon(timeforcedFontIcon, actionTypeId);
    }

    private void addActionColumns() {
        addComponentColumn(action -> buildActionButton(clickEvent -> confirmAndCancelAction(action.getId()),
                VaadinIcons.CLOSE_SMALL, "message.cancel.action", SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ACTION_HISTORY_TABLE_CANCEL_ID + "." + action.getId(),
                !action.isActive() || action.isCancelingOrCanceled() || !permissionChecker.hasUpdateTargetPermission()))
                        .setId(CANCEL_BUTTON_ID).setMinimumWidth(FIXED_PIX_MIN).setMaximumWidth(FIXED_PIX_MAX)
                        .setHidable(false).setHidden(false);

        addComponentColumn(action -> buildActionButton(clickEvent -> confirmAndForceAction(action.getId()),
                VaadinIcons.BOLT, "message.force.action", SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ACTION_HISTORY_TABLE_FORCE_ID + "." + action.getId(),
                !action.isActive() || action.isForce() || action.isCancelingOrCanceled()
                        || !permissionChecker.hasUpdateTargetPermission())).setId(FORCE_BUTTON_ID)
                                .setMinimumWidth(FIXED_PIX_MIN).setMaximumWidth(FIXED_PIX_MAX).setHidable(false)
                                .setHidden(false);

        addComponentColumn(action -> buildActionButton(clickEvent -> confirmAndForceQuitAction(action.getId()),
                VaadinIcons.CLOSE_SMALL, "message.forcequit.action", SPUIStyleDefinitions.STATUS_ICON_RED,
                UIComponentIdProvider.ACTION_HISTORY_TABLE_FORCE_QUIT_ID + "." + action.getId(),
                !action.isActive() || !action.isCancelingOrCanceled()
                        || !permissionChecker.hasUpdateTargetPermission())).setId(FORCE_QUIT_BUTTON_ID)
                                .setMinimumWidth(FIXED_PIX_MIN).setMaximumWidth(FIXED_PIX_MAX).setHidable(false)
                                .setHidden(false);

        getDefaultHeaderRow().join(CANCEL_BUTTON_ID, FORCE_BUTTON_ID, FORCE_QUIT_BUTTON_ID)
                .setText(i18n.getMessage("header.action"));
    }

    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final String style, final String buttonId, final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(style);

        return actionButton;
    }

    /**
     * Show confirmation window and if ok then only, force the action.
     *
     * @param actionId
     *            as Id if the action needs to be forced.
     */
    private void confirmAndForceAction(final Long actionId) {
        /* Display the confirmation */
        final ConfirmationDialog confirmDialog = new ConfirmationDialog(
                i18n.getMessage("caption.force.action.confirmbox"), i18n.getMessage("message.force.action.confirm"),
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (!ok) {
                        return;
                    }
                    deploymentManagement.forceTargetAction(actionId);
                    updateTargetDetails();
                    notification.displaySuccess(i18n.getMessage("message.force.action.success"));
                }, UIComponentIdProvider.CONFIRMATION_POPUP_ID);
        UI.getCurrent().addWindow(confirmDialog.getWindow());

        confirmDialog.getWindow().bringToFront();
    }

    private void updateTargetDetails() {
        // show the updated target action history details
        refreshContainer();
        // update the target table and its pinning details
        updateTargetAndDsTable();
    }

    private void updateTargetAndDsTable() {
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.UPDATED_ENTITY, selectedMasterTarget));
        updateDistributionTableStyle();
    }

    /**
     * Update the colors of Assigned and installed distribution set in Target
     * Pinning.
     */
    private void updateDistributionTableStyle() {
        managementUIState.getDistributionTableFilters().getPinnedTarget().ifPresent(pinnedTarget -> {
            if (pinnedTarget.getTargetId().equals(selectedMasterTarget.getId())) {
                eventBus.publish(this, PinUnpinEvent.PIN_TARGET);
            }
        });
    }

    /**
     * Show confirmation window and if ok then only, force quit action.
     *
     * @param actionId
     *            as Id if the action needs to be forced.
     */
    private void confirmAndForceQuitAction(final Long actionId) {
        /* Display the confirmation */
        final ConfirmationDialog confirmDialog = new ConfirmationDialog(
                i18n.getMessage("caption.forcequit.action.confirmbox"),
                i18n.getMessage("message.forcequit.action.confirm"), i18n.getMessage(UIMessageIdProvider.BUTTON_OK),
                i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL), ok -> {
                    if (!ok) {
                        return;
                    }
                    final boolean cancelResult = forceQuitActiveAction(actionId);
                    if (cancelResult) {
                        updateTargetDetails();
                        notification.displaySuccess(i18n.getMessage("message.forcequit.action.success"));
                    } else {
                        notification.displayValidationError(i18n.getMessage("message.forcequit.action.failed"));
                    }
                }, VaadinIcons.WARNING, UIComponentIdProvider.CONFIRMATION_POPUP_ID, null);
        UI.getCurrent().addWindow(confirmDialog.getWindow());

        confirmDialog.getWindow().bringToFront();
    }

    /**
     * Show confirmation window and if ok then only, cancel the action.
     *
     * @param actionId
     *            as Id if the action needs to be cancelled.
     */
    private void confirmAndCancelAction(final Long actionId) {
        if (actionId == null) {
            return;
        }

        final ConfirmationDialog confirmDialog = new ConfirmationDialog(
                i18n.getMessage("caption.cancel.action.confirmbox"), i18n.getMessage("message.cancel.action.confirm"),
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (!ok) {
                        return;
                    }
                    final boolean cancelResult = cancelActiveAction(actionId);
                    if (cancelResult) {
                        updateTargetDetails();
                        notification.displaySuccess(i18n.getMessage("message.cancel.action.success"));
                    } else {
                        notification.displayValidationError(i18n.getMessage("message.cancel.action.failed"));
                    }
                }, UIComponentIdProvider.CONFIRMATION_POPUP_ID);
        UI.getCurrent().addWindow(confirmDialog.getWindow());
        confirmDialog.getWindow().bringToFront();
    }

    // service call to cancel the active action
    private boolean cancelActiveAction(final Long actionId) {
        if (actionId != null) {
            try {
                deploymentManagement.cancelAction(actionId);
                return true;
            } catch (final CancelActionNotAllowedException e) {
                LOG.info("Cancel action not allowed exception :{}", e);
                return false;
            }
        }
        return false;
    }

    // service call to cancel the active action
    private boolean forceQuitActiveAction(final Long actionId) {
        if (actionId != null) {
            try {
                deploymentManagement.forceQuitAction(actionId);
                return true;
            } catch (final CancelActionNotAllowedException e) {
                LOG.info("Force Cancel action not allowed exception :{}", e);
                return false;
            }
        }
        return false;
    }

    public void setSelectedMasterTarget(final ProxyTarget selectedMasterTarget) {
        this.selectedMasterTarget = selectedMasterTarget;
    }

    /**
     * Adds support to resize the action history grid.
     */
    class ActionHistoryResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { ACTIVE_STATUS_ID, ACTION_ID, DS_NAME_VERSION_ID,
                LAST_MODIFIED_AT_ID, STATUS_ID, MAINTENANCE_WINDOW_ID, ROLLOUT_NAME_ID, TYPE_ID, TIME_FORCED_ID,
                CANCEL_BUTTON_ID, FORCE_BUTTON_ID, FORCE_QUIT_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { ACTIVE_STATUS_ID, DS_NAME_VERSION_ID,
                LAST_MODIFIED_AT_ID, STATUS_ID, MAINTENANCE_WINDOW_ID, TYPE_ID, TIME_FORCED_ID, CANCEL_BUTTON_ID,
                FORCE_BUTTON_ID, FORCE_QUIT_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(ACTION_ID).setHidden(false);
            getColumn(ROLLOUT_NAME_ID).setHidden(false);
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
            getColumn(LAST_MODIFIED_AT_ID).setMinimumWidth(100d).setMaximumWidth(150d);
        }

        @Override
        public void setMinimizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(minColumnOrder);
        }

        @Override
        public void setMinimizedHiddenColumns() {
            getColumn(ACTION_ID).setHidden(true);
            getColumn(ROLLOUT_NAME_ID).setHidden(true);
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
            getColumn(LAST_MODIFIED_AT_ID).setMinimumWidth(100d).setMaximumWidth(130d);
        }
    }
}
