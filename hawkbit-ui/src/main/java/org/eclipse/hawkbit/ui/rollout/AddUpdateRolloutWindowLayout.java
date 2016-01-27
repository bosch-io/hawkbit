/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.DistributionSetIdName;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessCondition;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleSmallNoBorder;
import org.eclipse.hawkbit.ui.documentation.DocumentationPageLink;
import org.eclipse.hawkbit.ui.filtermanagement.TargetFilterBeanQuery;
import org.eclipse.hawkbit.ui.management.footer.ActionTypeOptionGroupLayout;
import org.eclipse.hawkbit.ui.management.footer.ActionTypeOptionGroupLayout.ActionTypeOption;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIComponetIdProvider;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.spring.events.EventBus;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * 
 * Rollout add or update popup layout.
 *
 */
@SpringComponent
@ViewScope
public class AddUpdateRolloutWindowLayout extends CustomComponent {

    private static final long serialVersionUID = 2999293468801479916L;

    @Autowired
    private ActionTypeOptionGroupLayout actionTypeOptionGroupLayout;

    @Autowired
    private transient RolloutManagement rolloutManagement;

    @Autowired
    private transient DistributionSetManagement distributionSetManagement;

    @Autowired
    private transient TargetManagement targetManagement;

    @Autowired
    private UINotification uiNotification;

    @Autowired
    private I18N i18n;

    @Autowired
    private transient EventBus.SessionEventBus eventBus;

    private Label madatoryLabel;

    private TextField rolloutName;

    private ComboBox distributionSet;

    private ComboBox targetFilterQueryCombo;

    private TextField noOfGroups;

    private TextField triggerThreshold;

    private TextField errorThreshold;

    private TextArea description;

    private Button saveRollout;

    private Button discardRolllout;

    private OptionGroup saveStartOptionGroup;

    private OptionGroup errorThresholdOptionGroup;

    private Link linkToHelp;

    private Window addUpdateRolloutWindow;

    private Boolean editRollout;

    private Rollout rolloutForEdit;

    private Long totalTargetsCount;

    private Label totalTargetsLabel;

    private TextArea targetFilterQuery;

    /**
     * Create components and layout.
     */
    public void init() {
        createRequiredComponents();
        buildLayout();
    }

    public Window getWindow() {
        addUpdateRolloutWindow = SPUIComponentProvider.getWindow(i18n.get("caption.configure.rollout"), null,
                SPUIDefinitions.CREATE_UPDATE_WINDOW);
        addUpdateRolloutWindow.setContent(this);
        return addUpdateRolloutWindow;
    }

    /**
     * Reset the field values.
     */
    public void resetComponents() {
        editRollout = Boolean.FALSE;
        rolloutName.clear();
        targetFilterQuery.clear();
        resetFields();
        enableFields();
        populateDistributionSet();
        populateTargetFilterQuery();
        setDefaultSaveStartGroupOption();
        totalTargetsLabel.setVisible(false);
        targetFilterQuery.setVisible(false);
        targetFilterQueryCombo.setVisible(true);
        actionTypeOptionGroupLayout.selectDefaultOption();
        totalTargetsCount = 0L;
        rolloutForEdit = null;
    }

    private void resetFields() {
        rolloutName.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        noOfGroups.clear();
        noOfGroups.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        triggerThreshold.clear();
        triggerThreshold.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        errorThreshold.clear();
        errorThreshold.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        description.clear();
        description.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
    }

    private void buildLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(Boolean.TRUE);
        mainLayout.setSizeUndefined();

        mainLayout.addComponents(madatoryLabel, rolloutName, distributionSet, getTargetFilterLayout(), noOfGroups,
                getTriggerThresoldLayout(), getErrorThresoldLayout(), description, actionTypeOptionGroupLayout,
                getSaveStartOptionLayout(), getSaveDiscardButtonLayout());

