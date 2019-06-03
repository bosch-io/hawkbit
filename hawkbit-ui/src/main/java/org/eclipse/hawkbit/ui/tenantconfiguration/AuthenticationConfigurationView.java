/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SecurityTokenGenerator;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.AnonymousDownloadAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.CertificateAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.GatewaySecurityTokenAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.TargetSecurityTokenAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.generic.BooleanConfigurationItem;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * View to configure the authentication mode.
 */
public class AuthenticationConfigurationView extends BaseConfigurationView
        implements ConfigurationItem.ConfigurationItemChangeListener, ValueChangeListener {

    private static final String DIST_CHECKBOX_STYLE = "dist-checkbox-style";

    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final CertificateAuthenticationConfigurationItem certificateAuthenticationConfigurationItem;

    private final TargetSecurityTokenAuthenticationConfigurationItem targetSecurityTokenAuthenticationConfigurationItem;

    private final GatewaySecurityTokenAuthenticationConfigurationItem gatewaySecurityTokenAuthenticationConfigurationItem;

    private final AnonymousDownloadAuthenticationConfigurationItem anonymousDownloadAuthenticationConfigurationItem;

    private final UiProperties uiProperties;

    private CheckBox gatewaySecTokenCheckBox;

    private CheckBox targetSecTokenCheckBox;

    private CheckBox certificateAuthCheckbox;

    private CheckBox downloadAnonymousCheckBox;

    AuthenticationConfigurationView(final VaadinMessageSource i18n,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final SecurityTokenGenerator securityTokenGenerator, final UiProperties uiProperties) {
        this.i18n = i18n;
        this.uiProperties = uiProperties;
        this.certificateAuthenticationConfigurationItem = new CertificateAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n);
        this.targetSecurityTokenAuthenticationConfigurationItem = new TargetSecurityTokenAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n);
        this.gatewaySecurityTokenAuthenticationConfigurationItem = new GatewaySecurityTokenAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n, securityTokenGenerator);
        this.anonymousDownloadAuthenticationConfigurationItem = new AnonymousDownloadAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n);

        init();
    }

    private void init() {

        final Panel rootPanel = new Panel();
        rootPanel.setSizeFull();

        rootPanel.addStyleName("config-panel");

        final VerticalLayout vLayout = new VerticalLayout();
        vLayout.setMargin(true);
        vLayout.setSizeFull();

        final Label header = new Label(i18n.getMessage("configuration.authentication.title"));
        header.addStyleName("config-panel-header");
        vLayout.addComponent(header);

        final GridLayout gridLayout = new GridLayout(3, 4);
        gridLayout.setSpacing(true);
        gridLayout.setImmediate(true);
        gridLayout.setSizeFull();
        gridLayout.setColumnExpandRatio(1, 1.0F);

        certificateAuthCheckbox = SPUIComponentProvider.getCheckBox("", DIST_CHECKBOX_STYLE, null, false, "");
        certificateAuthCheckbox.setValue(certificateAuthenticationConfigurationItem.isConfigEnabled());
        certificateAuthCheckbox.addValueChangeListener(this);
        certificateAuthenticationConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(certificateAuthCheckbox, 0, 0);
        gridLayout.addComponent(certificateAuthenticationConfigurationItem, 1, 0);

        targetSecTokenCheckBox = SPUIComponentProvider.getCheckBox("", DIST_CHECKBOX_STYLE, null, false, "");
        targetSecTokenCheckBox.setValue(targetSecurityTokenAuthenticationConfigurationItem.isConfigEnabled());
        targetSecTokenCheckBox.addValueChangeListener(this);
        targetSecurityTokenAuthenticationConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(targetSecTokenCheckBox, 0, 1);
        gridLayout.addComponent(targetSecurityTokenAuthenticationConfigurationItem, 1, 1);

        gatewaySecTokenCheckBox = SPUIComponentProvider.getCheckBox("", DIST_CHECKBOX_STYLE, null, false, "");
        gatewaySecTokenCheckBox.setId("gatewaysecuritycheckbox");
        gatewaySecTokenCheckBox.setValue(gatewaySecurityTokenAuthenticationConfigurationItem.isConfigEnabled());
        gatewaySecTokenCheckBox.addValueChangeListener(this);
        gatewaySecurityTokenAuthenticationConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(gatewaySecTokenCheckBox, 0, 2);
        gridLayout.addComponent(gatewaySecurityTokenAuthenticationConfigurationItem, 1, 2);

        downloadAnonymousCheckBox = SPUIComponentProvider.getCheckBox("", DIST_CHECKBOX_STYLE, null, false, "");
        downloadAnonymousCheckBox.setId(UIComponentIdProvider.DOWNLOAD_ANONYMOUS_CHECKBOX);
        downloadAnonymousCheckBox.setValue(anonymousDownloadAuthenticationConfigurationItem.isConfigEnabled());
        downloadAnonymousCheckBox.addValueChangeListener(this);
        anonymousDownloadAuthenticationConfigurationItem.addChangeListener(this);
        gridLayout.addComponent(downloadAnonymousCheckBox, 0, 3);
        gridLayout.addComponent(anonymousDownloadAuthenticationConfigurationItem, 1, 3);

        final Link linkToSecurityHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiProperties.getLinks().getDocumentation().getSecurity());
        gridLayout.addComponent(linkToSecurityHelp, 2, 3);
        gridLayout.setComponentAlignment(linkToSecurityHelp, Alignment.BOTTOM_RIGHT);

        vLayout.addComponent(gridLayout);
        rootPanel.setContent(vLayout);
        setCompositionRoot(rootPanel);
    }

    @Override
    public void save() {
        certificateAuthenticationConfigurationItem.save();
        targetSecurityTokenAuthenticationConfigurationItem.save();
        gatewaySecurityTokenAuthenticationConfigurationItem.save();
        anonymousDownloadAuthenticationConfigurationItem.save();
    }

    @Override
    public void undo() {
        certificateAuthenticationConfigurationItem.undo();
        targetSecurityTokenAuthenticationConfigurationItem.undo();
        gatewaySecurityTokenAuthenticationConfigurationItem.undo();
        anonymousDownloadAuthenticationConfigurationItem.undo();
        certificateAuthCheckbox.setValue(certificateAuthenticationConfigurationItem.isConfigEnabled());
        targetSecTokenCheckBox.setValue(targetSecurityTokenAuthenticationConfigurationItem.isConfigEnabled());
        gatewaySecTokenCheckBox.setValue(gatewaySecurityTokenAuthenticationConfigurationItem.isConfigEnabled());
        downloadAnonymousCheckBox.setValue(anonymousDownloadAuthenticationConfigurationItem.isConfigEnabled());
    }

    @Override
    public void configurationHasChanged() {
        notifyConfigurationChanged();
    }

    @Override
    public void valueChange(final ValueChangeEvent event) {

        if (!(event.getProperty() instanceof CheckBox)) {
            return;
        }

        notifyConfigurationChanged();

        final CheckBox checkBox = (CheckBox) event.getProperty();
        BooleanConfigurationItem configurationItem;

        if (gatewaySecTokenCheckBox.equals(checkBox)) {
            configurationItem = gatewaySecurityTokenAuthenticationConfigurationItem;
        } else if (targetSecTokenCheckBox.equals(checkBox)) {
            configurationItem = targetSecurityTokenAuthenticationConfigurationItem;
        } else if (certificateAuthCheckbox.equals(checkBox)) {
            configurationItem = certificateAuthenticationConfigurationItem;
        } else if (downloadAnonymousCheckBox.equals(checkBox)) {
            configurationItem = anonymousDownloadAuthenticationConfigurationItem;
        } else {
            return;
        }

        if (checkBox.getValue()) {
            configurationItem.configEnable();
        } else {
            configurationItem.configDisable();
        }
    }
}
