/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.eventbus.event.RolloutGroupStatusUpdateEvent;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.RolloutTargetsStatusCount;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleSmallNoBorder;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPUIComponetIdProvider;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.TableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.alump.distributionbar.DistributionBar;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Rollout Group Table in List view.
 *
 */
@SpringComponent
@ViewScope
public class RolloutGroupListTable extends AbstractSimpleTable {

    private static final long serialVersionUID = 1182656768844867443L;

    @Autowired
    private I18N i18n;

    @Autowired
    private transient EventBus.SessionEventBus eventBus;

    @Autowired
    private transient RolloutManagement rolloutManagement;

    @Autowired
    private transient RolloutUIState rolloutUIState;

    @PostConstruct
    protected void init() {
        super.init();
        eventBus.subscribe(this);
    }

    @PreDestroy
    void destroy() {
        eventBus.unsubscribe(this);
    }

    @EventBusListenerMethod(scope = EventScope.SESSION)
    void onEvent(final RolloutEvent event) {
        if (event == RolloutEvent.SHOW_ROLLOUT_GROUPS) {
            ((LazyQueryContainer) getContainerDataSource()).refresh();
        }
    }

    /**
     * EventListener method which is called when a list of events is published.
     * Event types should not be mixed up.
     *
     * @param events
     *            list of events
     */
    @EventBusListenerMethod(scope = EventScope.SESSION)
    public void onEvents(final List<?> events) {
        final Object firstEvent = events.get(0);
        if (RolloutGroupStatusUpdateEvent.class.isInstance(firstEvent)) {
            onRolloutGroupStatusChange((List<RolloutGroupStatusUpdateEvent>) events);
        }
    }

    private void onRolloutGroupStatusChange(final List<RolloutGroupStatusUpdateEvent> events) {
        final List<Object> visibleItemIds = (List<Object>) getVisibleItemIds();
        for (final RolloutGroupStatusUpdateEvent rolloutGroupStatusUpdateEvent : events) {
            final RolloutGroup rolloutGroup = rolloutGroupStatusUpdateEvent.getEntity();
            if (visibleItemIds.contains(rolloutGroup.getId())) {
                updateVisibleItemOnEvent(rolloutGroup);
            }
        }
    }

    private void updateVisibleItemOnEvent(final RolloutGroup rolloutGroup) {
        final LazyQueryContainer rolloutContainer = (LazyQueryContainer) getContainerDataSource();
        final Item item = rolloutContainer.getItem(rolloutGroup.getId());
        item.getItemProperty(SPUILabelDefinitions.VAR_STATUS).setValue(rolloutGroup.getStatus());
    }

    @Override
    protected List<TableColumn> getTableVisibleColumns() {
        final List<TableColumn> columnList = new ArrayList<TableColumn>();
        columnList.add(new TableColumn(SPUIDefinitions.ROLLOUT_GROUP_NAME, i18n.get("header.name"), 0.1f));
        columnList.add(new TableColumn(SPUILabelDefinitions.VAR_CREATED_DATE, i18n
                .get("header.rolloutgroup.started.date"), 0.2f));
        columnList.add(new TableColumn(SPUIDefinitions.ROLLOUT_GROUP_ERROR_THRESHOLD, i18n
                .get("header.rolloutgroup.threshold.error"), 0.15f));
        columnList.add(new TableColumn(SPUIDefinitions.ROLLOUT_GROUP_THRESHOLD, i18n
                .get("header.rolloutgroup.threshold"), 0.15f));
        columnList.add(new TableColumn(SPUIDefinitions.ROLLOUT_GROUP_INSTALLED_PERCENTAGE, i18n
                .get("header.rolloutgroup.installed.percentage"), 0.15f));
        columnList.add(new TableColumn(SPUIDefinitions.ROLLOUT_GROUP_STATUS, i18n.get("header.status"), 0.1f));
        columnList.add(new TableColumn(SPUIDefinitions.DETAIL_STATUS, i18n.get("header.detail.status"), 0.15f));

        return columnList;
    }

    @Override
    protected Container createContainer() {
        final BeanQueryFactory<RolloutGroupBeanQuery> rolloutQf = new BeanQueryFactory<RolloutGroupBeanQuery>(
                RolloutGroupBeanQuery.class);
        final LazyQueryContainer rolloutGroupTableContainer = new LazyQueryContainer(new LazyQueryDefinition(true,
                SPUIDefinitions.PAGE_SIZE, SPUILabelDefinitions.VAR_ID), rolloutQf);
        return rolloutGroupTableContainer;
    }

