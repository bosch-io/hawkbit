/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetAttributesDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.KeyValueDetailsComponent;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class TargetAttributesDetailsComponent extends CustomField<ProxyTargetAttributesDetails> {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;
    private final transient TargetManagement targetManagement;

    private final VerticalLayout targetAttributesDetailsLayout;

    public TargetAttributesDetailsComponent(final VaadinMessageSource i18n, final TargetManagement targetManagement) {
        this.i18n = i18n;
        this.targetManagement = targetManagement;

        this.targetAttributesDetailsLayout = new VerticalLayout();
        this.targetAttributesDetailsLayout.setSpacing(false);
        this.targetAttributesDetailsLayout.setMargin(false);

        setReadOnly(true);
    }

    @Override
    public ProxyTargetAttributesDetails getValue() {
        // not needed to return meaningful object here, because it is
        // intended to be read-only
        return new ProxyTargetAttributesDetails();
    }

    @Override
    protected Component initContent() {
        return targetAttributesDetailsLayout;
    }

    @Override
    protected void doSetValue(final ProxyTargetAttributesDetails targetAttributesDetails) {
        // TODO: Consider changing the logic not to recreate all the elements
        // every time target is updated
        targetAttributesDetailsLayout.removeAllComponents();

        if (targetAttributesDetails == null) {
            return;
        }

        if (targetAttributesDetails.isRequestAttributes()) {
            targetAttributesDetailsLayout.addComponent(buildAttributesUpdateLabel());
        }

        targetAttributesDetailsLayout.addComponent(buildRequestAttributesUpdateButton(
                targetAttributesDetails.getControllerId(), targetAttributesDetails.isRequestAttributes()));

        final KeyValueDetailsComponent attributes = new KeyValueDetailsComponent();
        targetAttributesDetailsLayout.addComponent(attributes);
        attributes.setValue(targetAttributesDetails.getTargetAttributes());
    }

    private Label buildAttributesUpdateLabel() {
        final Label updateLabel = new Label(i18n.getMessage("label.target.attributes.update.pending"));
        updateLabel.setStyleName(ValoTheme.LABEL_SMALL);

        return updateLabel;
    }

    private Button buildRequestAttributesUpdateButton(final String controllerId, final boolean isRequestAttributes) {
        final Button requestAttributesButton = SPUIComponentProvider.getButton(
                UIComponentIdProvider.TARGET_ATTRIBUTES_UPDATE, "", "", "", false, VaadinIcons.REFRESH,
                SPUIButtonStyleNoBorder.class);

        // TODO: Consider changing the logic not to wait until the target is
        // updated just to show label
        requestAttributesButton.addClickListener(e -> {
            toggleRequestAttributesUpdateButton(e.getButton(), true);
            targetManagement.requestControllerAttributes(controllerId);
        });

        toggleRequestAttributesUpdateButton(requestAttributesButton, isRequestAttributes);

        return requestAttributesButton;
    }

    private void toggleRequestAttributesUpdateButton(final Button requestAttributesButton,
            final boolean isRequestAttributes) {
        requestAttributesButton
                .setDescription(isRequestAttributes ? i18n.getMessage("tooltip.target.attributes.update.requested")
                        : i18n.getMessage("tooltip.target.attributes.update.request"));
        requestAttributesButton.setEnabled(!isRequestAttributes);
    }
}