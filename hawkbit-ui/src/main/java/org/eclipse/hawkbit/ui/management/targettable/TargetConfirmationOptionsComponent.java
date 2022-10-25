/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Arrays;
import java.util.List;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.AutoConfirmationStatus;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.CommonUiDependencies;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetConfirmationOptions;
import org.eclipse.hawkbit.ui.common.detailslayout.KeyValueDetailsComponent;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static org.eclipse.hawkbit.ui.utils.UIComponentIdProvider.AUTO_CONFIRMATION_ACTIVATION_DIALOG;
import static org.eclipse.hawkbit.ui.utils.UIComponentIdProvider.AUTO_CONFIRMATION_DETAILS_ACTIVATEDAT;
import static org.eclipse.hawkbit.ui.utils.UIComponentIdProvider.AUTO_CONFIRMATION_DETAILS_INITIATOR;
import static org.eclipse.hawkbit.ui.utils.UIComponentIdProvider.AUTO_CONFIRMATION_DETAILS_REMARK;
import static org.eclipse.hawkbit.ui.utils.UIComponentIdProvider.AUTO_CONFIRMATION_DETAILS_STATE;

/**
 * target auto confirmation detail component
 */
public class TargetConfirmationOptionsComponent extends CustomField<ProxyTargetConfirmationOptions> {

    private static final long serialVersionUID = 1L;

    private final transient TargetAutoConfActivationWindowBuilder windowBuilder;
    private final transient TargetManagement targetManagement;
    private final VaadinMessageSource i18n;
    private final HorizontalLayout targetConfirmationOptionsLayout;

    /**
     * Constructor for TargetConfirmationOptionsComponent
     *
     * @param commonUiDependencies
     *            the {@link CommonUiDependencies}
     * @param uiProperties
     *            the {@link UiProperties}
     * @param targetManagement
     *            the {@link TargetManagement}
     * @param tenantAware
     *            the {@link TenantAware}
     */
    public TargetConfirmationOptionsComponent(final CommonUiDependencies commonUiDependencies,
            final UiProperties uiProperties, final TargetManagement targetManagement, final TenantAware tenantAware) {
        this.i18n = commonUiDependencies.getI18n();
        this.targetManagement = targetManagement;
        this.windowBuilder = new TargetAutoConfActivationWindowBuilder(commonUiDependencies, uiProperties, tenantAware,
                this::onActivatedConfirmationOptions);

        this.targetConfirmationOptionsLayout = new HorizontalLayout();
        this.targetConfirmationOptionsLayout.setSpacing(true);
        this.targetConfirmationOptionsLayout.setMargin(false);
        this.targetConfirmationOptionsLayout.setSizeFull();
        this.targetConfirmationOptionsLayout.addStyleName("disable-horizontal-scroll");
    }

    @Override
    public ProxyTargetConfirmationOptions getValue() {
        // keep returning same value since reloading (triggering 'doSetValue' method)
        // will be done on button click.
        return new ProxyTargetConfirmationOptions();
    }

    @Override
    protected Component initContent() {
        return targetConfirmationOptionsLayout;
    }

    @Override
    protected void doSetValue(final ProxyTargetConfirmationOptions targetConfirmationOptions) {
        targetConfirmationOptionsLayout.removeAllComponents();

        if (targetConfirmationOptions == null) {
            return;
        }

        final boolean isAutoConfirmationEnabled = targetConfirmationOptions.isAutoConfirmationEnabled();

        if (isAutoConfirmationEnabled) {
            final VerticalLayout detailsLayout = buildAutoConfirmationDetailsLayout(targetConfirmationOptions);
            targetConfirmationOptionsLayout.addComponent(detailsLayout);
            targetConfirmationOptionsLayout.setExpandRatio(detailsLayout, 1.0F);
        } else {
            final Label confirmationRequiredLabel = buildConfirmationRequiredLabel();
            targetConfirmationOptionsLayout.addComponent(confirmationRequiredLabel);
            targetConfirmationOptionsLayout.setExpandRatio(confirmationRequiredLabel, 1.0F);
        }

        final Button button = buildAutoConfirmationToggleButton(targetConfirmationOptions);
        targetConfirmationOptionsLayout.addComponent(button);
    }

