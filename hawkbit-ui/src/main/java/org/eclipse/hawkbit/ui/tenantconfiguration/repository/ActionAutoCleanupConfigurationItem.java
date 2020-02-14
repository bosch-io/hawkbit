/**
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.TenantConfiguration;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents the UI item for configuring automatic action cleanup in
 * the Repository Configuration section of the System Configuration view.
 */
public class ActionAutoCleanupConfigurationItem extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionAutoCleanupConfigurationItem.class);

    private static final int MAX_EXPIRY_IN_DAYS = 1000;
    public static final EnumSet<Status> EMPTY_STATUS_SET = EnumSet.noneOf(Status.class);

    private static final String MSG_KEY_PREFIX = "label.configuration.repository.autocleanup.action.prefix";
    private static final String MSG_KEY_BODY = "label.configuration.repository.autocleanup.action.body";
    private static final String MSG_KEY_SUFFIX = "label.configuration.repository.autocleanup.action.suffix";
    private static final String MSG_KEY_INVALID_EXPIRY = "label.configuration.repository.autocleanup.action.expiry.invalid";
    private static final String MSG_KEY_NOTICE = "label.configuration.repository.autocleanup.action.notice";

    public static final Collection<ActionStatusOption> ACTION_STATUS_OPTIONS = Arrays.asList(
            new ActionStatusOption(Status.CANCELED), new ActionStatusOption(Status.ERROR),
            new ActionStatusOption(Status.CANCELED, Status.ERROR));

    private final VerticalLayout container;
    private final ComboBox<ActionStatusOption> actionStatusCombobox;
    private final TextField actionExpiryInput;

    private final Binder<ProxySystemConfigWindow> binder;
    private final Binding<ProxySystemConfigWindow, String> actionExpiryInputBinding;
    private final VaadinMessageSource i18n;

    /**
     * Constructs the Action Cleanup configuration UI.
     *
     * @param binder
     * @param i18n
     */
    public ActionAutoCleanupConfigurationItem(Binder<ProxySystemConfigWindow> binder, final VaadinMessageSource i18n) {
        this.binder = binder;
        this.i18n = i18n;
        this.setSpacing(false);
        this.setMargin(false);
        addComponent(new LabelBuilder().name(i18n.getMessage("label.configuration.repository.autocleanup.action"))
                .buildLabel());
        container = new VerticalLayout();
        container.setSpacing(false);
        container.setMargin(false);
        final HorizontalLayout row1 = newHorizontalLayout();
        actionStatusCombobox = new ComboBox();
        actionStatusCombobox.setDescription("label.combobox.action.status.options");
        actionStatusCombobox.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_ACTION_CLEANUP_ACTION_TYPES);
        actionStatusCombobox.addStyleName(ValoTheme.COMBOBOX_TINY);
        actionStatusCombobox.setWidth(200f, Unit.PIXELS);
        actionStatusCombobox.setEmptySelectionAllowed(false);
        actionStatusCombobox.setItems(ACTION_STATUS_OPTIONS);
        actionStatusCombobox.setItemCaptionGenerator(ActionStatusOption::getName);
        binder.bind(actionStatusCombobox, ProxySystemConfigWindow::getActionCleanupStatus,
                ProxySystemConfigWindow::setActionCleanupStatus);
        actionExpiryInput = new TextFieldBuilder(TenantConfiguration.VALUE_MAX_SIZE).buildTextComponent();
        actionExpiryInput.setId(UIComponentIdProvider.SYSTEM_CONFIGURATION_ACTION_CLEANUP_ACTION_EXPIRY);
        actionExpiryInput.setWidth(55, Unit.PIXELS);
        actionExpiryInputBinding = binder.forField(actionExpiryInput)
                .asRequired(i18n.getMessage(MSG_KEY_INVALID_EXPIRY))
                .withValidator((value, context) -> {
                    try {
                        return new IntegerRangeValidator(i18n.getMessage(MSG_KEY_INVALID_EXPIRY), 1, MAX_EXPIRY_IN_DAYS)
                                .apply(Integer.parseInt(value), context);
                    } catch (final NumberFormatException ex) {
                        return ValidationResult.error(i18n.getMessage(MSG_KEY_INVALID_EXPIRY));
                    }
                })
                .bind(ProxySystemConfigWindow::getActionExpiryDays, ProxySystemConfigWindow::setActionExpiryDays);

        row1.addComponent(newLabel(MSG_KEY_PREFIX));
        row1.addComponent(actionStatusCombobox);
        row1.addComponent(newLabel(MSG_KEY_BODY));
        row1.addComponent(actionExpiryInput);
        row1.addComponent(newLabel(MSG_KEY_SUFFIX));
        container.addComponent(row1);

        final HorizontalLayout row2 = newHorizontalLayout();
        row2.addComponent(newLabel(MSG_KEY_NOTICE));
        container.addComponent(row2);
        if (binder.getBean().isActionAutocleanup()) {
            setSettingsVisible(true);
        }
    }

    private Label newLabel(final String msgKey) {
        final Label label = new LabelBuilder().name(i18n.getMessage(msgKey)).buildLabel();
        label.setWidthUndefined();
        return label;
    }

    private static HorizontalLayout newHorizontalLayout() {
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        return layout;
    }

    public void setSettingsVisible(final boolean visible) {
        if (visible) {
            addComponent(container);
        } else {
            removeComponent(container);
        }
    }

    public static class ActionStatusOption {
        private static final CharSequence SEPARATOR = " + ";
        private final Set<Status> statusSet;
        private String name;

        public ActionStatusOption(final Status... status) {
            statusSet = Arrays.stream(status).collect(Collectors.toCollection(() -> EnumSet.noneOf(Status.class)));
        }

        public String getName() {
            if (name == null) {
                name = assembleName();
            }
            return name;
        }

        public Set<Status> getStatus() {
            return statusSet;
        }

        private String assembleName() {
            return statusSet.stream().map(Status::name).collect(Collectors.joining(SEPARATOR));
        }

    }

}