    @Override
    protected void addContainerProperties(final Container container) {
        final LazyQueryContainer rolloutTableContainer = (LazyQueryContainer) container;
        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_ID, String.class, null, false, false);
        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_NAME, String.class, "", false, false);
        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_DESC, String.class, null, false, false);
        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_STATUS, RolloutGroupStatus.class, null,
                false, false);
        rolloutTableContainer.addContainerProperty(SPUIDefinitions.ROLLOUT_GROUP_INSTALLED_PERCENTAGE, String.class,
                null, false, false);
        rolloutTableContainer.addContainerProperty(SPUIDefinitions.ROLLOUT_GROUP_ERROR_THRESHOLD, String.class, null,
                false, false);

        rolloutTableContainer.addContainerProperty(SPUIDefinitions.ROLLOUT_GROUP_THRESHOLD, String.class, null, false,
                false);

        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_CREATED_DATE, String.class, null, false,
                false);

        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_MODIFIED_DATE, String.class, null, false,
                false);
        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_CREATED_USER, String.class, null, false,
                false);
        rolloutTableContainer.addContainerProperty(SPUILabelDefinitions.VAR_MODIFIED_BY, String.class, null, false,
                false);

    }

    @Override
    protected String getTableId() {
        return SPUIComponetIdProvider.ROLLOUT_GROUP_LIST_TABLE_ID;
    }

    @Override
    protected void onValueChange() {
        /**
         * No implementation required.
         */
    }

    @Override
    protected void addCustomGeneratedColumns() {
        addGeneratedColumn(SPUIDefinitions.ROLLOUT_GROUP_NAME, (source, itemId, columnId) -> getRolloutNameLink(itemId));
        addGeneratedColumn(SPUIDefinitions.ROLLOUT_GROUP_STATUS, (source, itemId, columnId) -> getStatusLabel(itemId));
        addGeneratedColumn(SPUIDefinitions.DETAIL_STATUS, (source, itemId, columnId) -> getProgressBar(itemId));
        setColumnAlignment(SPUIDefinitions.ROLLOUT_GROUP_STATUS, Align.CENTER);

    }

    private Label getStatusLabel(final Object itemId) {
        final Label statusLabel = new Label();
        statusLabel.setHeightUndefined();
        statusLabel.setContentMode(ContentMode.HTML);
        setStatusIcon(itemId, statusLabel);
        statusLabel.setDescription(getDescription(itemId));
        statusLabel.setSizeUndefined();
        addPropertyChangeListener(itemId, statusLabel);
        return statusLabel;
    }

    private void addPropertyChangeListener(final Object itemId, final Label statusLabel) {
        final Property status = getContainerProperty(itemId, SPUILabelDefinitions.VAR_STATUS);
        final Property.ValueChangeNotifier notifier = (Property.ValueChangeNotifier) status;
        notifier.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final com.vaadin.data.Property.ValueChangeEvent event) {
                setStatusIcon(itemId, statusLabel);
            }
        });
    }
    private String getDescription(final Object itemId) {
        final Item item = getItem(itemId);
        if (item != null) {
            final RolloutGroupStatus rolloutGroupStatus = (RolloutGroupStatus) item.getItemProperty(
                    SPUILabelDefinitions.VAR_STATUS).getValue();
            return rolloutGroupStatus.toString().toLowerCase();
        }
        return null;
    }

    private void setStatusIcon(final Object itemId, final Label statusLabel) {
        final Item item = getItem(itemId);
        if (item != null) {
            final RolloutGroupStatus rolloutGroupStatus = (RolloutGroupStatus) item.getItemProperty(
                    SPUILabelDefinitions.VAR_STATUS).getValue();
            setRolloutStatusIcon(rolloutGroupStatus, statusLabel);
        }
    }

    private void setRolloutStatusIcon(final RolloutGroupStatus rolloutGroupStatus, final Label statusLabel) {
        switch (rolloutGroupStatus) {
        case FINISHED:
            statusLabel.setValue(FontAwesome.CHECK_CIRCLE.getHtml());
            statusLabel.setStyleName("statusIconGreen");
            break;
        case SCHEDULED:
            statusLabel.setValue(FontAwesome.BULLSEYE.getHtml());
            statusLabel.setStyleName("statusIconBlue");
            break;
        case RUNNING:
            statusLabel.setValue(FontAwesome.ADJUST.getHtml());
            statusLabel.setStyleName("statusIconYellow");
            break;
        case READY:
            statusLabel.setValue(FontAwesome.DOT_CIRCLE_O.getHtml());
            statusLabel.setStyleName("statusIconLightBlue");
            break;
        case ERROR:
            statusLabel.setValue(FontAwesome.EXCLAMATION_CIRCLE.getHtml());
            statusLabel.setStyleName("statusIconRed");
            break;
        default:
            break;
        }
        statusLabel.addStyleName(ValoTheme.LABEL_SMALL);
    }

    private Button getRolloutNameLink(final Object itemId) {
        final Item row = getItem(itemId);
        final String rolloutGroupName = (String) row.getItemProperty(SPUILabelDefinitions.VAR_NAME).getValue();
        final Button updateIcon = SPUIComponentProvider.getButton(getDetailLinkId(rolloutGroupName), rolloutGroupName,
                SPUILabelDefinitions.SHOW_ROLLOUT_GROUP_DETAILS, null, false, null, SPUIButtonStyleSmallNoBorder.class);
        updateIcon.setData(rolloutGroupName);
        updateIcon.addStyleName(ValoTheme.LINK_SMALL + " " + "on-focus-no-border link");
        updateIcon.addClickListener(event -> showRolloutGroups(itemId));
        return updateIcon;
    }

    private void showRolloutGroups(final Object itemId) {
        rolloutUIState.setRolloutGroup(rolloutManagement.findRolloutGroupById((Long) itemId));
        eventBus.publish(this, RolloutEvent.SHOW_ROLLOUT_GROUP_TARGETS);
    }

    private DistributionBar getProgressBar(final Object itemId) {
        final DistributionBar bar = new DistributionBar(2);
        bar.setSizeFull();
        bar.setZeroSizedVisible(false);
        final int i = 0;
        final RolloutTargetsStatusCount rolloutTargetsStatus = rolloutManagement
                .getRolloutGroupDetailedStatus((Long) itemId);
        return HawkbitCommonUtil.getRolloutProgressBar(bar, i, rolloutTargetsStatus);
    }

    private static String getDetailLinkId(final String rolloutGroupName) {
        return new StringBuilder(SPUIComponetIdProvider.ROLLOUT_GROUP_NAME_LINK_ID).append('.')
                .append(rolloutGroupName).toString();
    }

    @Override
    protected void setCollapsiblecolumns() {
        /**
         * No implementation required.
         */

    }

}
