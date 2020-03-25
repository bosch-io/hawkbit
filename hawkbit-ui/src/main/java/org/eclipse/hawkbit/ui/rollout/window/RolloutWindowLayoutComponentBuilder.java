/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditionBuilder;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.rollout.groupschart.GroupsPieChart;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.DefineGroupsLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.GroupsLegendLayout;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder for Rollout window components.
 */
public final class RolloutWindowLayoutComponentBuilder {

    public static final String CAPTION_ROLLOUT_START_TYPE = "caption.rollout.start.type";
    public static final String CAPTION_ROLLOUT_ACTION_TYPE = "caption.rollout.action.type";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";
    public static final String PROMPT_TARGET_FILTER = "prompt.target.filter";
    public static final String PROMPT_DISTRIBUTION_SET = "prompt.distribution.set";
    public static final String TEXTFIELD_NAME = "textfield.name";

    private static final String MESSAGE_ENTER_NUMBER = "message.enter.number";
    private static final String APPROVAL_BUTTON_LABEL = "button.approve";
    private static final String DENY_BUTTON_LABEL = "button.deny";

    private static final String MESSAGE_ROLLOUT_FIELD_VALUE_RANGE = "message.rollout.field.value.range";
    private static final String MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED = "message.rollout.max.group.size.exceeded";
    private static final String MESSAGE_ROLLOUT_FILTER_TARGET_EXISTS = "message.rollout.filter.target.exists";

    private final RolloutWindowDependencies dependencies;

    private static final RolloutGroupConditions defaultRolloutGroupConditions;

    private final DistributionSetStatelessDataProvider distributionSetDataProvider;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    static {
        defaultRolloutGroupConditions = new RolloutGroupConditionBuilder().withDefaults().build();
    }

    public RolloutWindowLayoutComponentBuilder(final RolloutWindowDependencies rolloutWindowDependecies) {
        this.dependencies = rolloutWindowDependecies;

        this.distributionSetDataProvider = new DistributionSetStatelessDataProvider(
                dependencies.getDistributionSetManagement(), new DistributionSetToProxyDistributionMapper());
        this.targetFilterQueryDataProvider = new TargetFilterQueryDataProvider(
                dependencies.getTargetFilterQueryManagement(), new TargetFilterQueryToProxyTargetFilterMapper());
    }

    /**
     * create name field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createRolloutNameField(final Binder<ProxyRolloutWindow> binder) {
        final TextField textField = FormComponentBuilder
                .createNameInput(binder, dependencies.getI18n(), UIComponentIdProvider.ROLLOUT_NAME_FIELD_ID)
                .getComponent();
        textField.setCaption(null);
        return textField;
    }

    /**
     * create required Distribution Set ComboBox
     * 
     * @param binder
     *            binder the input will be bound to
     * @return ComboBox
     */
    public ComboBox<ProxyDistributionSet> createDistributionSetCombo(final Binder<ProxyRolloutWindow> binder) {
        final ComboBox<ProxyDistributionSet> comboBox = FormComponentBuilder.createDistributionSetComboBox(binder,
                distributionSetDataProvider, dependencies.getI18n(), UIComponentIdProvider.ROLLOUT_DS_ID)
                .getComponent();
        comboBox.setCaption(null);
        return comboBox;
    }

    public ComboBox<ProxyTargetFilterQuery> createTargetFilterQueryCombo(final Binder<ProxyRolloutWindow> binder,
            final ToLongFunction<String> totalTargetsByQueryCallback) {
        final ComboBox<ProxyTargetFilterQuery> targetFilterQueryCombo = new ComboBox<>();

        targetFilterQueryCombo.setId(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_COMBO_ID);
        targetFilterQueryCombo.setPlaceholder(dependencies.getI18n().getMessage(PROMPT_TARGET_FILTER));
        targetFilterQueryCombo.addStyleName(ValoTheme.COMBOBOX_SMALL);

        targetFilterQueryCombo.setItemCaptionGenerator(ProxyTargetFilterQuery::getName);
        targetFilterQueryCombo.setDataProvider(targetFilterQueryDataProvider);

        // TODO: use i18n for all the required fields messages
        binder.forField(targetFilterQueryCombo).asRequired((filterQuery, context) -> {
            if (filterQuery == null) {
                totalTargetsByQueryCallback.applyAsLong(null);

                return ValidationResult.error("You must provide the target filter");
            }

            return ValidationResult.ok();
        }).withValidator((filterQuery, context) -> {
            // TODO: workaround for the value set with setBean (see copy
            // layout)
            if (StringUtils.isEmpty(filterQuery.getQuery())) {
                return ValidationResult.ok();
            }

            final long targetsCountByFilter = totalTargetsByQueryCallback.applyAsLong(filterQuery.getQuery());

            return new LongRangeValidator(dependencies.getI18n().getMessage(MESSAGE_ROLLOUT_FILTER_TARGET_EXISTS), 1L,
                    null).apply(targetsCountByFilter, context);
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
        }).bind(ProxyRolloutWindow::getTargetFilterId, ProxyRolloutWindow::setTargetFilterId);

        return targetFilterQueryCombo;
    }

