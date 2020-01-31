/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.authentication;

import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.tenantconfiguration.generic.AbstractBooleanTenantConfigurationItem;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * This class represents the UI item for the anonymous download by in the
 * authentication configuration view.
 */
public class AnonymousDownloadAuthenticationConfigurationItem extends AbstractBooleanTenantConfigurationItem {

    private static final long serialVersionUID = 1L;

    public AnonymousDownloadAuthenticationConfigurationItem(
            final TenantConfigurationManagement tenantConfigurationManagement, final VaadinMessageSource i18n) {
        super(TenantConfigurationKey.ANONYMOUS_DOWNLOAD_MODE_ENABLED, tenantConfigurationManagement, i18n);
        super.init("label.configuration.anonymous.download");
    }

    @Override
    public void configEnable() {
    }

    @Override
    public void configDisable() {
    }

    @Override
    public void save() {
    }

    @Override
    public void undo() {
    }

}
