/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Optional;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.builder.SoftwareModuleCreate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.common.CommonDialogWindowV7;
import org.eclipse.hawkbit.ui.common.CommonDialogWindowV7.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilderV7;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilderV7;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilderV7;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilderV7;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

/**
 * Generates window for Software module add or update.
 */
public class SoftwareModuleAddUpdateWindow extends CustomComponent {

    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final UINotification uiNotifcation;

    private final transient EventBus.UIEventBus eventBus;

    private final transient SoftwareModuleManagement softwareModuleManagement;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;

    private final transient EntityFactory entityFactory;

    private TextField nameTextField;

    private TextField versionTextField;

    private TextField vendorTextField;

    private ComboBox<ProxyType> typeComboBox;

    private TextArea descTextArea;

    private Boolean editSwModule = Boolean.FALSE;

    private Long baseSwModuleId;

    private FormLayout formLayout;

    private Label softwareModuleType;

    /**
     * Constructor for SoftwareModuleAddUpdateWindow
     * 
     * @param i18n
     *            I18N
     * @param uiNotifcation
     *            UINotification
     * @param eventBus
     *            UIEventBus
     * @param softwareModuleManagement
     *            management for {@link SoftwareModule}s
     * @param softwareModuleTypeManagement
     *            management for {@link SoftwareModuleType}s
     * @param entityFactory
     *            EntityFactory
     */
    public SoftwareModuleAddUpdateWindow(final VaadinMessageSource i18n, final UINotification uiNotifcation,
            final UIEventBus eventBus, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory) {
        this.i18n = i18n;
        this.uiNotifcation = uiNotifcation;
        this.eventBus = eventBus;
        this.softwareModuleManagement = softwareModuleManagement;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
        this.entityFactory = entityFactory;

        createRequiredComponents();
    }

    /**
     * Save or update the sw module.
     */
    private final class SaveOnDialogCloseListener implements SaveDialogCloseListener {
        @Override
        public void saveOrUpdate() {
            if (editSwModule) {
                updateSwModule();
                return;
            }
            addNewBaseSoftware();
        }

        @Override
        public boolean canWindowSaveOrUpdate() {
            return editSwModule || !isDuplicate();
        }

