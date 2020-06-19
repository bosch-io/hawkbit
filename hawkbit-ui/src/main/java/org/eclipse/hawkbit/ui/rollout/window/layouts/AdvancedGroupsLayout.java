/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditionBuilder;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.ui.common.data.mappers.AdvancedRolloutGroupDefinitionToCreateMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroupRow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorderWithIcon;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Define groups for a Rollout
 */
public class AdvancedGroupsLayout extends ValidatableLayout {
    private static final String MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED = "message.rollout.max.group.size.exceeded.advanced";

    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final RolloutManagement rolloutManagement;
    private final QuotaManagement quotaManagement;

    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    private final GridLayout layout;

    private String targetFilter;

    private final List<AdvancedGroupRow> groupRows;

    private RolloutGroupsValidation groupsValidation;
    private final AtomicInteger runningValidationsCounter;

    public AdvancedGroupsLayout(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final RolloutManagement rolloutManagement, final QuotaManagement quotaManagement,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        super();

        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.rolloutManagement = rolloutManagement;
        this.quotaManagement = quotaManagement;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;

        this.layout = buildLayout();

        this.groupRows = new ArrayList<>(10);
        this.runningValidationsCounter = new AtomicInteger(0);
    }

    private GridLayout buildLayout() {
        final GridLayout gridLayout = new GridLayout();
        gridLayout.setMargin(false);
        gridLayout.setSpacing(true);
        gridLayout.setSizeUndefined();
        gridLayout.setRows(3);
        gridLayout.setColumns(6);
        gridLayout.setStyleName("marginTop");

        gridLayout.addComponent(SPUIComponentProvider.generateLabel(i18n, "caption.rollout.group.definition.desc"), 0,
                0, 5, 0);
        addHeaderRow(gridLayout, 1);
        gridLayout.addComponent(createAddButton(), 0, 2, 5, 2);

        return gridLayout;
    }

    private void addHeaderRow(final GridLayout gridLayout, final int headerRow) {
        final List<String> headerColumns = Arrays.asList("header.name", "header.target.filter.query",
                "header.target.percentage", "header.rolloutgroup.threshold", "header.rolloutgroup.threshold.error");
        for (int i = 0; i < headerColumns.size(); i++) {
            final Label label = SPUIComponentProvider.generateLabel(i18n, headerColumns.get(i));
            gridLayout.addComponent(label, i, headerRow);
        }
    }

    private Button createAddButton() {
        final Button button = SPUIComponentProvider.getButton(UIComponentIdProvider.ROLLOUT_GROUP_ADD_ID,
                i18n.getMessage("button.rollout.add.group"), "", "", true, VaadinIcons.PLUS,
                SPUIButtonStyleNoBorderWithIcon.class);
        button.setSizeUndefined();
        button.addStyleName("default-color");
        button.setEnabled(true);
        button.setVisible(true);
        button.addClickListener(event -> addGroupRowAndValidate());
        return button;

    }

    public void addGroupRowAndValidate() {
        final int groupIndex = groupRows.size() + 1;

        addGroupRow(getDefaultAdvancedRolloutGroupDefinition(groupIndex));

        updateValidation();
    }

    private ProxyAdvancedRolloutGroupRow getDefaultAdvancedRolloutGroupDefinition(final int groupIndex) {
        final ProxyAdvancedRolloutGroupRow advancedGroupRowBean = new ProxyAdvancedRolloutGroupRow();
        advancedGroupRowBean.setGroupName(i18n.getMessage("textfield.rollout.group.default.name", groupIndex));
        advancedGroupRowBean.setTargetPercentage(100f);
        setDefaultThresholds(advancedGroupRowBean);

        return advancedGroupRowBean;
    }

    private void setDefaultThresholds(final ProxyAdvancedRolloutGroupRow advancedGroupRow) {
        final RolloutGroupConditions defaultRolloutGroupConditions = new RolloutGroupConditionBuilder().withDefaults()
                .build();
        advancedGroupRow.setTriggerThresholdPercentage(defaultRolloutGroupConditions.getSuccessConditionExp());
        advancedGroupRow.setErrorThresholdPercentage(defaultRolloutGroupConditions.getErrorConditionExp());
    }

    private void addGroupRow(final ProxyAdvancedRolloutGroupRow advancedRolloutGroupDefinition) {
        final AdvancedGroupRow groupRow = addGroupRow();
        groupRow.setBean(advancedRolloutGroupDefinition);

        groupRow.addStatusChangeListener(event -> updateValidation());
    }

    private AdvancedGroupRow addGroupRow() {
        final AdvancedGroupRow groupRow = new AdvancedGroupRow(i18n, targetFilterQueryDataProvider);

        addRowToLayout(groupRow);
        groupRows.add(groupRow);

        return groupRow;
    }

    private void addRowToLayout(final AdvancedGroupRow groupRow) {
        final int index = layout.getRows() - 1;
        layout.insertRow(index);

        groupRow.addRowToLayout(layout, index);

        layout.addComponent(createRemoveButton(groupRow, index), 5, index);
    }

    private Button createRemoveButton(final AdvancedGroupRow groupRow, final int index) {
        final Button button = SPUIComponentProvider.getButton(
                UIComponentIdProvider.ROLLOUT_GROUP_REMOVE_ID + "." + index, "", "", "", true, VaadinIcons.MINUS,
                SPUIButtonStyleNoBorderWithIcon.class);
        button.setSizeUndefined();
        button.addStyleName("default-color");

        button.addClickListener(event -> removeGroupRow(groupRow, index));

        return button;
    }

