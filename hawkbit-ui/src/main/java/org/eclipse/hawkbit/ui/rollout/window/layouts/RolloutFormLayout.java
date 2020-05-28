/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.function.Consumer;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutForm;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class RolloutFormLayout {

    private static final String PROMPT_TARGET_FILTER = "prompt.target.filter";
    private static final String MESSAGE_ROLLOUT_FILTER_TARGET_EXISTS = "message.rollout.filter.target.exists";
    private static final String TEXTFIELD_DESCRIPTION = "textfield.description";
    private static final String PROMPT_DISTRIBUTION_SET = "prompt.distribution.set";
    private static final String TEXTFIELD_NAME = "textfield.name";
    private static final String CAPTION_ROLLOUT_START_TYPE = "caption.rollout.start.type";
    private static final String CAPTION_ROLLOUT_ACTION_TYPE = "caption.rollout.action.type";

    private static final int CAPTION_COLUMN = 0;
    private static final int FIELD_COLUMN = 1;

    private final VaadinMessageSource i18n;

    private final DistributionSetStatelessDataProvider distributionSetDataProvider;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    private final Binder<ProxyRolloutForm> binder;

    private final TextField nameField;
    private final ComboBox<ProxyDistributionSet> dsCombo;
    private final BoundComponent<ComboBox<ProxyTargetFilterQuery>> targetFilterQueryCombo;
    private final TextArea targetFilterQueryField;
    private final TextArea descriptionField;
    private final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeLayout;
    private final BoundComponent<AutoStartOptionGroupLayout> autoStartOptionGroupLayout;

    private Long totalTargets;

    private Consumer<String> filterQueryChangedListener;

    public RolloutFormLayout(final VaadinMessageSource i18n,
            final DistributionSetStatelessDataProvider distributionSetDataProvider,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        this.i18n = i18n;
        this.distributionSetDataProvider = distributionSetDataProvider;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;

        this.binder = new Binder<>();

        this.nameField = createRolloutNameField();
        this.dsCombo = createDistributionSetCombo();
        this.targetFilterQueryCombo = createTargetFilterQueryCombo();
        this.targetFilterQueryField = createTargetFilterQuery();
        this.descriptionField = createDescription();
        this.actionTypeLayout = createActionTypeOptionGroupLayout();
        this.autoStartOptionGroupLayout = createAutoStartOptionGroupLayout();

        addValueChangeListeners();
    }

    /**
     * Create name field.
     * 
     * @return input component
     */
    private TextField createRolloutNameField() {
        final TextField textField = FormComponentBuilder
                .createNameInput(binder, i18n, UIComponentIdProvider.ROLLOUT_NAME_FIELD_ID).getComponent();
        textField.setCaption(null);
        return textField;
    }

    /**
     * Create required Distribution Set ComboBox.
     * 
     * @return ComboBox
     */
    private ComboBox<ProxyDistributionSet> createDistributionSetCombo() {
        final ComboBox<ProxyDistributionSet> comboBox = FormComponentBuilder.createDistributionSetComboBox(binder,
                distributionSetDataProvider, i18n, UIComponentIdProvider.ROLLOUT_DS_ID).getComponent();
        comboBox.setCaption(null);
        return comboBox;
    }

    private BoundComponent<ComboBox<ProxyTargetFilterQuery>> createTargetFilterQueryCombo() {
        final ComboBox<ProxyTargetFilterQuery> tfqCombo = new ComboBox<>();

        tfqCombo.setId(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_COMBO_ID);
        tfqCombo.setPlaceholder(i18n.getMessage(PROMPT_TARGET_FILTER));
        tfqCombo.addStyleName(ValoTheme.COMBOBOX_SMALL);

        tfqCombo.setItemCaptionGenerator(ProxyTargetFilterQuery::getName);
        tfqCombo.setDataProvider(targetFilterQueryDataProvider);

        // TODO: use i18n for all the required fields messages
        final Binding<ProxyRolloutForm, Long> binding = binder.forField(tfqCombo)
                .asRequired("You must provide the target filter").withValidator((filterQuery, context) -> {
                    // TODO: workaround for the value set with setBean (see copy
                    // layout)
                    if (StringUtils.isEmpty(filterQuery.getQuery())) {
                        return ValidationResult.ok();
                    }

                    return new LongRangeValidator(i18n.getMessage(MESSAGE_ROLLOUT_FILTER_TARGET_EXISTS), 1L, null)
                            .apply(totalTargets, context);
                }).withConverter(filter -> {
                    if (filter == null) {
                        return null;
                    }

                    return filter.getId();
                }, filterId -> {
                    if (filterId == null) {
                        return null;
                    }

                    final ProxyTargetFilterQuery filter = new ProxyTargetFilterQuery();
                    filter.setId(filterId);

                    return filter;
                }).bind(ProxyRolloutForm::getTargetFilterId, ProxyRolloutForm::setTargetFilterId);

        return new BoundComponent<>(tfqCombo, binding);
    }

    private TextArea createTargetFilterQuery() {
        final TextArea targetFilterQuery = new TextAreaBuilder(TargetFilterQuery.QUERY_MAX_SIZE)
                .style("text-area-style").id(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_QUERY_FIELD)
                .buildTextComponent();
        targetFilterQuery.setSizeUndefined();

        binder.forField(targetFilterQuery).bind(ProxyRolloutForm::getTargetFilterQuery,
                ProxyRolloutForm::setTargetFilterQuery);

        return targetFilterQuery;
    }

    /**
     * Create description field.
     * 
     * @return input component
     */
    private TextArea createDescription() {
        final TextArea description = FormComponentBuilder
                .createDescriptionInput(binder, i18n, UIComponentIdProvider.ROLLOUT_DESCRIPTION_ID).getComponent();
        description.setCaption(null);

        return description;
    }

    /**
     * Create bound {@link ActionTypeOptionGroupAssignmentLayout}.
     * 
     * @return input component
     */
    private BoundComponent<ActionTypeOptionGroupAssignmentLayout> createActionTypeOptionGroupLayout() {
        final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeGroupBounded = FormComponentBuilder
                .createActionTypeOptionGroupLayout(binder, i18n, UIComponentIdProvider.ROLLOUT_ACTION_TYPE_OPTIONS_ID);
        actionTypeGroupBounded.setRequired(false);

        return actionTypeGroupBounded;
    }

    private BoundComponent<AutoStartOptionGroupLayout> createAutoStartOptionGroupLayout() {
        final AutoStartOptionGroupLayout autoStartOptionGroup = new AutoStartOptionGroupLayout(i18n);
        autoStartOptionGroup.addStyleName(SPUIStyleDefinitions.ROLLOUT_ACTION_TYPE_LAYOUT);

        binder.forField(autoStartOptionGroup.getAutoStartOptionGroup()).bind(ProxyRolloutForm::getAutoStartOption,
                ProxyRolloutForm::setAutoStartOption);

        // TODO: use i18n
        final Binding<ProxyRolloutForm, Long> binding = binder.forField(autoStartOptionGroup.getStartAtDateField())
                .asRequired("Scheduled time can not be empty").withConverter(localDateTime -> {
                    if (localDateTime == null) {
                        return null;
                    }

                    return SPDateTimeUtil.localDateTimeToEpochMilli(localDateTime);
                }, startAtTime -> {
                    if (startAtTime == null) {
                        return null;
                    }

                    return SPDateTimeUtil.epochMilliToLocalDateTime(startAtTime);
                }).bind(ProxyRolloutForm::getStartAt, ProxyRolloutForm::setStartAt);

        return new BoundComponent<>(autoStartOptionGroup, binding);
    }

    private void addValueChangeListeners() {
        targetFilterQueryCombo.getComponent().addValueChangeListener(event -> {
            // we do not want to call the value change listener while setting
            // the bean via binder.setBean()
            if (!event.isUserOriginated()) {
                return;
            }

            if (filterQueryChangedListener != null) {
                filterQueryChangedListener.accept(event.getValue() != null ? event.getValue().getQuery() : null);
            }
        });

        actionTypeLayout.getComponent().getActionTypeOptionGroup().addValueChangeListener(
                event -> actionTypeLayout.setRequired(event.getValue() == ActionType.TIMEFORCED));
        autoStartOptionGroupLayout.getComponent().getAutoStartOptionGroup().addValueChangeListener(
                event -> autoStartOptionGroupLayout.setRequired(event.getValue() == AutoStartOption.SCHEDULED));
    }

    public void addRowToLayout(final GridLayout layout, final boolean isEditMode) {
        layout.addComponent(getLabel(TEXTFIELD_NAME), CAPTION_COLUMN, 0);
        layout.addComponent(nameField, FIELD_COLUMN, 0);
        nameField.focus();

        layout.addComponent(getLabel(PROMPT_DISTRIBUTION_SET), CAPTION_COLUMN, 1);
        layout.addComponent(dsCombo, FIELD_COLUMN, 1);

        layout.addComponent(getLabel(PROMPT_TARGET_FILTER), CAPTION_COLUMN, 2);
        layout.addComponent(isEditMode ? targetFilterQueryField : targetFilterQueryCombo.getComponent(), FIELD_COLUMN,
                2);

        layout.addComponent(getLabel(TEXTFIELD_DESCRIPTION), CAPTION_COLUMN, 3);
        layout.addComponent(descriptionField, FIELD_COLUMN, 3);

        final int lastColumn = layout.getColumns() - 1;
        layout.addComponent(getLabel(CAPTION_ROLLOUT_ACTION_TYPE), CAPTION_COLUMN, 4);
        layout.addComponent(actionTypeLayout.getComponent(), FIELD_COLUMN, 4, lastColumn, 4);

        layout.addComponent(getLabel(CAPTION_ROLLOUT_START_TYPE), CAPTION_COLUMN, 5);
        layout.addComponent(autoStartOptionGroupLayout.getComponent(), FIELD_COLUMN, 5, lastColumn, 5);
    }

    public void disableFieldsOnEdit() {
        dsCombo.setEnabled(false);
        targetFilterQueryField.setEnabled(false);
        actionTypeLayout.getComponent().setEnabled(false);
        autoStartOptionGroupLayout.getComponent().setEnabled(false);
    }

    private Label getLabel(final String key) {
        return new LabelBuilder().name(i18n.getMessage(key)).buildLabel();
    }

    public void setFilterQueryChangedListener(final Consumer<String> filterQueryChangedListener) {
        this.filterQueryChangedListener = filterQueryChangedListener;
    }

    public void setTotalTargets(final Long totalTargets) {
        this.totalTargets = totalTargets;

        targetFilterQueryCombo.validate();
    }
}
