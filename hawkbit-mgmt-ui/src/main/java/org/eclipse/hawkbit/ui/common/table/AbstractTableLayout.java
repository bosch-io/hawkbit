/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.table;

import org.eclipse.hawkbit.ui.common.detailslayout.AbstractTableDetailsLayout;

import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Parent class for table layout.
 * 
 *
 *
 * 
 */
public abstract class AbstractTableLayout extends VerticalLayout {

    private static final long serialVersionUID = 8611248179949245460L;

    private AbstractTableHeader tableHeader;

    private AbstractTable table;

    private AbstractTableDetailsLayout detailsLayout;

    protected void init(final AbstractTableHeader tableHeader, final AbstractTable table,
            final AbstractTableDetailsLayout detailsLayout) {
        this.tableHeader = tableHeader;
        this.table = table;
        this.detailsLayout = detailsLayout;
        buildLayout();
    }

    private void buildLayout() {
        setSizeFull();
        setSpacing(true);
        setMargin(false);
        setStyleName("group");
        final VerticalLayout tableHeaderLayout = new VerticalLayout();
        tableHeaderLayout.setSizeFull();
        tableHeaderLayout.setSpacing(false);
        tableHeaderLayout.setMargin(false);

        tableHeaderLayout.setStyleName("table-layout");
        tableHeaderLayout.addComponent(tableHeader);

        tableHeaderLayout.setComponentAlignment(tableHeader, Alignment.TOP_CENTER);
        if (isShortCutKeysRequired()) {
            final Panel tablePanel = new Panel();
            tablePanel.setStyleName("table-panel");
            tablePanel.setHeight(100.0f, Unit.PERCENTAGE);
            tablePanel.setContent(table);
            tablePanel.addActionHandler(getShortCutKeysHandler());
            tablePanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
            tableHeaderLayout.addComponent(tablePanel);
            tableHeaderLayout.setComponentAlignment(tablePanel, Alignment.TOP_CENTER);
            tableHeaderLayout.setExpandRatio(tablePanel, 1.0f);
        } else {
            tableHeaderLayout.addComponent(table);
            tableHeaderLayout.setComponentAlignment(table, Alignment.TOP_CENTER);
            tableHeaderLayout.setExpandRatio(table, 1.0f);
        }

        addComponent(tableHeaderLayout);
        addComponent(detailsLayout);
        setComponentAlignment(tableHeaderLayout, Alignment.TOP_CENTER);
        setComponentAlignment(detailsLayout, Alignment.TOP_CENTER);
        setExpandRatio(tableHeaderLayout, 1.0f);
    }

    /**
     * If any short cut keys required on the table.
     * 
     * @return true if required else false. Default is 'false'.
     */
    protected boolean isShortCutKeysRequired() {
        return false;
    }

    /**
     * Get the action handler for the short cut keys.
     * 
     * @return reference of {@link Handler} to handler the short cut keys.
     *         Default is null.
     */
    protected Handler getShortCutKeysHandler() {
        return null;
    }

    public void setShowFilterButtonVisible(final boolean visible) {
        tableHeader.setFilterButtonsIconVisible(visible);
    }

}
