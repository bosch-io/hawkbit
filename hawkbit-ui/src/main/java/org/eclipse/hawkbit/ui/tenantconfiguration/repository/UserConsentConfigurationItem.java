/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.repository;

import com.vaadin.data.Binder;
import com.vaadin.ui.VerticalLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigRepository;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * This class represents the UI item for enabling/disabling the
 * User-consent flow feature as part of the repository configuration view.
 */
public class UserConsentConfigurationItem extends VerticalLayout {

  private static final long serialVersionUID = 1L;

  private static final String MSG_KEY_CHECKBOX = "label.configuration.repository.userconsent";
  private final VaadinMessageSource i18n;

  /**
   * Constructor.
   *
   * @param i18n
   *            VaadinMessageSource
   */
  public UserConsentConfigurationItem(final VaadinMessageSource i18n) {
    this.i18n = i18n;
    this.setSpacing(false);
    this.setMargin(false);
    addComponent(SPUIComponentProvider.generateLabel(i18n, MSG_KEY_CHECKBOX));
  }
}