        private void addNewBaseSoftware() {
            final String name = nameTextField.getValue();
            final String version = versionTextField.getValue();
            final String vendor = vendorTextField.getValue();
            final String description = descTextArea.getValue();
            final String type = typeComboBox.getSelectedItem().map(ProxyType::getName).orElse(null);

            final SoftwareModuleType softwareModuleTypeByName = softwareModuleTypeManagement.getByName(type)
                    .orElseThrow(() -> new EntityNotFoundException(SoftwareModuleType.class, type));
            final SoftwareModuleCreate softwareModule = entityFactory.softwareModule().create()
                    .type(softwareModuleTypeByName).name(name).version(version).description(description).vendor(vendor);

            final SoftwareModule newSoftwareModule = softwareModuleManagement.create(softwareModule);
            eventBus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.ADD_ENTITY,
                    new SoftwareModuleToProxyMapper().map(newSoftwareModule)));
            uiNotifcation.displaySuccess(i18n.getMessage("message.save.success",
                    newSoftwareModule.getName() + ":" + newSoftwareModule.getVersion()));
        }

        private boolean isDuplicate() {
            final String name = nameTextField.getValue();
            final String version = versionTextField.getValue();
            final String type = typeComboBox.getSelectedItem().map(ProxyType::getName).orElse(null);

            final Optional<Long> moduleType = softwareModuleTypeManagement.getByName(type)
                    .map(SoftwareModuleType::getId);
            if (moduleType.isPresent() && softwareModuleManagement
                    .getByNameAndVersionAndType(name, version, moduleType.get()).isPresent()) {
                uiNotifcation
                        .displayValidationError(i18n.getMessage("message.duplicate.softwaremodule", name, version));
                return true;
            }
            return false;
        }

        /**
         * updates a softwareModule
         */
        private void updateSwModule() {
            final SoftwareModule newSWModule = softwareModuleManagement.update(entityFactory.softwareModule()
                    .update(baseSwModuleId).description(descTextArea.getValue()).vendor(vendorTextField.getValue()));
            if (newSWModule != null) {
                uiNotifcation.displaySuccess(i18n.getMessage("message.save.success",
                        newSWModule.getName() + ":" + newSWModule.getVersion()));

                eventBus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.UPDATED_ENTITY,
                        new SoftwareModuleToProxyMapper().map(newSWModule)));
            }
        }

    }

    /**
     * Creates window for new software module.
     * 
     * @return reference of {@link com.vaadin.ui.Window} to add new software
     *         module.
     */
    public CommonDialogWindowV7 createAddSoftwareModuleWindow() {
        return createUpdateSoftwareModuleWindow(null);
    }

    /**
     * Creates window for update software module.
     * 
     * @param baseSwModuleId
     *            id of the software module to edit.
     * @return reference of {@link com.vaadin.ui.Window} to update software
     *         module.
     */
    public CommonDialogWindowV7 createUpdateSoftwareModuleWindow(final Long baseSwModuleId) {
        this.baseSwModuleId = baseSwModuleId;
        resetComponents();
        populateValuesOfSwModule();
        return createWindow();
    }

    private void createRequiredComponents() {

        nameTextField = createTextField("textfield.name", UIComponentIdProvider.SOFT_MODULE_NAME,
                SoftwareModule.NAME_MAX_SIZE);

        versionTextField = createTextField("textfield.version", UIComponentIdProvider.SOFT_MODULE_VERSION,
                SoftwareModule.VERSION_MAX_SIZE);

        vendorTextField = new TextFieldBuilderV7(SoftwareModule.VENDOR_MAX_SIZE)
                .caption(i18n.getMessage("textfield.vendor")).id(UIComponentIdProvider.SOFT_MODULE_VENDOR)
                .buildTextComponent();

        descTextArea = new TextAreaBuilderV7(SoftwareModule.DESCRIPTION_MAX_SIZE)
                .caption(i18n.getMessage("textfield.description")).style("text-area-style")
                .id(UIComponentIdProvider.ADD_SW_MODULE_DESCRIPTION).buildTextComponent();

        typeComboBox = new ComboBox<>(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_SOFTWARE_MODULE_TYPE));
        typeComboBox.setDescription(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_SOFTWARE_MODULE_TYPE));
        typeComboBox.setId(UIComponentIdProvider.SW_MODULE_TYPE);
        typeComboBox.addStyleName(SPUIDefinitions.COMBO_BOX_SPECIFIC_STYLE);
        typeComboBox.addStyleName(ValoTheme.COMBOBOX_TINY);
        typeComboBox.setItemCaptionGenerator(ProxyType::getName);

        typeComboBox.setDataProvider(
                new SoftwareModuleTypeDataProvider(softwareModuleTypeManagement, new TypeToProxyTypeMapper<>()));
    }

    private TextField createTextField(final String in18Key, final String id, final int maxLength) {
        return new TextFieldBuilderV7(maxLength).caption(i18n.getMessage(in18Key)).required(true, i18n).id(id)
                .buildTextComponent();
    }

    private void resetComponents() {
        vendorTextField.clear();
        nameTextField.clear();
        versionTextField.clear();
        descTextArea.clear();
        if (!editSwModule) {
            typeComboBox.clear();
        }
        editSwModule = Boolean.FALSE;
    }

    private CommonDialogWindowV7 createWindow() {
        final Label madatoryStarLabel = new Label("*");
        madatoryStarLabel.setStyleName("v-caption v-required-field-indicator");
        madatoryStarLabel.setWidth(null);
        addStyleName("lay-color");
        setSizeUndefined();

        formLayout = new FormLayout();
        formLayout.setCaption(null);
        if (editSwModule) {
            formLayout.addComponent(softwareModuleType);
        } else {
            formLayout.addComponent(typeComboBox);
            typeComboBox.focus();
        }

        formLayout.addComponent(nameTextField);
        formLayout.addComponent(versionTextField);
        formLayout.addComponent(vendorTextField);
        formLayout.addComponent(descTextArea);

        setCompositionRoot(formLayout);

        final CommonDialogWindowV7 window = new WindowBuilderV7(SPUIDefinitions.CREATE_UPDATE_WINDOW)
                .caption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.software.module")))
                .id(UIComponentIdProvider.SW_MODULE_CREATE_DIALOG).content(this).layout(formLayout).i18n(i18n)
                .saveDialogCloseListener(new SaveOnDialogCloseListener()).buildCommonDialogWindow();
        nameTextField.setEnabled(!editSwModule);
        versionTextField.setEnabled(!editSwModule);

        return window;
    }

    /**
     * fill the data of a softwareModule in the content of the window
     */
    private void populateValuesOfSwModule() {
        if (baseSwModuleId == null) {
            return;
        }
        editSwModule = Boolean.TRUE;
        softwareModuleManagement.get(baseSwModuleId).ifPresent(swModule -> {
            nameTextField.setValue(swModule.getName());
            versionTextField.setValue(swModule.getVersion());
            vendorTextField.setValue(swModule.getVendor());
            descTextArea.setValue(swModule.getDescription());
            softwareModuleType = new LabelBuilderV7().name(swModule.getType().getName())
                    .caption(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_SOFTWARE_MODULE_TYPE)).buildLabel();
        });
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

}
