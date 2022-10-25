/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import com.vaadin.server.Sizeable;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowBuilder;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.CommonUiDependencies;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetConfirmationOptions;

import com.vaadin.ui.Window;

import java.util.function.Consumer;

import static org.eclipse.hawkbit.ui.utils.UIComponentIdProvider.AUTO_CONFIRMATION_ACTIVATION_DIALOG;

/**
 * Target auto confirmation activation windows builder
 */
public class TargetAutoConfActivationWindowBuilder extends AbstractEntityWindowBuilder<ProxyTargetConfirmationOptions> {

    private final UiProperties uiProperties;

    private final TenantAware tenantAware;
    private final Consumer<ProxyTargetConfirmationOptions> onActivatedOptions;

    protected TargetAutoConfActivationWindowBuilder(final CommonUiDependencies uiDependencies,
            final UiProperties uiProperties, final TenantAware tenantAware,
            final Consumer<ProxyTargetConfirmationOptions> onActivatedOptions) {
        super(uiDependencies);
        this.uiProperties = uiProperties;
        this.tenantAware = tenantAware;
        this.onActivatedOptions = onActivatedOptions;
    }

    @Override
    protected String getHelpLink() {
        return uiProperties.getLinks().getDocumentation().getAutoConfirmationView();
    }

    @Override
    protected String getWindowId() {
        return AUTO_CONFIRMATION_ACTIVATION_DIALOG;
    }

    @Override
    public Window getWindowForAdd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Window getWindowForUpdate(final ProxyTargetConfirmationOptions entity) {
        final TargetAutoConfActivationLayout layout = new TargetAutoConfActivationLayout(uiDependencies.getI18n(),
                tenantAware, onActivatedOptions);
        layout.setEntity(entity);

        final CommonDialogWindow window = createWindow(layout.getRootComponent(), null);
        window.hideMandatoryExplanation();
        window.setAssistivePrefix(
                getI18n().getMessage("caption.target.auto.confirmation.activate.prefix") + " " + "<b>");
        window.setCaptionAsHtml(false);
        window.setCaption(entity.getControllerId());
        window.setAssistivePostfix("</b>");

        layout.setSaveCallback(window::setCloseListener);

        window.setHeight(320, Sizeable.Unit.PIXELS);
        window.setWidth(400, Sizeable.Unit.PIXELS);
        window.setSaveButtonEnabled(true);
        return window;
    }

}
