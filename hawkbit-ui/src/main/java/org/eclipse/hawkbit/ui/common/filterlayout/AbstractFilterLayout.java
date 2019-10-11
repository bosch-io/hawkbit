/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Parent class for filter button layout.
 */
public abstract class AbstractFilterLayout extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    protected final transient UIEventBus eventBus;

    protected AbstractFilterLayout(final UIEventBus eventBus) {
        this.eventBus = eventBus;

        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    protected void buildLayout() {
        setWidth(SPUIDefinitions.FILTER_BY_TYPE_WIDTH, Unit.PIXELS);
        setStyleName("filter-btns-main-layout");
        setHeight(100.0F, Unit.PERCENTAGE);
        setSpacing(false);
        setMargin(false);

        final Component filterHeader = getFilterHeader();
        final Component filterButtons = getFilterButtons();
        // adding border
        filterButtons.addStyleName("filter-btns-layout");
        filterButtons.setSizeFull();

        addComponents(filterHeader, filterButtons);

        setComponentAlignment(filterHeader, Alignment.TOP_CENTER);
        setComponentAlignment(filterButtons, Alignment.TOP_CENTER);

        setExpandRatio(filterButtons, 1.0F);
    }

    protected void restoreState() {
        // TODO: check if needed, adapt as neccessary
    }

    protected abstract AbstractGridHeader getFilterHeader();

    // we use Component here due to NO TAG button
    protected abstract Component getFilterButtons();
}
