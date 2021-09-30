/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterSingleButtonClick;

import java.util.function.BiConsumer;

/**
 * Single button click behaviour of custom target filter buttons layout.
 *
 */
public class TargetTypeFilterButtonClick extends AbstractFilterSingleButtonClick<ProxyTargetType> {
    private static final long serialVersionUID = 1L;

    private final transient BiConsumer<ProxyTargetType, ClickBehaviourType> filterChangedCallback;

    /**
     * Constructor
     *
     * @param filterChangedCallback
     *            filterChangedCallback
     */
    public TargetTypeFilterButtonClick(
            final BiConsumer<ProxyTargetType, ClickBehaviourType> filterChangedCallback) {
        this.filterChangedCallback = filterChangedCallback;
    }

    @Override
    protected void filterUnClicked(ProxyTargetType clickedFilter) {
        filterChangedCallback.accept(clickedFilter, ClickBehaviourType.UNCLICKED);
    }

    @Override
    protected void filterClicked(ProxyTargetType clickedFilter) {
        filterChangedCallback.accept(clickedFilter, ClickBehaviourType.CLICKED);
    }
}
