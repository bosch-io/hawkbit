/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.hawkbit.ui.common.entity.DistributionSetIdName;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionHistoryGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.management.state.DistributionTableFilters;
import org.eclipse.hawkbit.ui.management.state.TargetTableFilters;
import org.eclipse.hawkbit.ui.management.targettable.TargetBulkUploadUiState;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * User action on management UI.
 */
@VaadinSessionScope
@SpringComponent
public class ManagementUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final TargetBulkUploadUiState targetBulkUploadUiState;
    private final DistributionGridLayoutUiState distributionGridLayoutUiState;
    private final DistributionTagLayoutUiState distributionTagLayoutUiState;
    private final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState;

    private final DistributionTableFilters distributionTableFilters = new DistributionTableFilters();

    private final TargetTableFilters targetTableFilters = new TargetTableFilters();

    private final Set<DistributionSetIdName> deletedDistributionList = new HashSet<>();

    private final Set<TargetIdName> deletedTargetList = new HashSet<>();

    private Boolean targetTagLayoutVisible = Boolean.TRUE;

    private Boolean distTagLayoutVisible = Boolean.FALSE;

    private transient Optional<Long> lastSelectedTargetId = Optional.empty();

    private Set<Long> selectedTargetId = Collections.emptySet();

    private boolean targetTagFilterClosed;

    private boolean distTagFilterClosed = true;

    private Long targetsTruncated;

    private final AtomicLong targetsCountAll = new AtomicLong();

    private boolean dsTableMaximized;

    private transient Optional<Long> lastSelectedDsIdName = Optional.empty();

    private Set<Long> selectedDsIdName = Collections.emptySet();

    private boolean targetTableMaximized;

    private boolean actionHistoryMaximized;

    private boolean noDataAvilableTarget;

    private boolean noDataAvailableDistribution;

    private final Set<String> canceledTargetName = new HashSet<>();

    private boolean customFilterSelected;

    private boolean bulkUploadWindowMinimised;

    private transient Optional<Long> lastSelectedActionId = Optional.empty();

    private transient Optional<Long> lastSelectedActionStatusId = Optional.empty();

    ManagementUIState() {
        this.targetTagFilterLayoutUiState = new TargetTagFilterLayoutUiState();
        this.targetGridLayoutUiState = new TargetGridLayoutUiState();
        this.targetBulkUploadUiState = new TargetBulkUploadUiState();
        this.distributionGridLayoutUiState = new DistributionGridLayoutUiState();
        this.distributionTagLayoutUiState = new DistributionTagLayoutUiState();
        this.actionHistoryGridLayoutUiState = new ActionHistoryGridLayoutUiState();
    }

    public TargetTagFilterLayoutUiState getTargetTagFilterLayoutUiState() {
        return targetTagFilterLayoutUiState;
    }

    public TargetGridLayoutUiState getTargetGridLayoutUiState() {
        return targetGridLayoutUiState;
    }

    public DistributionGridLayoutUiState getDistributionGridLayoutUiState() {
        return distributionGridLayoutUiState;
    }

    public DistributionTagLayoutUiState getDistributionTagLayoutUiState() {
        return distributionTagLayoutUiState;
    }

    public ActionHistoryGridLayoutUiState getActionHistoryGridLayoutUiState() {
        return actionHistoryGridLayoutUiState;
    }

    public TargetBulkUploadUiState getTargetBulkUploadUiState() {
        return targetBulkUploadUiState;
    }

    public boolean isBulkUploadWindowMinimised() {
        return bulkUploadWindowMinimised;
    }

    public void setBulkUploadWindowMinimised(final boolean bulkUploadWindowMinimised) {
        this.bulkUploadWindowMinimised = bulkUploadWindowMinimised;
    }

    public boolean isCustomFilterSelected() {
        return customFilterSelected;
    }

    public void setCustomFilterSelected(final boolean isCustomFilterSelected) {
        customFilterSelected = isCustomFilterSelected;
    }

    public Set<String> getCanceledTargetName() {
        return canceledTargetName;
    }

    public void setDistTagLayoutVisible(final Boolean distTagLayoutVisible) {
        this.distTagLayoutVisible = distTagLayoutVisible;
    }

    public Boolean getDistTagLayoutVisible() {
        return distTagLayoutVisible;
    }

    public void setTargetTagLayoutVisible(final Boolean targetTagVisible) {
        targetTagLayoutVisible = targetTagVisible;
    }

    public Boolean getTargetTagLayoutVisible() {
        return targetTagLayoutVisible;
    }

    public TargetTableFilters getTargetTableFilters() {
        return targetTableFilters;
    }

    public DistributionTableFilters getDistributionTableFilters() {
        return distributionTableFilters;
    }

    public Set<DistributionSetIdName> getDeletedDistributionList() {
        return deletedDistributionList;
    }

    public Set<TargetIdName> getDeletedTargetList() {
        return deletedTargetList;
    }

    public Optional<Long> getLastSelectedTargetId() {
        return lastSelectedTargetId;
    }

    public void setLastSelectedTargetId(final Long lastSelectedTargetId) {
        this.lastSelectedTargetId = Optional.ofNullable(lastSelectedTargetId);
    }

    public Set<Long> getSelectedTargetId() {
        return selectedTargetId;
    }

    public void setSelectedTargetId(final Set<Long> selectedTargetId) {
        this.selectedTargetId = selectedTargetId;
    }

    public boolean isTargetTagFilterClosed() {
        return targetTagFilterClosed;
    }

    public void setTargetTagFilterClosed(final boolean targetTagFilterClosed) {
        this.targetTagFilterClosed = targetTagFilterClosed;
    }

    public boolean isDistTagFilterClosed() {
        return distTagFilterClosed;
    }

    public void setDistTagFilterClosed(final boolean distTagFilterClosed) {
        this.distTagFilterClosed = distTagFilterClosed;
    }

    public Long getTargetsTruncated() {
        return targetsTruncated;
    }

    public void setTargetsTruncated(final Long targetsTruncated) {
        this.targetsTruncated = targetsTruncated;
    }

    public long getTargetsCountAll() {
        return targetsCountAll.get();
    }

    public void setTargetsCountAll(final long targetsCountAll) {
        this.targetsCountAll.set(targetsCountAll);
    }

    public boolean isDsTableMaximized() {
        return dsTableMaximized;
    }

    public void setDsTableMaximized(final boolean isDsTableMaximized) {
        this.dsTableMaximized = isDsTableMaximized;
    }

    public Optional<Long> getLastSelectedDsIdName() {
        return lastSelectedDsIdName;
    }

    public void setLastSelectedEntityId(final Long value) {
        this.lastSelectedDsIdName = Optional.ofNullable(value);
    }

    public void setSelectedEnitities(final Set<Long> values) {
        this.selectedDsIdName = values;
    }

    public Set<Long> getSelectedDsIdName() {
        return selectedDsIdName;
    }

    public boolean isTargetTableMaximized() {
        return targetTableMaximized;
    }

    public void setTargetTableMaximized(final boolean isTargetTableMaximized) {
        this.targetTableMaximized = isTargetTableMaximized;
    }

    public boolean isActionHistoryMaximized() {
        return actionHistoryMaximized;
    }

    public void setActionHistoryMaximized(final boolean isActionHistoryMaximized) {
        this.actionHistoryMaximized = isActionHistoryMaximized;
    }

    public boolean isNoDataAvilableTarget() {
        return noDataAvilableTarget;
    }

    public void setNoDataAvailableTarget(final boolean noDataAvilableTarget) {
        this.noDataAvilableTarget = noDataAvilableTarget;
    }

    public boolean isNoDataAvailableDistribution() {
        return noDataAvailableDistribution;
    }

    public void setNoDataAvailableDistribution(final boolean noDataAvailableDistribution) {
        this.noDataAvailableDistribution = noDataAvailableDistribution;
    }

    public Optional<Long> getLastSelectedActionId() {
        return lastSelectedActionId;
    }

    public void setLastSelectedActionId(final Long lastSelectedActionId) {
        this.lastSelectedActionId = Optional.ofNullable(lastSelectedActionId);
    }

    public Optional<Long> getLastSelectedActionStatusId() {
        return lastSelectedActionStatusId;
    }

    public void setLastSelectedActionStatusId(final Long lastSelectedActionStatusId) {
        this.lastSelectedActionStatusId = Optional.ofNullable(lastSelectedActionStatusId);
    }
}