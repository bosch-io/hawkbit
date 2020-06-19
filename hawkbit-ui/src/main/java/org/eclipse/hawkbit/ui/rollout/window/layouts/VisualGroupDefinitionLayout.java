/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroupRow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow.GroupDefinitionMode;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.springframework.util.CollectionUtils;

import com.vaadin.ui.GridLayout;

public class VisualGroupDefinitionLayout {

    private final GroupsPieChart groupsPieChart;
    private final GroupsLegendLayout groupsLegendLayout;

    private Long totalTargets;
    private int noOfGroups;
    private RolloutGroupsValidation validation;
    private List<ProxyAdvancedRolloutGroupRow> advancedRolloutGroupDefinitions;
    private GroupDefinitionMode groupDefinitionMode;

    public VisualGroupDefinitionLayout(final GroupsPieChart groupsPieChart,
            final GroupsLegendLayout groupsLegendLayout) {
        this.groupsPieChart = groupsPieChart;
        this.groupsLegendLayout = groupsLegendLayout;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;

        groupsLegendLayout.setTotalTargets(totalTargets);

        if (groupDefinitionMode == GroupDefinitionMode.SIMPLE) {
            updateBySimpleGroupsDefinition();
        } else {
            updateByAdvancedGroupsDefinition();
        }
    }

    private void updateBySimpleGroupsDefinition() {
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets) || noOfGroups <= 0) {
            clearGroupChartAndLegend();
            return;
        }

        final List<Long> targetsPerGroup = new ArrayList<>(noOfGroups);
        long leftTargets = totalTargets;
        for (int i = 0; i < noOfGroups; i++) {
            final double percentage = 1.0 / (noOfGroups - i);
            final long targetsInGroup = Math.round(percentage * leftTargets);
            leftTargets -= targetsInGroup;
            targetsPerGroup.add(targetsInGroup);
        }

        groupsPieChart.setChartState(targetsPerGroup, totalTargets);
        groupsLegendLayout.populateGroupsLegendByTargetCounts(targetsPerGroup);
    }

    private void clearGroupChartAndLegend() {
        groupsPieChart.setChartState(Collections.emptyList(), 0L);
        groupsLegendLayout.populateGroupsLegendByTargetCounts(Collections.emptyList());
    }

    private void updateByAdvancedGroupsDefinition() {
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets) || validation == null
                || CollectionUtils.isEmpty(validation.getTargetsPerGroup())) {
            clearGroupChartAndLegend();
            return;
        }

        groupsPieChart.setChartState(validation.getTargetsPerGroup(), totalTargets);
        groupsLegendLayout.populateGroupsLegendByValidation(validation, advancedRolloutGroupDefinitions);
    }

    public void setNoOfGroups(final int noOfGroups) {
        this.noOfGroups = noOfGroups;

        if (groupDefinitionMode == GroupDefinitionMode.SIMPLE) {
            updateBySimpleGroupsDefinition();
        }
    }

    public void setAdvancedRolloutGroupsValidation(final RolloutGroupsValidation validation,
            final List<ProxyAdvancedRolloutGroupRow> advancedRolloutGroupDefinitions) {
        this.validation = validation;
        this.advancedRolloutGroupDefinitions = advancedRolloutGroupDefinitions;

        if (groupDefinitionMode == GroupDefinitionMode.ADVANCED) {
            updateByAdvancedGroupsDefinition();
        }
    }

    public void updateByRolloutGroups(final List<ProxyAdvancedRolloutGroupRow> rolloutGroups) {
        if (!HawkbitCommonUtil.atLeastOnePresent(totalTargets) || CollectionUtils.isEmpty(rolloutGroups)) {
            clearGroupChartAndLegend();
            return;
        }

        final List<Long> targetsPerGroup = rolloutGroups.stream().map(group -> (long) group.getTotalTargets())
                .collect(Collectors.toList());

        groupsPieChart.setChartState(targetsPerGroup, totalTargets);
        groupsLegendLayout.populateGroupsLegendByGroups(rolloutGroups);
    }

    public void displayLoading() {
        groupsLegendLayout.displayLoading();
    }

    public void setGroupDefinitionMode(final GroupDefinitionMode groupDefinitionMode) {
        this.groupDefinitionMode = groupDefinitionMode;
    }

    public void addChartWithLegendToLayout(final GridLayout layout, final int lastColumnIdx, final int heightInRows) {
        layout.addComponent(groupsPieChart, lastColumnIdx - 1, 0, lastColumnIdx - 1, heightInRows);
        layout.addComponent(groupsLegendLayout, lastColumnIdx, 0, lastColumnIdx, heightInRows);
    }
}
