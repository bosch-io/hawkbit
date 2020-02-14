/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutoCleanupConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutoCloseConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.MultiAssignmentsConfigurationItem;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * View to configure the authentication mode.
 */
public class RepositoryConfigurationView extends CustomComponent {

    private static final String DIST_CHECKBOX_STYLE = "dist-checkbox-style";

    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final UiProperties uiProperties;

    private final ActionAutoCloseConfigurationItem actionAutocloseConfigurationItem;

    private final ActionAutoCleanupConfigurationItem actionAutocleanupConfigurationItem;

    private final MultiAssignmentsConfigurationItem multiAssignmentsConfigurationItem;

    private CheckBox actionAutoCloseCheckBox;
    private CheckBox actionAutoCleanupCheckBox;
    private CheckBox multiAssignmentsCheckBox;

    private final Binder<ProxySystemConfigWindow> binder;

    RepositoryConfigurationView(final VaadinMessageSource i18n, final UiProperties uiProperties,
            final ActionAutoCloseConfigurationItem actionAutocloseConfigurationItem,
            final ActionAutoCleanupConfigurationItem actionAutocleanupConfigurationItem,
            final MultiAssignmentsConfigurationItem multiAssignmentsConfigurationItem,
            final Binder<ProxySystemConfigWindow> binder) {
        this.i18n = i18n;
        this.uiProperties = uiProperties;
        this.actionAutocloseConfigurationItem = actionAutocloseConfigurationItem;
        this.actionAutocleanupConfigurationItem = actionAutocleanupConfigurationItem;
        this.multiAssignmentsConfigurationItem = multiAssignmentsConfigurationItem;
        this.binder = binder;

        init();
    }

    private void init() {

        final Panel rootPanel = new Panel();
        rootPanel.setSizeFull();

        rootPanel.addStyleName("config-panel");

        final VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSpacing(false);
        vLayout.setMargin(true);
        vLayout.setSizeFull();

        final Label header = new Label(i18n.getMessage("configuration.repository.title"));
        header.addStyleName("config-panel-header");
        vLayout.addComponent(header);

        final GridLayout gridLayout = new GridLayout(3, 3);
        gridLayout.setSpacing(true);

        gridLayout.setColumnExpandRatio(1, 1.0F);
        gridLayout.setSizeFull();

        actionAutoCloseCheckBox = new CheckBox();
        actionAutoCloseCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        actionAutoCloseCheckBox.setId(UIComponentIdProvider.REPOSITORY_ACTIONS_AUTOCLOSE_CHECKBOX);
        actionAutoCloseCheckBox.setEnabled(!binder.getBean().isMultiAssignments());
        actionAutocloseConfigurationItem.setEnabled(!binder.getBean().isMultiAssignments());
        binder.bind(actionAutoCloseCheckBox, ProxySystemConfigWindow::isActionAutoclose,
                ProxySystemConfigWindow::setActionAutoclose);
        gridLayout.addComponent(actionAutoCloseCheckBox, 0, 0);
        gridLayout.addComponent(actionAutocloseConfigurationItem, 1, 0);

        multiAssignmentsCheckBox = new CheckBox();
        multiAssignmentsCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        multiAssignmentsCheckBox.setId(UIComponentIdProvider.REPOSITORY_MULTI_ASSIGNMENTS_CHECKBOX);
        multiAssignmentsCheckBox.setEnabled(!binder.getBean().isMultiAssignments());
        multiAssignmentsConfigurationItem.setEnabled(!binder.getBean().isMultiAssignments());
        multiAssignmentsCheckBox.addValueChangeListener(event -> {
            actionAutoCloseCheckBox.setEnabled(!event.getValue());
            actionAutocloseConfigurationItem.setEnabled(!event.getValue());
            multiAssignmentsConfigurationItem.setSettingsVisible(event.getValue());
        });
        binder.bind(multiAssignmentsCheckBox, ProxySystemConfigWindow::isMultiAssignments,
                ProxySystemConfigWindow::setMultiAssignments);
        gridLayout.addComponent(multiAssignmentsCheckBox, 0, 1);
        gridLayout.addComponent(multiAssignmentsConfigurationItem, 1, 1);

        actionAutoCleanupCheckBox = new CheckBox();
        actionAutoCleanupCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        actionAutoCleanupCheckBox.setId(UIComponentIdProvider.REPOSITORY_ACTIONS_AUTOCLEANUP_CHECKBOX);
        actionAutoCleanupCheckBox.addValueChangeListener(
                event -> actionAutocleanupConfigurationItem.setSettingsVisible(event.getValue()));
        binder.bind(actionAutoCleanupCheckBox, ProxySystemConfigWindow::isActionAutocleanup,
                ProxySystemConfigWindow::setActionAutocleanup);
        gridLayout.addComponent(actionAutoCleanupCheckBox, 0, 2);
        gridLayout.addComponent(actionAutocleanupConfigurationItem, 1, 2);

        final Link linkToProvisioningHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiProperties.getLinks().getDocumentation().getProvisioningStateMachine());
        gridLayout.addComponent(linkToProvisioningHelp, 2, 2);
        gridLayout.setComponentAlignment(linkToProvisioningHelp, Alignment.BOTTOM_RIGHT);

        vLayout.addComponent(gridLayout);
        rootPanel.setContent(vLayout);
        setCompositionRoot(rootPanel);
    }

    public void disableMultipleAssignmentOption() {
        multiAssignmentsCheckBox.setEnabled(false);
        multiAssignmentsConfigurationItem.setEnabled(false);
    }

}