    private void removeGroupRow(final AdvancedGroupRow groupRow, final int index) {
        layout.removeRow(index);
        groupRows.remove(groupRow);

        updateValidation();
    }

    private void updateValidation() {
        validationStatus = ValidationStatus.VALID;
        if (allGroupRowsValid()) {
            setValidationStatus(ValidationStatus.LOADING);
            validateRemainingTargets();
        } else {
            setValidationStatus(ValidationStatus.INVALID);
        }
    }

    private boolean allGroupRowsValid() {
        if (groupRows.isEmpty()) {
            return false;
        }

        return groupRows.stream().allMatch(AdvancedGroupRow::isValid);
    }

    private void validateRemainingTargets() {
        resetErrors();

        if (StringUtils.isEmpty(targetFilter)) {
            return;
        }

        if (runningValidationsCounter.incrementAndGet() == 1) {
            final List<RolloutGroupCreate> groupsCreate = getRolloutGroupsCreateFromDefinitions(
                    getAdvancedRolloutGroupDefinitions());
            final ListenableFuture<RolloutGroupsValidation> validateTargetsInGroups = rolloutManagement
                    .validateTargetsInGroups(groupsCreate, targetFilter, System.currentTimeMillis());
            final UI ui = UI.getCurrent();
            validateTargetsInGroups.addCallback(validation -> ui.access(() -> setGroupsValidation(validation)),
                    throwable -> ui.access(() -> setGroupsValidation(null)));
            return;
        }

        runningValidationsCounter.incrementAndGet();
    }

    private void resetErrors() {
        groupRows.forEach(AdvancedGroupRow::resetError);
    }

    public List<ProxyAdvancedRolloutGroupRow> getAdvancedRolloutGroupDefinitions() {
        return groupRows.stream().map(AdvancedGroupRow::getBean).collect(Collectors.toList());
    }

    private List<RolloutGroupCreate> getRolloutGroupsCreateFromDefinitions(
            final List<ProxyAdvancedRolloutGroupRow> advancedRolloutGroupDefinitions) {
        final AdvancedRolloutGroupDefinitionToCreateMapper mapper = new AdvancedRolloutGroupDefinitionToCreateMapper(
                entityFactory);

        return advancedRolloutGroupDefinitions.stream().map(mapper::map).collect(Collectors.toList());
    }

    /**
     * YOU SHOULD NOT CALL THIS METHOD MANUALLY. It's only for the callback.
     * Only 1 runningValidation should be executed. If this runningValidation is
     * done, then this method is called. Maybe then a new runningValidation is
     * executed.
     * 
     */
    private void setGroupsValidation(final RolloutGroupsValidation validation) {
        final int runningValidation = runningValidationsCounter.getAndSet(0);
        if (runningValidation > 1) {
            validateRemainingTargets();
            return;
        }
        groupsValidation = validation;

        if (groupsValidation != null && groupsValidation.isValid() && validationStatus != ValidationStatus.INVALID) {
            setValidationStatus(ValidationStatus.VALID);
        } else {
            final AdvancedGroupRow lastRow = groupRows.get(groupRows.size() - 1);
            lastRow.setError(i18n.getMessage("message.rollout.remaining.targets.error"));
            setValidationStatus(ValidationStatus.INVALID);
        }

        validateSingleGroups();
    }

    private void validateSingleGroups() {
        if (groupsValidation == null || CollectionUtils.isEmpty(groupsValidation.getTargetsPerGroup())) {
            return;
        }

        final int maxTargets = quotaManagement.getMaxTargetsPerRolloutGroup();
        final boolean hasRemainingTargetsError = validationStatus == ValidationStatus.INVALID;
        final int lastIdx = groupRows.size() - 1;
        for (int i = 0; i < groupRows.size(); ++i) {
            // do not mask the 'remaining targets' error
            if (hasRemainingTargetsError && (i == lastIdx)) {
                continue;
            }

            final Long count = groupsValidation.getTargetsPerGroup().get(i);
            if (count != null && count > maxTargets) {
                final AdvancedGroupRow row = groupRows.get(i);
                row.setError(i18n.getMessage(MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED, maxTargets));
                setValidationStatus(ValidationStatus.INVALID);
            }
        }
    }

    /**
     * @param targetFilter
     *            the target filter which is required for verification
     */
    public void setTargetFilter(final String targetFilter) {
        this.targetFilter = targetFilter;

        updateValidation();
    }

    /**
     * Populate groups by rollout groups
     *
     * @param groups
     *            the rollout groups
     */
    public void populateByAdvancedRolloutGroupDefinitions(final List<ProxyAdvancedRolloutGroupRow> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }

        removeAllRows();
        groups.forEach(this::addGroupRow);

        updateValidation();
    }

    private void removeAllRows() {
        for (int i = layout.getRows() - 2; i > 1; i--) {
            layout.removeRow(i);
        }

        groupRows.clear();
    }

    /**
     * @return the validation instance if was already validated
     */
    public RolloutGroupsValidation getGroupsValidation() {
        return groupsValidation;
    }

    public GridLayout getLayout() {
        return layout;
    }
}