        setCompositionRoot(mainLayout);
    }

    private HorizontalLayout getErrorThresoldLayout() {
        final HorizontalLayout errorThresoldLayout = new HorizontalLayout();
        errorThresoldLayout.setSizeFull();
        errorThresoldLayout.addComponents(errorThreshold, errorThresholdOptionGroup);
        errorThresoldLayout.setExpandRatio(errorThreshold, 1.0f);
        return errorThresoldLayout;
    }

    private HorizontalLayout getTargetFilterLayout() {
        final HorizontalLayout targetFilterLayout = new HorizontalLayout();
        targetFilterLayout.setSizeFull();
        targetFilterLayout.addComponents(targetFilterQueryCombo, targetFilterQuery, totalTargetsLabel);
        targetFilterLayout.setExpandRatio(targetFilterQueryCombo, 0.71f);
        targetFilterLayout.setExpandRatio(targetFilterQuery, 0.70f);
        targetFilterLayout.setExpandRatio(totalTargetsLabel, 0.29f);
        targetFilterLayout.setComponentAlignment(totalTargetsLabel, Alignment.MIDDLE_CENTER);
        return targetFilterLayout;
    }

    private HorizontalLayout getTriggerThresoldLayout() {
        final HorizontalLayout triggerThresholdLayout = new HorizontalLayout();
        triggerThresholdLayout.setSizeFull();
        triggerThresholdLayout.addComponents(triggerThreshold, getPercentHintLabel());
        triggerThresholdLayout.setExpandRatio(triggerThreshold, 1.0f);
        return triggerThresholdLayout;
    }

    private Label getPercentHintLabel() {
        final Label percentSymbol = new Label("%");
        percentSymbol.addStyleName(ValoTheme.LABEL_TINY + " " + ValoTheme.LABEL_BOLD);
        percentSymbol.setSizeUndefined();
        return percentSymbol;
    }

    private HorizontalLayout getSaveStartOptionLayout() {
        final HorizontalLayout layout = new HorizontalLayout(saveStartOptionGroup, linkToHelp);
        layout.setSizeFull();
        layout.setComponentAlignment(saveStartOptionGroup, Alignment.MIDDLE_LEFT);
        layout.setComponentAlignment(linkToHelp, Alignment.MIDDLE_RIGHT);
        return layout;
    }

    private HorizontalLayout getSaveDiscardButtonLayout() {
        final HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.addComponents(saveRollout, discardRolllout);
        buttonsLayout.setComponentAlignment(saveRollout, Alignment.BOTTOM_LEFT);
        buttonsLayout.setComponentAlignment(discardRolllout, Alignment.BOTTOM_RIGHT);
        buttonsLayout.addStyleName("window-style");
        return buttonsLayout;
    }

    private void createRequiredComponents() {
        madatoryLabel = createMandatoryLabel();
        rolloutName = createRolloutNameField();
        distributionSet = createDistributionSetCombo();
        populateDistributionSet();

        targetFilterQueryCombo = createTargetFilterQueryCombo();
        populateTargetFilterQuery();

        noOfGroups = createNoOfGroupsField();
        triggerThreshold = createTriggerThresold();
        errorThreshold = createErrorThresold();
        description = createDescription();
        saveStartOptionGroup = createSaveStartOptionGroup();
        errorThresholdOptionGroup = createErrorThresholdOptionGroup();
        setDefaultSaveStartGroupOption();
        saveRollout = createSaveButton();
        discardRolllout = createDiscardButton();
        actionTypeOptionGroupLayout.selectDefaultOption();

        totalTargetsLabel = createTotalTargetsLabel();
        targetFilterQuery = createTargetFilterQuery();

        linkToHelp = DocumentationPageLink.DEPLOYMENT_VIEW.getLink();
        actionTypeOptionGroupLayout.addStyleName(SPUIStyleDefinitions.ROLLOUT_ACTION_TYPE_LAYOUT);

    }

    private TextArea createTargetFilterQuery() {
        final TextArea filterField = SPUIComponentProvider.getTextArea("text-area-style", ValoTheme.TEXTFIELD_TINY,
                false, null, null, SPUILabelDefinitions.TARGET_FILTER_QUERY_TEXT_FIELD_LENGTH);
        filterField.setId(SPUIComponetIdProvider.ROLLOUT_TARGET_FILTER_QUERY_FIELD);
        filterField.setNullRepresentation(HawkbitCommonUtil.SP_STRING_EMPTY);
        filterField.setVisible(false);
        filterField.setEnabled(false);
        filterField.setSizeFull();
        return filterField;
    }

    private Label createTotalTargetsLabel() {
        final Label targetCountLabel = SPUIComponentProvider.getLabel("", SPUILabelDefinitions.SP_LABEL_SIMPLE);
        targetCountLabel.addStyleName(ValoTheme.LABEL_TINY + " " + "rollout-target-count-message");
        targetCountLabel.setImmediate(true);
        targetCountLabel.setVisible(false);
        targetCountLabel.setSizeUndefined();
        return targetCountLabel;
    }

    private OptionGroup createErrorThresholdOptionGroup() {
        final OptionGroup errorThresoldOptions = new OptionGroup();
        for (final ERRORTHRESOLDOPTIONS option : ERRORTHRESOLDOPTIONS.values()) {
            errorThresoldOptions.addItem(option.getValue());
        }
        errorThresoldOptions.setId(SPUIComponetIdProvider.ROLLOUT_ERROR_THRESOLD_OPTION_ID);
        errorThresoldOptions.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        errorThresoldOptions.addStyleName(SPUIStyleDefinitions.ROLLOUT_OPTION_GROUP);
        errorThresoldOptions.setSizeUndefined();
        errorThresoldOptions.addValueChangeListener(event -> onErrorThresoldOptionChange(event));
        return errorThresoldOptions;
    }

    private void onErrorThresoldOptionChange(final ValueChangeEvent event) {
        errorThreshold.clear();
        errorThreshold.removeAllValidators();
        if (event.getProperty().getValue().equals(ERRORTHRESOLDOPTIONS.COUNT.getValue())) {
            errorThreshold.addValidator(new ErrorThresoldOptionValidator());
        } else {
            errorThreshold.addValidator(new ThresoldFieldValidator());
        }
        errorThreshold.getValidators();
    }

    private ComboBox createTargetFilterQueryCombo() {
        final ComboBox targetFilter = SPUIComponentProvider.getComboBox("", "", null, null, true, "",
                i18n.get("prompt.target.filter"));
        targetFilter.setImmediate(true);
        targetFilter.setPageLength(7);
        targetFilter.setItemCaptionPropertyId(SPUILabelDefinitions.VAR_NAME);
        targetFilter.setId(SPUIComponetIdProvider.ROLLOUT_TARGET_FILTER_COMBO_ID);
        targetFilter.setSizeFull();
        targetFilter.addValueChangeListener(event -> onTargetFilterChange());
        return targetFilter;
    }

    private void onTargetFilterChange() {
        final String filterQueryString = getTargetFilterQuery();
        if (filterQueryString != null) {
            totalTargetsCount = targetManagement.countTargetByTargetFilterQuery(filterQueryString);
            totalTargetsLabel.setValue(getTotalTargetMessage());
            totalTargetsLabel.setVisible(true);
        }
    }

    private String getTotalTargetMessage() {
        return new StringBuilder(i18n.get("label.target.filter.count")).append(totalTargetsCount).toString();
    }

    private void populateTargetFilterQuery() {
        final Container container = createTargetFilterComboContainer();
        targetFilterQueryCombo.setContainerDataSource(container);
    }

    private Container createTargetFilterComboContainer() {
        final BeanQueryFactory<TargetFilterBeanQuery> targetFilterQF = new BeanQueryFactory<>(
                TargetFilterBeanQuery.class);
        final LazyQueryContainer targetFilterContainer = new LazyQueryContainer(new LazyQueryDefinition(true,
                SPUIDefinitions.PAGE_SIZE, SPUILabelDefinitions.VAR_NAME), targetFilterQF);
        return targetFilterContainer;

    }

    private Button createDiscardButton() {
        final Button discardRollloutBtn = SPUIComponentProvider.getButton(
                SPUIComponetIdProvider.ROLLOUT_CREATE_UPDATE_DISCARD_ID, "", "", "", true, FontAwesome.TIMES,
                SPUIButtonStyleSmallNoBorder.class);
        discardRollloutBtn.addClickListener(event -> onDiscard());
        return discardRollloutBtn;
    }

    private Button createSaveButton() {
        final Button saveRolloutBtn = SPUIComponentProvider.getButton(
                SPUIComponetIdProvider.ROLLOUT_CREATE_UPDATE_SAVE_ID, "", "", "", true, FontAwesome.SAVE,
                SPUIButtonStyleSmallNoBorder.class);
        saveRolloutBtn.addClickListener(event -> onRolloutSave());
        saveRolloutBtn.setImmediate(true);
        return saveRolloutBtn;
    }

    private void onDiscard() {
        closeThisWindow();
    }

    private void onRolloutSave() {
        if (editRollout) {
            editRollout();
        } else {
            createRollout();
        }
    }

    private void editRollout() {
        if (mandatoryCheckForEdit() && validateFields() && duplicateCheckForEdit() && null != rolloutForEdit) {
            rolloutForEdit.setName(rolloutName.getValue());
            rolloutForEdit.setDescription(description.getValue());
            final DistributionSetIdName distributionSetIdName = (DistributionSetIdName) distributionSet.getValue();
            rolloutForEdit.setDistributionSet(distributionSetManagement.findDistributionSetById(distributionSetIdName
                    .getId()));
            rolloutForEdit.setActionType(getActionType());
            rolloutForEdit.setForcedTime(getForcedTimeStamp());
            final int amountGroup = Integer.parseInt(noOfGroups.getValue());
            final int errorThresoldPercent = getErrorThresoldPercentage(amountGroup);

            for (final RolloutGroup rolloutGroup : rolloutForEdit.getRolloutGroups()) {
                rolloutGroup.setErrorConditionExp(triggerThreshold.getValue());
                rolloutGroup.setSuccessConditionExp(String.valueOf(errorThresoldPercent));
            }
            final Rollout updatedRollout = rolloutManagement.updateRollout(rolloutForEdit);
            uiNotification
                    .displaySuccess(i18n.get("message.update.success", new Object[] { updatedRollout.getName() }));
            if (rolloutForEdit.getStatus() == RolloutStatus.READY
                    && saveStartOptionGroup.getValue().equals(SAVESTARTOPTIONS.START.getValue())) {
                rolloutManagement.startRollout(updatedRollout);
            }
            closeThisWindow();
            eventBus.publish(this, RolloutEvent.UPDATE_ROLLOUT);
        }
    }

    private boolean duplicateCheckForEdit() {
        final String rolloutNameVal = getRolloutName();
        if (!rolloutForEdit.getName().equals(rolloutNameVal)
                && rolloutManagement.findRolloutByName(rolloutNameVal) != null) {
            uiNotification.displayValidationError(i18n.get("message.rollout.duplicate.check", rolloutNameVal));
            return false;
        }
        return true;
    }

    private long getForcedTimeStamp() {
        return (((ActionTypeOptionGroupLayout.ActionTypeOption) actionTypeOptionGroupLayout.getActionTypeOptionGroup()
                .getValue()) == ActionTypeOption.AUTO_FORCED) ? actionTypeOptionGroupLayout.getForcedTimeDateField()
                .getValue().getTime() : Action.NO_FORCE_TIME;
    }

    private ActionType getActionType() {
        return ((ActionTypeOptionGroupLayout.ActionTypeOption) actionTypeOptionGroupLayout.getActionTypeOptionGroup()
                .getValue()).getActionType();
    }

    private void createRollout() {
        if (mandatoryCheck() && validateFields() && duplicateCheck()) {
            final Rollout rolloutToCreate = saveRollout();
            if (saveStartOptionGroup.getValue().equals(SAVESTARTOPTIONS.START.getValue())) {
                rolloutManagement.startRollout(rolloutToCreate);
            }
            uiNotification.displaySuccess(i18n.get("message.save.success", new Object[] { rolloutToCreate.getName() }));
            eventBus.publish(this, RolloutEvent.CREATE_ROLLOUT);
            closeThisWindow();
        }
    }

    private Rollout saveRollout() {
        Rollout rolloutToCreate = new Rollout();
        final int amountGroup = Integer.parseInt(noOfGroups.getValue());
        final String targetFilter = getTargetFilterQuery();
        final int errorThresoldPercent = getErrorThresoldPercentage(amountGroup);

        final RolloutGroupConditions conditions = new RolloutGroup.RolloutGroupConditionBuilder()
                .successAction(RolloutGroupSuccessAction.NEXTGROUP, null)
                .successCondition(RolloutGroupSuccessCondition.THRESHOLD, triggerThreshold.getValue())
                .errorCondition(RolloutGroupErrorCondition.THRESHOLD, String.valueOf(errorThresoldPercent))
                .errorAction(RolloutGroupErrorAction.PAUSE, null).build();

        final DistributionSetIdName distributionSetIdName = (DistributionSetIdName) distributionSet.getValue();
        rolloutToCreate.setName(rolloutName.getValue());
        rolloutToCreate.setDescription(description.getValue());
        rolloutToCreate.setTargetFilterQuery(targetFilter);
        rolloutToCreate.setDistributionSet(distributionSetManagement.findDistributionSetById(distributionSetIdName
                .getId()));
        rolloutToCreate.setActionType(getActionType());
        rolloutToCreate.setForcedTime(getForcedTimeStamp());

        rolloutToCreate = rolloutManagement.createRolloutAsync(rolloutToCreate, amountGroup, conditions);
        return rolloutToCreate;
    }

    private String getTargetFilterQuery() {
        if (null != targetFilterQueryCombo.getValue()
                && HawkbitCommonUtil.trimAndNullIfEmpty((String) targetFilterQueryCombo.getValue()) != null) {
            final Item filterItem = targetFilterQueryCombo.getContainerDataSource().getItem(
                    targetFilterQueryCombo.getValue());
            final String targetFilter = (String) filterItem.getItemProperty("query").getValue();
            return targetFilter;
        }
        return null;
    }

    private int getErrorThresoldPercentage(final int amountGroup) {
        int errorThresoldPercent = Integer.parseInt(errorThreshold.getValue());
        if (errorThresholdOptionGroup.getValue().equals(ERRORTHRESOLDOPTIONS.COUNT.getValue())) {
            final int groupSize = (int) Math.ceil((double) totalTargetsCount / (double) amountGroup);
            final int erroThresoldCount = Integer.parseInt(errorThreshold.getValue());
            errorThresoldPercent = (int) Math.ceil(((float) erroThresoldCount / (float) groupSize) * 100);
        }
        return errorThresoldPercent;
    }

    private boolean validateFields() {
        if (!noOfGroups.isValid() || !errorThreshold.isValid() || !triggerThreshold.isValid()) {
            uiNotification.displayValidationError(i18n.get("message.correct.invalid.value"));
            return false;
        }
        return true;
    }

    private void closeThisWindow() {
        addUpdateRolloutWindow.close();
        UI.getCurrent().removeWindow(addUpdateRolloutWindow);
    }

    private boolean mandatoryCheck() {
        final DistributionSetIdName ds = getDistributionSetSelected();
        final String targetFilter = (String) targetFilterQueryCombo.getValue();
        final String triggerThresoldValue = triggerThreshold.getValue();
        final String errorThresoldValue = errorThreshold.getValue();
        if (hasNoNameOrTargetFilter(targetFilter) || ds == null
                || HawkbitCommonUtil.trimAndNullIfEmpty(noOfGroups.getValue()) == null
                || isThresholdValueMissing(triggerThresoldValue, errorThresoldValue)) {
            uiNotification.displayValidationError(i18n.get("message.mandatory.check"));
            return false;
        }
        return true;
    }

    private boolean mandatoryCheckForEdit() {
        final DistributionSetIdName ds = getDistributionSetSelected();
        final String targetFilter = targetFilterQuery.getValue();
        final String triggerThresoldValue = triggerThreshold.getValue();
        final String errorThresoldValue = errorThreshold.getValue();
        if (hasNoNameOrTargetFilter(targetFilter) || ds == null
                || HawkbitCommonUtil.trimAndNullIfEmpty(noOfGroups.getValue()) == null
                || isThresholdValueMissing(triggerThresoldValue, errorThresoldValue)) {
            uiNotification.displayValidationError(i18n.get("message.mandatory.check"));
            return false;
        }
        return true;
    }

    private boolean hasNoNameOrTargetFilter(final String targetFilter) {
        return getRolloutName() == null || targetFilter == null;
    }

    private boolean isThresholdValueMissing(final String triggerThresoldValue, final String errorThresoldValue) {
        return HawkbitCommonUtil.trimAndNullIfEmpty(triggerThresoldValue) == null
                || HawkbitCommonUtil.trimAndNullIfEmpty(errorThresoldValue) == null;
    }

    private boolean duplicateCheck() {
        if (rolloutManagement.findRolloutByName(getRolloutName()) != null) {
            uiNotification.displayValidationError(i18n.get("message.rollout.duplicate.check"));
            return false;
        }
        return true;
    }

    private OptionGroup createSaveStartOptionGroup() {
        final OptionGroup saveStartOptions = new OptionGroup();
        for (final SAVESTARTOPTIONS option : SAVESTARTOPTIONS.values()) {
            saveStartOptions.addItem(option.getValue());
        }
        saveStartOptions.setId(SPUIComponetIdProvider.ROLLOUT_SAVESTARTOPTION_ID);
        saveStartOptions.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        saveStartOptions.addStyleName(SPUIStyleDefinitions.ROLLOUT_OPTION_GROUP);
        return saveStartOptions;
    }

    private void setDefaultSaveStartGroupOption() {
        saveStartOptionGroup.setValue(SAVESTARTOPTIONS.SAVE.getValue());
        errorThresholdOptionGroup.setValue(ERRORTHRESOLDOPTIONS.PERCENT.getValue());
    }

    private TextArea createDescription() {
        final TextArea descriptionField = SPUIComponentProvider.getTextArea("text-area-style",
                ValoTheme.TEXTFIELD_TINY, false, null, i18n.get("textfield.description"),
                SPUILabelDefinitions.TEXT_AREA_MAX_LENGTH);
        descriptionField.setId(SPUIComponetIdProvider.ROLLOUT_DESCRIPTION_ID);
        descriptionField.setNullRepresentation(HawkbitCommonUtil.SP_STRING_EMPTY);
        descriptionField.setSizeFull();
        return descriptionField;
    }

    private TextField createErrorThresold() {
        final TextField errorField = SPUIComponentProvider.getTextField("", ValoTheme.TEXTFIELD_TINY, true, null,
                i18n.get("prompt.error.threshold"), true, SPUILabelDefinitions.TEXT_FIELD_MAX_LENGTH);
        errorField.addValidator(new ThresoldFieldValidator());
        errorField.setId(SPUIComponetIdProvider.ROLLOUT_ERROR_THRESOLD_ID);
        errorField.setMaxLength(7);
        errorField.setSizeFull();
        return errorField;
    }

    private TextField createTriggerThresold() {
        final TextField thresholdField = SPUIComponentProvider.getTextField("", ValoTheme.TEXTFIELD_TINY, true, null,
                i18n.get("prompt.tigger.thresold"), true, SPUILabelDefinitions.TEXT_FIELD_MAX_LENGTH);
        thresholdField.setId(SPUIComponetIdProvider.ROLLOUT_TRIGGER_THRESOLD_ID);
        thresholdField.addValidator(new ThresoldFieldValidator());
        thresholdField.setSizeFull();
        thresholdField.setMaxLength(3);
        return thresholdField;
    }

    private TextField createNoOfGroupsField() {
        final TextField noOfGroupsField = SPUIComponentProvider.getTextField("", ValoTheme.TEXTFIELD_TINY, true, null,
                i18n.get("prompt.number.of.groups"), true, SPUILabelDefinitions.TEXT_FIELD_MAX_LENGTH);
        noOfGroupsField.setId(SPUIComponetIdProvider.ROLLOUT_NO_OF_GROUPS_ID);
        noOfGroupsField.addValidator(new GroupNumberValidator());
        noOfGroupsField.setSizeFull();
        noOfGroupsField.setMaxLength(3);
        return noOfGroupsField;
    }

    private ComboBox createDistributionSetCombo() {
        final ComboBox dsSet = SPUIComponentProvider.getComboBox("", "", null, null, true, "",
                i18n.get("prompt.distribution.set"));
        dsSet.setImmediate(true);
        dsSet.setPageLength(7);
        dsSet.setItemCaptionPropertyId(SPUILabelDefinitions.VAR_NAME);
        dsSet.setId(SPUIComponetIdProvider.ROLLOUT_DS_ID);
        dsSet.setSizeFull();
        return dsSet;
    }

    private void populateDistributionSet() {
        final Container container = createDsComboContainer();
        distributionSet.setContainerDataSource(container);
    }

    private Container createDsComboContainer() {
        final BeanQueryFactory<DistBeanQuery> distributionQF = new BeanQueryFactory<>(DistBeanQuery.class);
        final LazyQueryContainer distributionContainer = new LazyQueryContainer(new LazyQueryDefinition(true,
                SPUIDefinitions.PAGE_SIZE, SPUILabelDefinitions.VAR_DIST_ID_NAME), distributionQF);
        return distributionContainer;

    }

    private TextField createRolloutNameField() {
        final TextField rolloutNameField = SPUIComponentProvider.getTextField("", ValoTheme.TEXTFIELD_TINY, true, null,
                i18n.get("textfield.name"), true, SPUILabelDefinitions.TEXT_FIELD_MAX_LENGTH);
        rolloutNameField.setId(SPUIComponetIdProvider.ROLLOUT_NAME_FIELD_ID);
        rolloutNameField.setSizeFull();
        return rolloutNameField;
    }

    private Label createMandatoryLabel() {
        final Label madatoryLbl = new Label(i18n.get("label.mandatory.field"));
        madatoryLbl.addStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR + " " + ValoTheme.LABEL_SMALL);
        return madatoryLbl;
    }

    private String getRolloutName() {
        return HawkbitCommonUtil.trimAndNullIfEmpty(rolloutName.getValue());
    }

    private DistributionSetIdName getDistributionSetSelected() {
        return (DistributionSetIdName) distributionSet.getValue();
    }

    class ErrorThresoldOptionValidator implements Validator {
        private static final long serialVersionUID = 9049939751976326550L;

        @Override
        public void validate(final Object value) throws InvalidValueException {
            try {
                if (HawkbitCommonUtil.trimAndNullIfEmpty(noOfGroups.getValue()) == null
                        || HawkbitCommonUtil.trimAndNullIfEmpty((String) targetFilterQueryCombo.getValue()) == null) {
                    uiNotification.displayValidationError(i18n
                            .get("message.rollout.noofgroups.or.targetfilter.missing"));
                } else {
                    new RegexpValidator("[-]?[0-9]*\\.?,?[0-9]+", i18n.get("message.enter.number")).validate(value);
                    final int groupSize = (int) Math.ceil((double) totalTargetsCount
                            / Double.parseDouble(noOfGroups.getValue()));
                    new IntegerRangeValidator(i18n.get("message.rollout.field.value.range", 0, groupSize), 0, groupSize)
                            .validate(Integer.parseInt(value.toString()));
                }
            } catch (final InvalidValueException ex) {
                throw ex;
            }
        }
    }

    class ThresoldFieldValidator implements Validator {
        private static final long serialVersionUID = 9049939751976326550L;

        @Override
        public void validate(final Object value) throws InvalidValueException {
            try {
                new RegexpValidator("[-]?[0-9]*\\.?,?[0-9]+", i18n.get("message.enter.number")).validate(value);
                new IntegerRangeValidator(i18n.get("message.rollout.field.value.range", 0, 100), 0, 100)
                        .validate(Integer.parseInt(value.toString()));
            } catch (final InvalidValueException ex) {
                throw ex;
            }
        }
    }

    class GroupNumberValidator implements Validator {
        private static final long serialVersionUID = 9049939751976326550L;

        @Override
        public void validate(final Object value) throws InvalidValueException {
            try {
                new RegexpValidator("[-]?[0-9]*\\.?,?[0-9]+", i18n.get("message.enter.number")).validate(value);
                new IntegerRangeValidator(i18n.get("message.rollout.field.value.range", 0, 500), 0, 500)
                        .validate(Integer.parseInt(value.toString()));
            } catch (final InvalidValueException ex) {
                throw ex;
            }
        }
    }

    /**
     * 
     * Populate rollout details.
     * 
     * @param rolloutId
     *            rollout id
     */
    public void populateData(final Long rolloutId) {
        resetComponents();
        editRollout = Boolean.TRUE;
        rolloutForEdit = rolloutManagement.findRolloutById(rolloutId);
        rolloutName.setValue(rolloutForEdit.getName());
        description.setValue(rolloutForEdit.getDescription());
        distributionSet.setValue(rolloutForEdit.getDistributionSet().getDistributionSetIdName());
        final List<RolloutGroup> rolloutGroups = rolloutForEdit.getRolloutGroups();
        setThresoldValues(rolloutGroups);
        if (rolloutForEdit.getStatus() != RolloutStatus.READY) {
            saveStartOptionGroup.setValue(SAVESTARTOPTIONS.START.getValue());
        }
        setActionType(rolloutForEdit);
        if (rolloutForEdit.getStatus() != RolloutStatus.READY) {
            disableRequiredFieldsOnEdit();
        }

        noOfGroups.setEnabled(false);
        targetFilterQuery.setValue(rolloutForEdit.getTargetFilterQuery());
        targetFilterQuery.setVisible(true);
        targetFilterQueryCombo.setVisible(false);

        totalTargetsCount = targetManagement.countTargetByTargetFilterQuery(rolloutForEdit.getTargetFilterQuery());
        totalTargetsLabel.setValue(getTotalTargetMessage());
        totalTargetsLabel.setVisible(true);
    }

    private void disableRequiredFieldsOnEdit() {
        distributionSet.setEnabled(false);
        errorThreshold.setEnabled(false);
        triggerThreshold.setEnabled(false);
        actionTypeOptionGroupLayout.getActionTypeOptionGroup().setEnabled(false);
        saveStartOptionGroup.setEnabled(false);
        errorThresholdOptionGroup.setEnabled(false);
        actionTypeOptionGroupLayout.addStyleName(SPUIStyleDefinitions.DISABLE_ACTION_TYPE_LAYOUT);
    }

    private void enableFields() {
        distributionSet.setEnabled(true);
        errorThreshold.setEnabled(true);
        triggerThreshold.setEnabled(true);
        actionTypeOptionGroupLayout.getActionTypeOptionGroup().setEnabled(true);
        actionTypeOptionGroupLayout.removeStyleName(SPUIStyleDefinitions.DISABLE_ACTION_TYPE_LAYOUT);
        saveStartOptionGroup.setEnabled(true);
        noOfGroups.setEnabled(true);
        targetFilterQueryCombo.setEnabled(true);
        errorThresholdOptionGroup.setEnabled(true);
    }

    private void setActionType(final Rollout rollout) {
        for (final ActionTypeOptionGroupLayout.ActionTypeOption groupAction : ActionTypeOptionGroupLayout.ActionTypeOption
                .values()) {
            if (groupAction.getActionType() == rollout.getActionType()) {
                actionTypeOptionGroupLayout.getActionTypeOptionGroup().setValue(groupAction);
                final SimpleDateFormat format = new SimpleDateFormat(SPUIDefinitions.LAST_QUERY_DATE_FORMAT);
                format.setTimeZone(SPDateTimeUtil.getBrowserTimeZone());
                actionTypeOptionGroupLayout.getForcedTimeDateField().setValue(new Date(rollout.getForcedTime()));
                break;
            }
        }
    }

    /**
     * @param rolloutGroups
     */
    private void setThresoldValues(final List<RolloutGroup> rolloutGroups) {
        if (null != rolloutGroups && !rolloutGroups.isEmpty()) {
            errorThreshold.setValue(rolloutGroups.get(0).getErrorConditionExp());
            triggerThreshold.setValue(rolloutGroups.get(0).getSuccessConditionExp());
            noOfGroups.setValue(String.valueOf(rolloutGroups.size()));
        } else {
            errorThreshold.setValue("0");
            triggerThreshold.setValue("0");
            noOfGroups.setValue("0");
        }
    }

    enum SAVESTARTOPTIONS {
        SAVE("Save"), START("Start");

        String value;

        private SAVESTARTOPTIONS(final String val) {
            this.value = val;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

    }

    enum ERRORTHRESOLDOPTIONS {
        PERCENT("%"), COUNT("Count");

        String value;

        private ERRORTHRESOLDOPTIONS(final String val) {
            this.value = val;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

    }
}
