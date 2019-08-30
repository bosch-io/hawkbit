/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Software Module Source Type grid which is shown on the Distribution Type
 * Create/Update popup layout.
 */
public class SmTypeSourceGrid extends Grid<ProxyType> {
    private static final long serialVersionUID = 1L;

    private static final String SM_TYPE_SOURCE_NAME_ID = "smTypeSourceName";

    private final VaadinMessageSource i18n;

    public SmTypeSourceGrid(final VaadinMessageSource i18n) {
        this.i18n = i18n;

        init();
    }

    private void init() {
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_SMALL);
        addStyleName("dist_type_twin-table");

        setId(SPUIDefinitions.TWIN_TABLE_SOURCE_ID);
        setSelectionMode(SelectionMode.MULTI);
        setSizeFull();

        addColumns();
    }

    private void addColumns() {
        addColumn(ProxyType::getName).setId(SM_TYPE_SOURCE_NAME_ID)
                .setCaption(i18n.getMessage("header.dist.twintable.available"))
                .setDescriptionGenerator(ProxyType::getDescription).setExpandRatio(1);
    }
}