    public TextArea createTargetFilterQuery(final Binder<ProxyRolloutWindow> binder) {
        final TextArea targetFilterQuery = new TextAreaBuilder(TargetFilterQuery.QUERY_MAX_SIZE)
                .style("text-area-style").id(UIComponentIdProvider.ROLLOUT_TARGET_FILTER_QUERY_FIELD)
                .buildTextComponent();
        targetFilterQuery.setEnabled(false);
        targetFilterQuery.setSizeUndefined();

        binder.forField(targetFilterQuery).bind(ProxyRolloutWindow::getTargetFilterQuery,
                ProxyRolloutWindow::setTargetFilterQuery);

        return targetFilterQuery;
    }

    /**
     * create description field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextArea createDescription(final Binder<ProxyRolloutWindow> binder) {
        return FormComponentBuilder
                .createDescriptionInput(binder, dependencies.getI18n(), UIComponentIdProvider.ROLLOUT_DESCRIPTION_ID)
                .getComponent();
    }

    /**
     * create bound {@link ActionTypeOptionGroupAssignmentLayout}
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public BoundComponent<ActionTypeOptionGroupAssignmentLayout> createActionTypeOptionGroupLayout(
            final Binder<ProxyRolloutWindow> binder) {
        final BoundComponent<ActionTypeOptionGroupAssignmentLayout> actionTypeGroupBounded = FormComponentBuilder
                .createActionTypeOptionGroupLayout(binder, dependencies.getI18n(),
                        UIComponentIdProvider.ROLLOUT_ACTION_TYPE_OPTIONS_ID);
        actionTypeGroupBounded.setRequired(false);

        return actionTypeGroupBounded;
    }

    public BoundComponent<AutoStartOptionGroupLayout> createAutoStartOptionGroupLayout(
            final Binder<ProxyRolloutWindow> binder) {
        final AutoStartOptionGroupLayout autoStartOptionGroupLayout = new AutoStartOptionGroupLayout(
                dependencies.getI18n());
        autoStartOptionGroupLayout.addStyleName(SPUIStyleDefinitions.ROLLOUT_ACTION_TYPE_LAYOUT);

        binder.forField(autoStartOptionGroupLayout.getAutoStartOptionGroup())
                .bind(ProxyRolloutWindow::getAutoStartOption, ProxyRolloutWindow::setAutoStartOption);

        // TODO: use i18n
        final Binding<ProxyRolloutWindow, Long> binding = binder
                .forField(autoStartOptionGroupLayout.getStartAtDateField())
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
                }).bind(ProxyRolloutWindow::getStartAt, ProxyRolloutWindow::setStartAt);

        return new BoundComponent<>(autoStartOptionGroupLayout, binding);
    }

    public TabSheet createGroupDefinitionTabs(final Component simpleGroupDefinitionTab,
            final Component advancedGroupDefinitionTab) {
        final TabSheet groupsDefinitionTabs = new TabSheet();
        groupsDefinitionTabs.setId(UIComponentIdProvider.ROLLOUT_GROUPS);
        groupsDefinitionTabs.setWidth(850, Unit.PIXELS);
        groupsDefinitionTabs.setHeight(300, Unit.PIXELS);
        groupsDefinitionTabs.setStyleName(SPUIStyleDefinitions.ROLLOUT_GROUPS);

        groupsDefinitionTabs
                .addTab(simpleGroupDefinitionTab, dependencies.getI18n().getMessage("caption.rollout.tabs.simple"))
                .setId(UIComponentIdProvider.ROLLOUT_SIMPLE_TAB);

        groupsDefinitionTabs
                .addTab(advancedGroupDefinitionTab, dependencies.getI18n().getMessage("caption.rollout.tabs.advanced"))
                .setId(UIComponentIdProvider.ROLLOUT_ADVANCED_TAB);

        return groupsDefinitionTabs;
    }

    public GridLayout createSimpleGroupDefinitionTab(final Component noOfGroups, final Component groupSizeLabel,
            final Component errorThreshold, final Component errorThresholdOptionGroup,
            final Binder<ProxyRolloutWindow> binder) {
        final GridLayout layout = new GridLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setColumns(3);
        layout.setRows(4);
        layout.setStyleName("marginTop");

        layout.addComponent(getLabel("caption.rollout.generate.groups"), 0, 0, 2, 0);

        layout.addComponent(getLabel("prompt.number.of.groups"), 0, 1);
        layout.addComponent(noOfGroups, 1, 1);
        layout.addComponent(groupSizeLabel, 2, 1);

        layout.addComponent(getLabel("prompt.tigger.threshold"), 0, 2);
        layout.addComponent(createTriggerThreshold(binder), 1, 2);
        layout.addComponent(getPercentHintLabel(), 2, 2);

        layout.addComponent(getLabel("prompt.error.threshold"), 0, 3);
        layout.addComponent(errorThreshold, 1, 3);
        layout.addComponent(errorThresholdOptionGroup, 2, 3);

        return layout;
    }

    public Label getLabel(final String key) {
        return new LabelBuilder().name(dependencies.getI18n().getMessage(key)).buildLabel();
    }

    public Entry<TextField, Binding<ProxyRolloutWindow, Integer>> createNoOfGroupsField(
            final Binder<ProxyRolloutWindow> binder, final ToIntFunction<Integer> groupSizeByGroupNumberProvider) {
        final TextField noOfGroups = new TextFieldBuilder(3).id(UIComponentIdProvider.ROLLOUT_NO_OF_GROUPS_ID)
                .prompt(dependencies.getI18n().getMessage("prompt.number.of.groups")).buildTextComponent();
        noOfGroups.setSizeUndefined();

        final Binding<ProxyRolloutWindow, Integer> noOfGroupsFieldBinding = binder.forField(noOfGroups)
                .asRequired("You must specify at least one group").withNullRepresentation("")
                .withConverter(new StringToIntegerConverter(dependencies.getI18n().getMessage(MESSAGE_ENTER_NUMBER)))
                .withValidator((number, context) -> {
                    final int maxGroups = dependencies.getQuotaManagement().getMaxRolloutGroupsPerRollout();
                    return new IntegerRangeValidator(
                            dependencies.getI18n().getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 1, maxGroups), 1,
                            maxGroups).apply(number, context);
                }).withValidator((number, context) -> {
                    final int maxGroupSize = dependencies.getQuotaManagement().getMaxTargetsPerRolloutGroup();
                    if (groupSizeByGroupNumberProvider.applyAsInt(number) > maxGroupSize) {
                        final String msg = dependencies.getI18n().getMessage(MESSAGE_ROLLOUT_MAX_GROUP_SIZE_EXCEEDED,
                                maxGroupSize);
                        return ValidationResult.error(msg);
                    } else {
                        return ValidationResult.ok();
                    }
                }).bind(ProxyRolloutWindow::getNumberOfGroups, ProxyRolloutWindow::setNumberOfGroups);

        return new AbstractMap.SimpleImmutableEntry<>(noOfGroups, noOfGroupsFieldBinding);
    }

    public Label createCountLabel() {
        final Label groupSizeLabel = new LabelBuilder().visible(false).name("").buildLabel();
        groupSizeLabel.addStyleName(ValoTheme.LABEL_TINY + " " + "rollout-target-count-message");
        groupSizeLabel.setSizeUndefined();

        return groupSizeLabel;
    }

    private TextField createTriggerThreshold(final Binder<ProxyRolloutWindow> binder) {
        final TextField triggerThreshold = new TextFieldBuilder(3).id(UIComponentIdProvider.ROLLOUT_TRIGGER_THRESOLD_ID)
                .prompt(dependencies.getI18n().getMessage("prompt.tigger.threshold")).buildTextComponent();
        triggerThreshold.setSizeUndefined();

        // TODO: use i18n
        binder.forField(triggerThreshold).asRequired("Trigger threshold can not be empty")
                .withValidator((triggerThresholdText,
                        context) -> new IntegerRangeValidator(
                                dependencies.getI18n().getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 0, 100), 0, 100)
                                        .apply(Integer.valueOf(triggerThresholdText), context))
                .bind(ProxyRolloutWindow::getTriggerThresholdPercentage,
                        ProxyRolloutWindow::setTriggerThresholdPercentage);

        return triggerThreshold;
    }

    private Label getPercentHintLabel() {
        final Label percentSymbol = new Label("%");
        percentSymbol.addStyleName(ValoTheme.LABEL_TINY + " " + ValoTheme.LABEL_BOLD);
        percentSymbol.setSizeUndefined();

        return percentSymbol;
    }

    public TextField createErrorThreshold(final Binder<ProxyRolloutWindow> binder,
            final Supplier<ERROR_THRESHOLD_OPTIONS> errorThresholdOptionProvider, final IntSupplier groupSizeProvider) {
        final TextField errorThreshold = new TextFieldBuilder(3).id(UIComponentIdProvider.ROLLOUT_ERROR_THRESOLD_ID)
                .prompt(dependencies.getI18n().getMessage("prompt.error.threshold")).buildTextComponent();
        errorThreshold.setSizeUndefined();

        // TODO: use i18n
        binder.forField(errorThreshold).asRequired("Error threshold can not be empty")
                .withValidator((errorThresholdText, context) -> {
                    if (ERROR_THRESHOLD_OPTIONS.PERCENT == errorThresholdOptionProvider.get()) {
                        return new IntegerRangeValidator(
                                dependencies.getI18n().getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 0, 100), 0, 100)
                                        .apply(Integer.valueOf(errorThresholdText), context);
                    }

                    final int groupSize = groupSizeProvider.getAsInt();

                    if (groupSize == 0) {
                        final String msg = dependencies.getI18n()
                                .getMessage("message.rollout.noofgroups.or.targetfilter.missing");

                        return ValidationResult.error(msg);
                    } else {
                        return new IntegerRangeValidator(
                                dependencies.getI18n().getMessage(MESSAGE_ROLLOUT_FIELD_VALUE_RANGE, 0, groupSize), 0,
                                groupSize).apply(Integer.valueOf(errorThresholdText), context);
                    }
                }).withConverter(errorThresholdPresentation -> {
                    if (errorThresholdPresentation == null) {
                        return null;
                    }

                    if (ERROR_THRESHOLD_OPTIONS.COUNT == errorThresholdOptionProvider.get()) {
                        final int errorThresholdCount = Integer.parseInt(errorThresholdPresentation);
                        final int groupSize = groupSizeProvider.getAsInt();

                        return String.valueOf((int) Math.ceil(((float) errorThresholdCount / (float) groupSize) * 100));
                    }

                    return errorThresholdPresentation;
                }, errorThresholdModel -> {
                    if (errorThresholdModel == null) {
                        return null;
                    }

                    return errorThresholdModel;
                })
                .bind(ProxyRolloutWindow::getErrorThresholdPercentage, ProxyRolloutWindow::setErrorThresholdPercentage);

        return errorThreshold;
    }

    public RadioButtonGroup<ERROR_THRESHOLD_OPTIONS> createErrorThresholdOptionGroup() {
        final RadioButtonGroup<ERROR_THRESHOLD_OPTIONS> errorThresholdOptionGroup = new RadioButtonGroup<>();
        errorThresholdOptionGroup.setItems(ERROR_THRESHOLD_OPTIONS.values());
        errorThresholdOptionGroup.setValue(ERROR_THRESHOLD_OPTIONS.PERCENT);
        errorThresholdOptionGroup.setId(UIComponentIdProvider.ROLLOUT_ERROR_THRESOLD_OPTION_ID);
        errorThresholdOptionGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        errorThresholdOptionGroup.addStyleName(SPUIStyleDefinitions.ROLLOUT_OPTION_GROUP);
        errorThresholdOptionGroup.setSizeUndefined();

        errorThresholdOptionGroup
                .setItemCaptionGenerator(errorThresholdOption -> errorThresholdOption.getValue(dependencies.getI18n()));

        return errorThresholdOptionGroup;
    }

    public DefineGroupsLayout createAdvancedGroupDefinitionTab() {
        final DefineGroupsLayout defineGroupsLayout = new DefineGroupsLayout(dependencies.getI18n(),
                dependencies.getEntityFactory(), dependencies.getRolloutManagement(),
                dependencies.getTargetFilterQueryManagement(), dependencies.getRolloutGroupManagement(),
                dependencies.getQuotaManagement(), targetFilterQueryDataProvider);
        defineGroupsLayout.setDefaultErrorThreshold(defaultRolloutGroupConditions.getErrorConditionExp());
        defineGroupsLayout.setDefaultTriggerThreshold(defaultRolloutGroupConditions.getSuccessConditionExp());

        return defineGroupsLayout;
    }

    public HorizontalLayout createApprovalLayout(final Binder<ProxyRolloutWindow> binder) {
        final RadioButtonGroup<Rollout.ApprovalDecision> approveButtonsGroup = new RadioButtonGroup<>();
        approveButtonsGroup.setId(UIComponentIdProvider.ROLLOUT_APPROVAL_OPTIONGROUP_ID);
        approveButtonsGroup.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        approveButtonsGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        approveButtonsGroup.addStyleName("custom-option-group");
        approveButtonsGroup.setItems(Rollout.ApprovalDecision.values());

        approveButtonsGroup.setItemCaptionGenerator(item -> {
            if (Rollout.ApprovalDecision.APPROVED == item) {
                return dependencies.getI18n().getMessage(APPROVAL_BUTTON_LABEL);
            } else {
                return dependencies.getI18n().getMessage(DENY_BUTTON_LABEL);
            }
        });
        approveButtonsGroup.setItemIconGenerator(item -> {
            if (Rollout.ApprovalDecision.APPROVED == item) {
                return VaadinIcons.CHECK;
            } else {
                return VaadinIcons.CLOSE;
            }
        });

        binder.forField(approveButtonsGroup).bind(ProxyRolloutWindow::getApprovalDecision,
                ProxyRolloutWindow::setApprovalDecision);

        final TextField approvalRemarkField = new TextFieldBuilder(Rollout.APPROVAL_REMARK_MAX_SIZE)
                .id(UIComponentIdProvider.ROLLOUT_APPROVAL_REMARK_FIELD_ID)
                .prompt(dependencies.getI18n().getMessage("label.approval.remark")).buildTextComponent();
        approvalRemarkField.setWidthFull();

        binder.forField(approvalRemarkField).bind(ProxyRolloutWindow::getApprovalRemark,
                ProxyRolloutWindow::setApprovalRemark);

        final HorizontalLayout approvalButtonsLayout = new HorizontalLayout(approveButtonsGroup, approvalRemarkField);
        approvalButtonsLayout.setWidthFull();
        approvalButtonsLayout.setExpandRatio(approvalRemarkField, 1.0F);

        return approvalButtonsLayout;
    }

    public GroupsPieChart createGroupsPieChart() {
        final GroupsPieChart groupsPieChart = new GroupsPieChart();
        groupsPieChart.setWidth(260, Unit.PIXELS);
        groupsPieChart.setHeight(220, Unit.PIXELS);
        groupsPieChart.setStyleName(SPUIStyleDefinitions.ROLLOUT_GROUPS_CHART);

        return groupsPieChart;
    }

    public GroupsLegendLayout createGroupsLegendLayout() {
        return new GroupsLegendLayout(dependencies.getI18n());
    }

    public static RolloutGroupConditions getDefaultRolloutGroupConditions() {
        return defaultRolloutGroupConditions;
    }

    public enum ERROR_THRESHOLD_OPTIONS {

        PERCENT("label.errorthreshold.option.percent"), COUNT("label.errorthreshold.option.count");

        private final String value;

        ERROR_THRESHOLD_OPTIONS(final String value) {
            this.value = value;
        }

        private String getValue(final VaadinMessageSource i18n) {
            return i18n.getMessage(value);
        }
    }
}