    private VerticalLayout buildAutoConfirmationDetailsLayout(final ProxyTargetConfirmationOptions options) {
        final VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setMargin(false);
        detailsLayout.setSpacing(false);

        final List<ProxyKeyValueDetails> values = Arrays.asList(
                new ProxyKeyValueDetails(AUTO_CONFIRMATION_DETAILS_STATE,
                        i18n.getMessage("label.target.auto.confirmation.state"),
                        i18n.getMessage("label.target.auto.confirmation.active")),
                new ProxyKeyValueDetails(AUTO_CONFIRMATION_DETAILS_INITIATOR,
                        i18n.getMessage("label.target.auto.confirmation.initiator"), options.getInitiator()),
                new ProxyKeyValueDetails(AUTO_CONFIRMATION_DETAILS_REMARK,
                        i18n.getMessage("label.target.auto.confirmation.remark"), options.getRemark()),
                new ProxyKeyValueDetails(AUTO_CONFIRMATION_DETAILS_ACTIVATEDAT,
                        i18n.getMessage("label.target.auto.confirmation.activatedat"),
                        SPDateTimeUtil.getFormattedDate(options.getActivatedAt())));

        final KeyValueDetailsComponent details = new KeyValueDetailsComponent();
        details.disableSpacing();
        details.setValue(values);
        detailsLayout.addComponent(details);

        return detailsLayout;
    }

    private Label buildConfirmationRequiredLabel() {
        final Label confirmationLabel = new Label(i18n.getMessage("label.target.auto.confirmation.disabled"));
        confirmationLabel.setStyleName(ValoTheme.LABEL_SMALL);
        return confirmationLabel;
    }

    private Button buildAutoConfirmationToggleButton(final ProxyTargetConfirmationOptions options) {
        final Button requestAttributesButton;

        if (options.isAutoConfirmationEnabled()) {
            requestAttributesButton = SPUIComponentProvider.getButton(UIComponentIdProvider.TARGET_ATTRIBUTES_UPDATE,
                    "", i18n.getMessage("button.target.auto.confirmation.disable"), "", false, VaadinIcons.DOT_CIRCLE,
                    SPUIButtonStyleNoBorder.class);
            requestAttributesButton.setStyleName(SPUIStyleDefinitions.STATUS_ICON_GREEN);
        } else {
            requestAttributesButton = SPUIComponentProvider.getButton(UIComponentIdProvider.TARGET_ATTRIBUTES_UPDATE,
                    "", i18n.getMessage("button.target.auto.confirmation.activate"), "", false, VaadinIcons.DOT_CIRCLE,
                    SPUIButtonStyleNoBorder.class);
            requestAttributesButton.setStyleName(SPUIStyleDefinitions.STATUS_ICON_YELLOW);
        }

        requestAttributesButton.addClickListener(e -> {
            if (options.isAutoConfirmationEnabled()) {
                final ConfirmationDialog dialog = new ConfirmationDialog(i18n,
                        i18n.getMessage("caption.target.auto.confirmation.disable"),
                        i18n.getMessage("message.target.auto.confirmation.disable"), ok -> {
                            if (ok) {
                                targetManagement.disableAutoConfirmation(options.getControllerId());
                                doSetValue(ProxyTargetConfirmationOptions.disabled(options.getControllerId()));
                            }
                        }, VaadinIcons.WARNING, AUTO_CONFIRMATION_ACTIVATION_DIALOG, null);

                UI.getCurrent().addWindow(dialog.getWindow());

                dialog.getWindow().bringToFront();
            } else {
                final Window window = windowBuilder.getWindowForUpdate(options);
                UI.getCurrent().addWindow(window);
                window.setVisible(Boolean.TRUE);
            }
        });

        return requestAttributesButton;
    }

    private void onActivatedConfirmationOptions(final ProxyTargetConfirmationOptions activatedOptions) {
        final AutoConfirmationStatus updatedStatus = targetManagement.activeAutoConfirmation(
                activatedOptions.getControllerId(), activatedOptions.getInitiator(), activatedOptions.getRemark());
        doSetValue(ProxyTargetConfirmationOptions.active(updatedStatus));
    }

}
